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

import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.VariableNode;

/**
 * An ArgumentList is used for packing arguments into a list. There can be named
 * and unnamed arguments, which are stored separately. ArgumentList is used for
 * representing the arguments of a function or a mixin. ArgumentList is also
 * used in ActualArgumentList to represent the unnamed (positional) and named
 * parameters of an @include or a function call.
 */
public class ArgumentList extends SassList implements Serializable {
    private List<VariableNode> namedVariables = new ArrayList<VariableNode>();

    public ArgumentList(SassList list) {
        super(list.getSeparator(), list.getItems());
    }

    public ArgumentList(Separator separator, List<SassListItem> list,
            List<VariableNode> named) {
        super(separator, list);
        namedVariables = named;
    }

    public ArgumentList(Separator sep, SassListItem... items) {
        super(sep, items);
    }

    public ArgumentList(Separator separator, List<SassListItem> newParamValues) {
        super(separator, newParamValues);
    }

    public List<VariableNode> getNamedVariables() {
        return Collections.unmodifiableList(namedVariables);
    }

    @Override
    public ArgumentList replaceVariables(Collection<VariableNode> variables) {
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
        return new ArgumentList(getSeparator(), list, named);
    }

    @Override
    public ArgumentList evaluateFunctionsAndExpressions(
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
        return new ArgumentList(getSeparator(), list, named);
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

    @Override
    public String toString() {
        String unnamed = buildString(Node.PRINT_STRATEGY);
        String named = namedAsString();
        if (unnamed.length() > 0 && named.length() > 0) {
            unnamed += ", ";
        }
        return "ArgumentList [" + unnamed + named + "]";
    }

    private String namedAsString() {
        String result = "";
        for (int i = 0; i < namedVariables.size(); i++) {
            VariableNode named = namedVariables.get(i);
            String contents = named.getExpr() == null ? "null" : named
                    .getExpr().buildString(Node.PRINT_STRATEGY);
            result += "$" + named.getName() + ": " + contents;
            if (i < namedVariables.size() - 1) {
                result += ", ";
            }
        }
        return result;
    }

}