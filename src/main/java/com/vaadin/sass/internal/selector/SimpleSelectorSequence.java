/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.sass.internal.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;

/**
 * Immutable simple list of simple selector segments (e.g. "a.foo.bar" consists
 * of "a", ".foo" and ".bar").
 * 
 * For full selectors (e.g. "a.foo .bar") see {@link Selector}.
 */
public class SimpleSelectorSequence implements SelectorSegment {

    private List<SimpleSelector> selectors;

    public SimpleSelectorSequence() {
        selectors = Collections.emptyList();
    }

    /**
     * Constructs a {@link SimpleSelectorSequence} from a list of simple
     * selectors.
     * 
     * @param seq
     *            list of simple selectors
     */
    public SimpleSelectorSequence(List<SimpleSelector> seq) {
        // TODO this could be optimized by not always creating a new list, but
        // that would open the possibility of the caller e.g. accidentally
        // giving a list that is backing/backed by another list or otherwise
        // shared
        selectors = Collections.unmodifiableList(new ArrayList<SimpleSelector>(
                seq));
    }

    public SimpleSelectorSequence(SimpleSelectorSequence prior,
            SimpleSelector simpleSelector) {
        ArrayList<SimpleSelector> list = new ArrayList<SimpleSelector>();
        if (prior != null) {
            list.addAll(prior.selectors);
        }
        list.add(simpleSelector);
        selectors = Collections.unmodifiableList(list);
    }

    public SimpleSelectorSequence(SimpleSelector simpleSelector,
            SimpleSelectorSequence tail) {
        ArrayList<SimpleSelector> list = new ArrayList<SimpleSelector>();
        list.add(simpleSelector);
        if (tail != null) {
            list.addAll(tail.selectors);
        }
        selectors = Collections.unmodifiableList(list);
    }

    /**
     * Returns this \ that, set-theoretically
     */
    public SimpleSelectorSequence difference(SimpleSelectorSequence that) {
        ArrayList<SimpleSelector> result = new ArrayList<SimpleSelector>(
                selectors);
        result.removeAll(that.selectors);
        return new SimpleSelectorSequence(result);
    }

    /**
     * Returns this followed by all elements in that but not in this
     */
    public SimpleSelectorSequence union(SimpleSelectorSequence that) {
        ArrayList<SimpleSelector> union = new ArrayList<SimpleSelector>(
                selectors);
        ArrayList<SimpleSelector> newEntries = new ArrayList<SimpleSelector>(
                that.selectors);
        newEntries.removeAll(selectors);
        union.addAll(newEntries);
        ensureOrdering(union);
        return new SimpleSelectorSequence(union);
    }

    /**
     * Called after combining SimpleSelectSequences. Ensures that the type
     * selector, if present, appears first in list. Also ensures that
     * pseudoclass-selectors and pseudoelement-selectors, if present, appear
     * next to last / last in list. Mutating for efficiency.
     * 
     * @param list
     *            the simple selector list to sort
     */
    private static void ensureOrdering(ArrayList<SimpleSelector> list) {

        Comparator<SimpleSelector> c = new Comparator<SimpleSelector>() {

            public int getOrdinal(SimpleSelector it) {
                if (it instanceof TypeSelector) {
                    return -1;
                } else if (it instanceof PseudoElementSelector) {
                    return 3;
                } else if (it instanceof PseudoClassSelector) {
                    return 2;
                } else {
                    return 0;
                }
            }

            @Override
            public int compare(SimpleSelector it, SimpleSelector that) {
                return getOrdinal(it) - getOrdinal(that);
            }
        };
        Collections.sort(list, c);
    }

