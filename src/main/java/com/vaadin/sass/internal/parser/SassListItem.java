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

import java.util.Collection;

import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.tree.VariableNode;

/**
 * SassListItem specifies the interface implemented by both list items
 * (LexicalUnitImpl) and lists (SassList). SassListItems are used as the
 * elements of a SassList so that a list can contain both single values and
 * other lists as its elements.
 * 
 * To allow unified handling of lists and single values, SassListItem specifies
 * several methods that are needed in both cases. This includes methods for
 * replacing variables with their values and toString-like methods. Several list
 * methods are also in the interface to allow a single value to behave like a
 * list. For instance, it is possible to add an element to a single value and
 * obtain a list as a result. The unified behavior of lists and single values is
 * relevant because the value of a Sass variable can be either a list or a
 * single value.
 * 
 */
public interface SassListItem {

    public int getLineNumber();

    public int getColumnNumber();

    /**
     * Checks whether the item contains an arithmetic expression.
     */
    public boolean containsArithmeticalOperator();

    /**
     * Evaluates the arithmetic expressions and functions of this item without
     * modifying this item.
     * 
     * @param evaluateArithmetics
     *            True indicates that the arithmetic expressions in this item
     *            should be evaluated. This parameter is used to handle the case
     *            where the operator '/' should not be interpreted as an
     *            arithmetic operation. The arithmetic expressions occurring in
     *            the parameter lists of functions will be evaluated even if
     *            evaluateArithmetics is false.
     * 
     * @return For single values, the result of the arithmetic expression or
     *         function. For a list, a copy of the list where the arithmetic
     *         expressions and functions have been replaced with their evaluated
     *         values.
     */
    public SassListItem evaluateFunctionsAndExpressions(
            boolean evaluateArithmetics);

    /**
     * Returns a new item that is otherwise equal to this one but all
     * occurrences of the given variables have been replaced by the values given
     * in the VariableNodes. Does not modify this item.
     * 
     * @param variables
     *            A list of nodes. The nodes contain the names and the current
     *            values of the variables to be replaced.
     * @return A SassListItem where all occurrences of variables have been
     *         replaced by their values.
     */
    public SassListItem replaceVariables(Collection<VariableNode> variable);

    /**
     * Updates all url's of this item by, e.g., adding the prefix to an url not
     * starting with slash "/" and not containing the symbol ":". This is a
     * mutating method, i.e. it modifies the contents of the current object.
     * 
     * @param prefix
     *            The prefix to be added.
     */
    public void updateUrl(String prefix);

    /**
     * Returns a string representation of this item. See
     * {@link LexicalUnitImpl#printState()}. For a list, the string
     * representation contains the list items separated with the separator
     * character of the list. No parentheses appear in the string representation
     * of a list, for valid CSS output.
     * 
     * @return A string representation of this item.
     */
    public String printState();

    /**
     * Return a string representation of this item using the given strategy of
     * converting items to strings. See
     * {@link LexicalUnitImpl#buildString(BuildStringStrategy)}.
     * 
     * @param strategy
     *            Specifies how an item is converted to a string. The strategy
     *            may use the toString- and printState-methods.
     * @return A string representation of this string.
     */
    public String buildString(BuildStringStrategy strategy);

    /**
     * Returns a string representation of this item with surrounding quotation
     * marks of the same type (" or ') removed. Quotation marks are only removed
     * from a single item or a list containing a single element and only one
     * pair of quotation marks is removed.
     * 
     * @return An unquoted string representation of this item.
     */
    public String unquotedString();

    // TODO this should be replaced with a more appropriate API for performing
    // the operations that we actually want to perform
    @Deprecated
    public LexicalUnitImpl getContainedValue();

    /**
     * Returns an object that is otherwise similar to this but chains of
     * LexicalUnitImpl objects have been replaced with SassExpressions
     * representing the same expressions.
     * 
     * @return An object equivalent to this but with chains of LexicalUnitImpl
     *         objects replaced with corresponding SassExpressions.
     */
    public SassListItem replaceChains();
}