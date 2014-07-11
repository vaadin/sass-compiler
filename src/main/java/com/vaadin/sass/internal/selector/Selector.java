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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.visitor.Extension;

// note: a Selector is effectively immutable - only methods creating a new selector can modify its parts directly
public class Selector implements Serializable {

    /*
     * Current TODOs:
     * 
     * - Subsumption calculation mostly unimplemented.
     * 
     * - @extend support is limited
     * 
     * - Need to study and implement the sass-lang algorithm for weaving
     * together two nested selectors in @extend-unification. See:
     * https://github.com/nex3/sass/blob/stable/lib/sass/selector/sequence.rb
     * 
     * - Specificity (see http://www.w3.org/TR/css3-selectors/#specificity)
     * calculation not yet implemented. This is needed to control redundancy
     * elimination (see https://github.com/nex3/sass/issues/324).
     */

    private List<SelectorSegment> parts = new ArrayList<SelectorSegment>();

    private Selector() {
        /*
         * Initializes selector in an illegal state (simpleSeqs empty), hence
         * private.
         */
    }

    public Selector(Selector source) {
        this(source.parts);
    }

    protected Selector(List<SelectorSegment> parts) {
        this.parts.addAll(parts);
    }

    /**
     * Non-nested selector
     */
    public Selector(SimpleSelectorSequence simple) {
        parts.add(simple);
    }

    /**
     * Selector with a leading combinator ("> foo")
     */
    public Selector(Combinator comb, SimpleSelectorSequence simpl) {
        parts.add(comb);
        parts.add(simpl);
    }

    /**
     * Joins two selectors in sequence with given combinator. Either first and
     * comb are both not null, or both null.
     */
    public Selector(Selector first, Combinator comb, Selector second) {
        assert (comb == null && first == null)
                || (comb != null && first != null);

        if (first != null) {
            parts.addAll(first.parts);
            parts.add(comb);
        }
        parts.addAll(second.parts);
    }

    private boolean hasLeadingCombinator() {
        return parts.size() > 0 && parts.get(0) instanceof Combinator;
    }

    public boolean isSimple() {
        return parts.size() == 1
                && (parts.get(0) instanceof SimpleSelectorSequence);
    }

    public SimpleSelectorSequence firstSimple() {
        for (SelectorSegment segment : parts) {
            if (segment instanceof SimpleSelectorSequence) {
                return (SimpleSelectorSequence) segment;
            }
        }
        return null;
    }

    private SimpleSelectorSequence lastSimple() {
        if (parts.size() == 0
                || !(parts.get(parts.size() - 1) instanceof SimpleSelectorSequence)) {
            throw new ParseException("Invalid last part of selector: "
                    + toString());
        }
        return (SimpleSelectorSequence) parts.get(parts.size() - 1);
    }

    /**
     * For a nested selector "A > B > C > ...", returns "B > C > ...". For a
     * non-nested selector, returns null.
     */
    private Selector tail() {
        if (parts.size() > 2) {
            return new Selector(parts.subList(2, parts.size()));
        } else {
            return null;
        }
    }

    /**
     * Returns whether this selector matches (at least) all elements that
     * selector matches.
     */
    public boolean subsumes(Selector that) {
        // reflexive case
        if (that.equals(this)) {
            return true;
        }

        // a selector with leading combinator cannot subsume/be subsumed
        if (hasLeadingCombinator() || that.hasLeadingCombinator()) {
            return false;
        }

        // a deeper nested selector is more specific and cannot subsume this
        if (parts.size() > that.parts.size()) {
            return false;
        }

        // if this selector is not nested, its simple selector must subsume the
        // last simple selector of that
        if (isSimple()) {
            return firstSimple().subsumes(that.lastSimple());
        }

        assert parts.size() > 2;
        assert parts.size() <= that.parts.size();

        if (firstSimple().subsumes(that.firstSimple())) {
            return tail().subsumes(that.tail());
        }

        return false;
    }

    /**
     * Combine a parent selector with this selector, replacing parent reference
     * selectors (&-selector) in this with replacement if any and return the
     * modified copy of this. If there are no parent reference selectors, simply
     * concatenates the parent selector and this with the DESCENDANT
     * relationship.
     * 
     * @param replacement
     *            replacement selector, or null in which case & is just removed
     * @return modified copy of this with parent selector prepended or
     *         substituted for the parent reference selector
     */
    public Selector replaceParentReference(Selector replacement) {
        boolean foundParentReference = false;
        Selector sel = new Selector();
        for (int i = 0; i < parts.size(); i++) {
            SelectorSegment segment = parts.get(i);
            if (segment instanceof SimpleSelectorSequence) {
                SimpleSelectorSequence simple = (SimpleSelectorSequence) segment;
                if (ParentSelector.it.equals(simple.getTypeSelector())) {
                    foundParentReference = true;
                    if (replacement != null) {
                        if (replacement.hasLeadingCombinator()) {
                            throw new ParseException(
                                    "Parent selector should not have a leading combinator when using & parent selector reference");
                        }
                        // splice in each sequence from replacement
                        sel.parts.addAll(replacement.parts);
                        // replace "&" with new type selector in the last part,
                        // keeping all the non-type selectors
                        SimpleSelectorSequence last = replacement.lastSimple();
                        last = last.union(simple.withoutTypeSelector());
                        sel.parts.set(sel.parts.size() - 1, last);
                    } else {
                        // remove the type selector "&"
                        sel.parts.add(simple.withoutTypeSelector());
                    }
                } else {
                    // no parent to replace
                    sel.parts.add(simple);
                }
            } else {
                sel.parts.add(segment);
            }
        }

        if (foundParentReference) {
            return sel;
        } else if (replacement != null) {
            // no explicit parent reference selector, simply prepend parent
            return new Selector(replacement, Combinator.DESCENDANT, this);
        } else {
            // no modifications necessary
            return this;
        }
    }

