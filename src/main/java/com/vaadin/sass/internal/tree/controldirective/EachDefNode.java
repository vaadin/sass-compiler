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

package com.vaadin.sass.internal.tree.controldirective;

import java.util.Collection;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.visitor.EachNodeHandler;

public class EachDefNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 7943948981204906221L;

    private String var;
    private SassListItem list;

    public EachDefNode(String var, SassListItem list) {
        super();
        this.var = var;
        this.list = list;
    }

    private EachDefNode(EachDefNode nodeToCopy) {
        super(nodeToCopy);
        var = nodeToCopy.var;
        list = nodeToCopy.list;
    }

    public SassList getVariables() {
        if (list instanceof SassList) {
            return (SassList) list;
        } else {
            return new SassList(list);
        }
    }

    public String getVariableName() {
        return var;
    }

    @Override
    public String toString() {
        return "Each Definition Node: {variable : " + var + ", "
                + "children : " + list + "}";
    }

    @Override
    public void replaceVariables(ScssContext context) {
        list = list.replaceVariables(context);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        replaceVariables(context);
        return EachNodeHandler.traverse(context, this);
    }

    @Override
    public EachDefNode copy() {
        return new EachDefNode(this);
    }
}
