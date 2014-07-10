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
package com.vaadin.sass.internal.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;

/**
 * StringInterpolationSequence is used for representing sequences consisting of
 * strings and interpolation.
 * 
 * An unevaluated StringInterpolationSequence consists of StringItem (a wrapper
 * for a String) and Interpolation objects. The result of calling
 * replaceInterpolation is a StringInterpolationSequence where occurrences of
 * interpolation have been replaced with the contents of the interpolation.
 * 
 * @author Vaadin
 * 
 */
public class StringInterpolationSequence implements Serializable {
    private boolean containsInterpolation = false;
    private SassList items;

    /**
     * Creates a new StringInterpolationSequence containing only the given item.
     * 
     */
    public StringInterpolationSequence(String value) {
        this(Collections.<SassListItem> singletonList(new StringItem(value)));
    }

    /**
     * Creates a new StringInterpolationSequence. The list sequence should only
     * contain StringItem and Interpolation objects.
     * 
     * @param sequence
     *            A list of StringItem and Interpolation objects.
     */
    public StringInterpolationSequence(List<SassListItem> sequence) {
        for (SassListItem item : sequence) {
            if (item instanceof Interpolation) {
                containsInterpolation = true;
            } else {
                String itemString = item.printState();
                if (itemString.contains("#{")) {
                    // This is inexact but sufficient for avoiding most cases of
                    // unnecessary evaluation.
                    containsInterpolation = true;
                }
            }
        }
        items = new SassList(SassList.Separator.SPACE, sequence);
    }

    private StringInterpolationSequence(SassList list) {
        this(list.getItems());
    }

    /**
     * Creates a new sequence that is obtained from this by replacing all
     * variables occurring in expressions. Also replaces functions, arithmetic
     * expressions and interpolation if all variables have been set.
     * 
     * @param context
     *            current compilation context
     * @return A new StringInterpolationSequence.
     */
    public StringInterpolationSequence replaceVariables(ScssContext context) {
        if (!containsInterpolation()) {
            return this;
        }
        SassList resultList = items.replaceVariables(context);
        if (!resultList.containsVariable()) {
            resultList = resultList.evaluateFunctionsAndExpressions(context,
                    false);
            resultList = replaceInterpolation(resultList);
        }
        return new StringInterpolationSequence(resultList);
    }

    /**
     * Returns a new SassList that is obtained from list by replacing all
     * occurrences of interpolation with the contents of the interpolation.
     * 
     * It is assumed that variable replacement and the evaluation of functions
     * and arithmetic expressions have been performed before calling this
     * method.
     * 
     * @return A new StringInterpolationSequence.
     */
    private static SassList replaceInterpolation(SassList list) {
        List<SassListItem> newItems = new ArrayList<SassListItem>();
        for (SassListItem item : list) {
            if (item instanceof Interpolation) {
                newItems.add(((Interpolation) item).replaceInterpolation());
            } else {
                newItems.add(item);
            }
        }
        return new SassList(SassList.Separator.SPACE, newItems);
    }

    /**
     * Creates a new StringInterpolationSequence that contains all items of this
     * and other. Does not modify this or other.
     * 
     * @param other
     *            The StringInterpolationSequence to be appended to the end of
     *            this.
     * @return The appended StringInterpolationSequence.
     */
    public StringInterpolationSequence append(StringInterpolationSequence other) {
        ArrayList<SassListItem> result = new ArrayList<SassListItem>(
                items.getItems());
        result.addAll(other.items.getItems());
        return new StringInterpolationSequence(result);
    }

    @Override
    public String toString() {
        String result = "";
        for (SassListItem item : items) {
            result += item.printState();
        }
        return result;
    }

    /**
     * Returns true if this sequence contains interpolation, i.e. either an
     * interpolation object or a string containing interpolation. This method is
     * intended to be used as a quick test for avoiding repeated evaluation of
     * interpolation when none appear in the StringInterpolationSequence. As
     * such, the result false is always exact but if this method returns true,
     * it is still possible that there is no interpolation.
     * 
     * @return whether this sequence contains an Interpolation object or a
     *         string containing interpolation.
     */
    public boolean containsInterpolation() {
        return containsInterpolation;
    }

    public List<SassListItem> getItems() {
        return items.getItems();
    }
}