    /**
     * Replace variables with their values (textually) in subselectors
     */
    public Selector replaceVariables(ScssContext context) {
        // It would be sensible to rethink the whole handling of interpolations
        Selector sel = new Selector();
        for (SelectorSegment segment : parts) {
            if (segment instanceof SimpleSelectorSequence) {
                SimpleSelectorSequence seq = (SimpleSelectorSequence) segment;
                seq = seq.replaceVariables(context);
                sel.parts.add(seq);
            } else {
                sel.parts.add(segment);
            }
        }
        return sel;
    }

    /**
     * Appends the given sequence of simple selectors to this selector using
     * combinator c.
     */
    public Selector createNested(Combinator c, SimpleSelectorSequence s) {
        Selector ext = new Selector();
        ext.parts.addAll(parts);
        ext.parts.add(c);
        ext.parts.add(s);
        return ext;
    }

    /**
     * Combine this selector with an @extend. This effectively replaces
     * instances of extendSelector in this with extending and returns the new
     * copy of this.
     * 
     * @param extension
     *            information about extend selector, extending selector and
     *            context
     * @return new selector replacing this
     */
    public Selector replace(Extension extension) {
        Selector sel = new Selector();
        List<SelectorSegment> previousSegments = new ArrayList<SelectorSegment>();
        for (SelectorSegment segment : parts) {
            if (segment instanceof SimpleSelectorSequence
                    && extension.extendSelector.equals(segment)) {
                // handle nested @extend
                Selector newExtending = extension.replacingSelector
                        .removePrefix(previousSegments);
                if (extension.context != null) {
                    newExtending = newExtending.removePrefix(extension.context);
                }
                // simply replace the whole part
                sel.parts.addAll(newExtending.parts);
                // stop keeping track of previous segments to avoid
                // multiple removal
                previousSegments = null;
            } else {
                if (previousSegments != null) {
                    previousSegments.add(segment);
                }
                sel.parts.add(segment);
            }
        }
        if (sel.parts.size() == 0) {
            return sel;
        }
        assert (sel.parts.get(sel.parts.size() - 1) instanceof SimpleSelectorSequence);
        // if last part of result is a partial match, use SSS.unify()
        // TODO this is limited/broken
        SimpleSelectorSequence seq = sel.lastSimple();
        SimpleSelectorSequence lastUnified = seq.unify(
                extension.extendSelector,
                extension.replacingSelector.lastSimple());
        if (lastUnified != null) {
            sel.parts.remove(sel.parts.size() - 1);
            sel.parts.addAll(extension.replacingSelector.parts);
            sel.parts.set(sel.parts.size() - 1, lastUnified);
        }

        return sel;
    }

    /**
     * Remove toRemove segments from this if and only if this starts with the
     * whole list.
     * 
     * @param toRemove
     *            segments to remove or null if none
     * @return new selector or this if no modifications are needed
     */
    private Selector removePrefix(List<SelectorSegment> toRemove) {
        if (toRemove == null || parts.size() < toRemove.size()) {
            return this;
        }
        for (int i = 0; i < toRemove.size(); ++i) {
            if (!parts.get(i).equals(toRemove.get(i))) {
                return this;
            }
        }
        return new Selector(parts.subList(toRemove.size(), parts.size()));
    }

    /**
     * Remove toRemove segments from this selector and returns the updated
     * selector. If any entry on toRemove is a prefix of this, its segments are
     * removed from this and other prefixes are not checked. Otherwise, this is
     * returned unmodified.
     * 
     * @param toRemove
     *            selectors one of which to remove or empty collection if none
     * @return new selector or this if no modifications are needed
     */
    private Selector removePrefix(Collection<Selector> toRemove) {
        for (Selector sel : toRemove) {
            if (hasPrefix(sel)) {
                return removePrefix(sel.parts);
            }
        }
        return this;
    }

    private boolean hasPrefix(Selector prefix) {
        if (parts.size() < prefix.parts.size()) {
            return false;
        }
        for (int i = 0; i < prefix.parts.size(); ++i) {
            if (!parts.get(i).equals(prefix.parts.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SelectorSegment segment : parts) {
            sb.append(segment.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Selector)) {
            return false;
        }
        Selector thatSelector = (Selector) that;
        return parts.equals(thatSelector.parts);

    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    /**
     * Returns whether the selector contains a placeholder selector
     */
    public boolean isPlaceholder() {
        for (SelectorSegment segment : parts) {
            if (segment instanceof SimpleSelectorSequence
                    && ((SimpleSelectorSequence) segment).isPlaceholder()) {
                return true;
            }
        }
        return false;
    }

}
