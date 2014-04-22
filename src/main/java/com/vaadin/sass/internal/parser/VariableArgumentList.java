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
import com.vaadin.sass.internal.util.DeepCopy;

/**
 * A VariableArgumentList is used for packing arguments into a list. There can
 * be named and unnamed arguments, which are stored separately.
 * VariableArgumentLists are used as parameter lists of mixins and functions.
 */
public class VariableArgumentList extends SassList implements Serializable {
    private List<VariableNode> namedVariables = new ArrayList<VariableNode>();

    public VariableArgumentList(SassList.Separator sep) {
        super(sep);
    }

    public VariableArgumentList(Separator separator, List<SassListItem> list,
            List<VariableNode> named) {
        super(separator, list);
        namedVariables = named;
    }

    public List<VariableNode> getNamedVariables() {
        return Collections.unmodifiableList(namedVariables);
    }

    @Override
    public VariableArgumentList replaceVariables(
            Collection<VariableNode> variables) {
        // TODO this can be removed once LUI is immutable
        SassList copy = (SassList) DeepCopy.copy(this);
        // The actual replacing happens in LexicalUnitImpl, which also
        // implements SassListItem.
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : copy) {
            list.add(item.replaceVariables(variables));
        }
        List<VariableNode> named = new ArrayList<VariableNode>();
        for (VariableNode node : namedVariables) {
            named.add(new VariableNode(node.getName(), node.getExpr()
                    .replaceVariables(variables), node.isGuarded()));
        }
        return new VariableArgumentList(getSeparator(), list, named);
    }

    @Override
    public VariableArgumentList replaceFunctions() { // handle the VariableNodes
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : this) {
            list.add(item.replaceFunctions());
        }
        List<VariableNode> named = new ArrayList<VariableNode>();
        for (VariableNode node : namedVariables) {
            named.add(new VariableNode(node.getName(), node.getExpr()
                    .replaceFunctions(), node.isGuarded()));
        }
        return new VariableArgumentList(getSeparator(), list, named);

    }

    @Override
    public SassListItem flatten() {
        throw new UnsupportedOperationException();
    }
}
