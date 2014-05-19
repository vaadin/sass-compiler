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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.vaadin.sass.internal.tree.VariableNode;

public class SimpleSelectorSequence extends ArrayList<SimpleSelector> implements
        SelectorSegment {

    public SimpleSelectorSequence() {
        super();
    }

    public SimpleSelectorSequence(Collection<SimpleSelector> seq) {
        super(seq);
    }

    public SimpleSelectorSequence(SimpleSelectorSequence prior,
            SimpleSelector simpleSelector) {
        if (prior != null) {
            addAll(prior);
        }
        add(simpleSelector);
    }

    public SimpleSelectorSequence(SimpleSelector simpleSelector,
            SimpleSelectorSequence tail) {
        add(simpleSelector);
        if (tail != null) {
            addAll(tail);
        }
    }

    /**
     * Returns this \ that, set-theoretically
     */
    public SimpleSelectorSequence difference(SimpleSelectorSequence that) {
        SimpleSelectorSequence seq = new SimpleSelectorSequence(this);
        seq.removeAll(that);
        return seq;
    }

    /**
     * Returns this followed by all elements in that but not in this
     */
    public SimpleSelectorSequence union(SimpleSelectorSequence that) {
        SimpleSelectorSequence union = new SimpleSelectorSequence(this);
        union.addAll(that.difference(this));
        union.ensureOrdering();
        return union;
    }

    /**
     * Called after combining SimpleSelectSequences. Ensures that the type
     * selector, if present, appears first in list. Also ensures that
     * pseudoclass-selectors and pseudoelement-selectors, if present, appear
     * next to last / last in list. Mutating for efficiency.
     */
    private void ensureOrdering() {

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
        Collections.sort(this, c);
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
        for (SimpleSelector s : this) {
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
        SimpleSelector head = get(0);
        return head instanceof TypeSelector ? (TypeSelector) head : null;
    }

    public SimpleSelectorSequence getNonTypeSelectors() {
        SimpleSelector head = get(0);
        return head instanceof TypeSelector ? new SimpleSelectorSequence(
                subList(1, size())) : this;
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
            return that.getNonTypeSelectors()
                    .containsAll(getNonTypeSelectors());
        }
        return that.containsAll(this);
    }

    public SimpleSelectorSequence replaceVariable(VariableNode var) {
        SimpleSelectorSequence newSeq = new SimpleSelectorSequence();
        for (SimpleSelector s : this) {
            newSeq.add(s.replaceVariable(var));
        }
        return newSeq;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SimpleSelector s : this) {
            sb.append(s.toString());
        }
        return sb.toString();
    }

    /**
     * Returns the same sequence, but without type selector if one was present.
     */
    public SimpleSelectorSequence withoutTypeSelector() {
        SimpleSelectorSequence seq = new SimpleSelectorSequence();
        for (SimpleSelector sel : this) {
            if (!(sel instanceof TypeSelector)) {
                seq.add(sel);
            }
        }
        return seq;
    }

    /**
     * Returns whether this selector contains a placeholder (%-selector)
     */
    public boolean isPlaceholder() {
        for (SimpleSelector s : this) {
            if (s instanceof PlaceholderSelector) {
                return true;
            }
        }
        return false;
    }

}