    /**
     * Tries to unify this with the "extending" simple selector sequence by
     * extend. The simple selector sequence extend is a unifier only if it
     * subsumes (is more general than) this selector. The result is this
     * selector minus simple selectors in extend plus simple selectors in
     * extending.
     * 
     * Example:
     * 
     * this = a.foo.bar, extend = a.foo, extending = b.baz result = b.bar.baz
     * 
     * Note: if extend lacks a type selector, this and extending must share the
     * same type selector.
     */
    public SimpleSelectorSequence unify(SimpleSelectorSequence extend,
            SimpleSelectorSequence extending) {

        if (extend != null && extend.subsumes(this)) {
            /*
             * Check for type selector compatibility with the extending block.
             * Examples:
             * 
             * The following should unify:
             * 
             * - a.foo with b.bar { @extend a } ---> b.foo.bar
             * 
             * 
             * The following should NOT unify:
             * 
             * - a.foo with b.bar { @extend .foo }
             */
            if (extend.getTypeSelector() == null
                    && extending.getTypeSelector() != null) {
                if (!(getTypeSelector() == null || extending.getTypeSelector()
                        .equals(getTypeSelector()))) {
                    return null;
                }
            }

            SimpleSelectorSequence retval = difference(extend).union(extending);

            // Do not return selectors such as #foo#bar
            if (retval.cannotMatchAnything()) {
                return null;
            }

            return retval;

        } else {
            // we have a more specific @extend-selector, hence cannot unify
            return null;
        }
    }

    /**
     * Returns true iff sequence contains two incompatible id-selectors
     * (#foo#bar)
     */
    private boolean cannotMatchAnything() {
        IdSelector id = null;
        for (SimpleSelector s : selectors) {
            if (s instanceof IdSelector) {
                if (id == null) {
                    id = (IdSelector) s;
                } else {
                    if (!id.equals(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public TypeSelector getTypeSelector() {
        SimpleSelector head = selectors.get(0);
        return head instanceof TypeSelector ? (TypeSelector) head : null;
    }

    public SimpleSelectorSequence getNonTypeSelectors() {
        SimpleSelector head = selectors.get(0);
        if (head instanceof TypeSelector) {
            // note that the sublist may be backed by the original list
            List<SimpleSelector> tail = selectors.subList(1, selectors.size());
            return new SimpleSelectorSequence(tail);
        } else {
            return this;
        }
    }

    // optimization - note that the result is backed by selectors of this
    private List<SimpleSelector> getNonTypeSelectorList() {
        SimpleSelector head = selectors.get(0);
        if (head instanceof TypeSelector) {
            // note that the sublist may be backed by the original list
            return selectors.subList(1, selectors.size());
        } else {
            return selectors;
        }
    }

    /**
     * Returns whether this simple selector sequence matches (at least) all
     * elements that simple selector sequence matches. True if the simple
     * selectors in this is a subset of those in that. If type selector is
     * universal in this, compare other kinds of simple selectors only.
     */
    public boolean subsumes(SimpleSelectorSequence that) {
        TypeSelector ts = getTypeSelector();
        if (ts == null || ts.equals(UniversalSelector.it)) {
            return that.getNonTypeSelectorList().containsAll(
                    getNonTypeSelectorList());
        }
        return that.selectors.containsAll(selectors);
    }

    public SimpleSelectorSequence replaceVariables(ScssContext context) {
        ArrayList<SimpleSelector> list = new ArrayList<SimpleSelector>();
        for (SimpleSelector s : selectors) {
            list.add(s.replaceVariables(context));
        }
        return new SimpleSelectorSequence(list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SimpleSelector s : selectors) {
            sb.append(s.toString());
        }
        return sb.toString();
    }

    /**
     * Returns the same sequence, but without type selector if one was present.
     */
    public SimpleSelectorSequence withoutTypeSelector() {
        ArrayList<SimpleSelector> list = new ArrayList<SimpleSelector>();
        for (SimpleSelector sel : selectors) {
            if (!(sel instanceof TypeSelector)) {
                list.add(sel);
            }
        }
        return new SimpleSelectorSequence(list);
    }

    /**
     * Returns whether this selector contains a placeholder (%-selector)
     */
    public boolean isPlaceholder() {
        for (SimpleSelector s : selectors) {
            if (s instanceof PlaceholderSelector) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }
        return selectors.equals(((SimpleSelectorSequence) obj).selectors);
    }

    @Override
    public int hashCode() {
        return selectors.hashCode();
    }

}
