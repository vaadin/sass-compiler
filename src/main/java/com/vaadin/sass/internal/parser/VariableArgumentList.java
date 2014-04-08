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

import com.vaadin.sass.internal.tree.VariableNode;

/**
 * A VariableArgumentList is used for packing arguments into a list. There can
 * be named and unnamed arguments, which are stored separately.
 * VariableArgumentLists are used as parameter lists of mixins and functions.
 */
public class VariableArgumentList extends SassList implements Serializable {
    private ArrayList<VariableNode> namedVariables = new ArrayList<VariableNode>();

    public VariableArgumentList(SassList.Separator sep) {
        super(sep);
    }

    public ArrayList<VariableNode> getNamedVariables() {
        return namedVariables;
    }

    public void addNamed(String name, SassListItem sassListItem) {
        VariableNode node = new VariableNode(name, sassListItem, false);
        namedVariables.add(node);
    }

}
