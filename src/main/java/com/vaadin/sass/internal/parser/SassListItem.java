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
import java.util.Iterator;

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
public interface SassListItem extends Iterable<SassListItem> {

    public int getLineNumber();

    public int getColumnNumber();

    /**
     * Checks whether the item contains an arithmetic expression.
     */
    public boolean containsArithmeticalOperator();

    /**
     * Evaluates the arithmetic expressions of this item without modifying this
     * item.
     * 
     * @return For single values, the result of the arithmetic expression. For a
     *         list, a copy of the list where the arithmetic expressions have
     *         been replaced with their evaluated values.
     */
    public SassListItem evaluateArithmeticExpressions();

    /**
     * Returns a new item that is the result of evaluating all functions in this
     * item. Does not modify this item. If the item does not change as a result
     * of evaluating functions, an implementation can return the item itself.
     * Otherwise a new item should be returned. For a list with both changed and
     * unchanged items, the unchanged items can be references to the same
     * objects as in the original list.
     */
    public SassListItem replaceFunctions();

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
     * Returns a new item that is otherwise equal to this one but all
     * occurrences of the given variable have been replaced by the value given
     * in the VariableNode.
     * 
     * Note that unlike replaceVariables, this method may modify the current
     * item.
     * 
     * @param variable
     *            A node containing the name and the current value of the
     *            variable to be replaced.
     * @return A SassListItem where all occurrences of variable have been
     *         replaced by its value.
     */
    public SassListItem replaceVariable(VariableNode variable);

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

    // The following methods are used to make all values behave like a list.
    // The modification methods return the modified list so that it is possible
    // to add a value to a LexicalUnitImpl object and get a list as a result.
    public int size();

    public SassListItem get(int index);

    public Iterator<SassListItem> iterator();

    public SassList.Separator getSeparator();

    public LexicalUnitImpl getContainedValue();

    /**
     * Returns a list that contains all items of this list except those given in
     * the parameter items. If items or this is a single value instead of a
     * list, it is treated as if it were a list with one item.
     * 
     * @param items
     *            The items to be removed from this list.
     * @return A SassList containing the elements of this list except for those
     *         appearing in items.
     */
    public SassList removeAllItems(SassListItem items);

    /**
     * Adds an item to the end of this list. The parameter item can be a single
     * item or a list. If this element is a single value, it is treated like a
     * list containing the value.
     * 
     * Note that contrary to most list modification methods, item is not treated
     * like a list with one element if it is not a list. For example, adding the
     * single value 3 to (1, 2) yields the list (1, 2, 3) whereas adding the
     * list (3) to the list yields (1, 2, (3)), a nested list.
     * 
     * Does not actually modify this list but returns a new list with the
     * modification.
     * 
     * @param item
     *            The item to be added to this list.
     * @return A new list that is otherwise identical to this one but with the
     *         specified item added to the end.
     */
    public SassList addItem(SassListItem item);

    /**
     * Returns a list that is obtained from this list by adding all given items
     * to the end of this list. If items or this is a single value instead of a
     * list, it is treated as if it were a list with one item.
     * 
     * @param items
     *            The items to be added to this list.
     * @return A SassList containing the elements of this list and those
     *         appearing in items.
     */
    public SassList addAllItems(SassListItem items);

    /**
     * Checks whether all given items appear in this list. If items or this is a
     * single value instead of a list, it is treated as if it were a list with
     * one item.
     * 
     * @param items
     *            A set of items or a single item.
     * @return true, if this contains all elements of items. false, otherwise.
     */
    public boolean containsAllItems(SassListItem items);

    /**
     * Returns a flattened representation of this item. The flattened
     * representation of a single value or an empty list is the item itself.
     * 
     * For a non-empty list the definition of flatten is recursive. The
     * flattened representation of a list containing a single value is the
     * flattened representation of the value. For a list containing multiple
     * values, the flattened representation is obtained by replacing all
     * elements of the list by their flattened representations.
     * 
     * Examples of flattened representations: a) (1) -> 1 b) (1 (2) ((3)) ) ->
     * (1 2 3) c) (1, (2, 3), 4) -> (1, (2, 3), 4) (i.e., no change).
     * 
     * Note that the flattened representation of a list can be a single value
     * instead of a list, as in the example (a) above.
     * 
     * @return A flattened representation of this item.
     */
    public SassListItem flatten();
}
