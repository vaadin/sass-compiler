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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.sass.internal.tree.VariableNode;

/**
 * A VariableArgumentList is used for packing arguments into a list. There can
 * be named and unnamed arguments, which are stored separately.
 * VariableArgumentLists are used as parameter lists of mixins and functions.
 */
public class VariableArgumentList extends SassList implements Serializable {
    private List<VariableNode> namedVariables = new ArrayList<VariableNode>();
    private boolean hasVariableArguments = false;

    public VariableArgumentList(SassList list, boolean hasVariableArguments) {
        super(list.getSeparator(), list.getItems());
        this.hasVariableArguments = hasVariableArguments;
    }

    public VariableArgumentList(Separator separator, List<SassListItem> list,
            List<VariableNode> named, boolean hasVariableArguments) {
        super(separator, list);
        namedVariables = named;
        this.hasVariableArguments = hasVariableArguments;
    }

    public List<VariableNode> getNamedVariables() {
        return Collections.unmodifiableList(namedVariables);
    }

    @Override
    public VariableArgumentList replaceVariables(
            Collection<VariableNode> variables) {
        // The actual replacing happens in LexicalUnitImpl, which also
        // implements SassListItem.
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : this) {
            list.add(item.replaceVariables(variables));
        }
        List<VariableNode> named = new ArrayList<VariableNode>();
        for (VariableNode node : namedVariables) {
            named.add(new VariableNode(node.getName(), node.getExpr()
                    .replaceVariables(variables), node.isGuarded()));
        }
        return new VariableArgumentList(getSeparator(), list, named,
                hasVariableArguments);
    }

    @Override
    public VariableArgumentList evaluateFunctionsAndExpressions(
            boolean evaluateArithmetics) {
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : this) {
            list.add(item.evaluateFunctionsAndExpressions(evaluateArithmetics));
        }
        List<VariableNode> named = new ArrayList<VariableNode>();
        for (VariableNode node : namedVariables) {
            named.add(new VariableNode(node.getName(), node.getExpr()
                    .evaluateFunctionsAndExpressions(evaluateArithmetics), node
                    .isGuarded()));
        }
        return new VariableArgumentList(getSeparator(), list, named,
                hasVariableArguments);
    }

    public VariableArgumentList expandVariableArguments() {
        /*
         * If there are variable arguments, the last argument is expanded into
         * separate arguments.
         * 
         * Note that the separator character should be preserved when an
         * 
         * @include expands variables into separate arguments and the
         * corresponding @mixin packs them again into a list. Some cases have
         * not yet been verified to work as they should.
         * 
         * To illustrate the cases, suppose that there is a mixin with variable
         * arguments @mixin foo($a1, $a2, ..., $ak...). That is used by an
         * include with variable arguments: @include foo($b1, $b2, ..., $bl...).
         * Then the include will expand the argument bl into separate arguments,
         * if bl is a list. The mixin will pack possibly several arguments into
         * a list ak. The cases are then
         * 
         * 1) k = l. Then ak will be a list equal to bl. To retain the
         * separator, it needs to be taken from the list bl.
         * 
         * 2) l < k. Now ak will be a sublist of bl, the first elements of bl
         * will be used for the parameters a(l+1), ..., a(k-1). If a list should
         * retain the separator, its sublist should also have the same
         * separator.
         * 
         * 3) l > k, the uncertain and only partially verified case. Now, ak
         * will be a list that contains the parameters b(k+1), ..., b(l-1) and
         * the contents of the list bl. Using the separator of the list bl means
         * that the same separator will also separate the parameters b(k+1)...
         * from each other in the list ak. That is the approach adopted here,
         * but it is only based on a limited amount of testing.
         * 
         * The separator of a one-element list is considered to be a comma here.
         * 
         * 
         * Also note that the named and unnamed parameters are stored in two
         * separate lists. The named parameters packed into a variable argument
         * list cannot be accessed inside the mixin. While this is unexpected,
         * it seems to be the desired behavior, although only a limited amount
         * of testing has been done to verify this.
         */
        if (hasVariableArguments()) {
            List<SassListItem> unnamedArgs = new ArrayList<SassListItem>(
                    getItems());
            List<VariableNode> namedArgs = new ArrayList<VariableNode>(
                    getNamedVariables());
            SassListItem last = unnamedArgs.get(unnamedArgs.size() - 1);
            if (last instanceof SassList) {
                unnamedArgs.remove(unnamedArgs.size() - 1);
                SassList lastList = (SassList) last;
                for (SassListItem item : lastList) {
                    unnamedArgs.add(item);
                }
            }
            // Append any remaining variable name-value pairs to the argument
            // list
            if (last instanceof VariableArgumentList) {
                for (VariableNode namedNode : ((VariableArgumentList) last)
                        .getNamedVariables()) {
                    namedArgs.add(namedNode.copy());
                }
            }
            return new VariableArgumentList(getSeparator(last), unnamedArgs,
                    namedArgs, false);
        }
        return this;
    }

    protected Separator getSeparator(SassListItem expr) {
        if (expr instanceof SassList) {
            SassList lastList = (SassList) expr;
            if (lastList.size() > 1) {
                return lastList.getSeparator();
            }
        }
        return SassList.Separator.COMMA;
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }

    public List<VariableNode> getVariableNodeList() {
        ArrayList<VariableNode> nodes = new ArrayList<VariableNode>();
        for (SassListItem item : getItems()) {
            nodes.add(new VariableNode(null, item, false));
        }
        nodes.addAll(getNamedVariables());
        return nodes;
    }

}
