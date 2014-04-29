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

package com.vaadin.sass.internal.tree;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.visitor.MixinNodeHandler;

public class MixinNode extends NodeWithVariableArguments {
    private static final long serialVersionUID = 4725008226813110658L;

    public MixinNode(String name) {
        this(name, new ArrayList<VariableNode>(), false);
    }

    public MixinNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        super(name, args, hasVariableArgs);
    }

    @Override
    public String printState() {
        return "name: " + getName() + " args: " + getArglist();
    }

    @Override
    public String toString() {
        return "Mixin node [" + printState() + "]";
    }

    protected void replaceVariablesForChildren() {
        for (Node child : getChildren()) {
            if (child instanceof IVariableNode) {
                ((IVariableNode) child).replaceVariables(ScssStylesheet
                        .getVariables());
            }
        }
    }

    @Override
    public void doTraverse() throws Exception {
        replaceVariables(ScssStylesheet.getVariables());
        if (!getArglist().isEmpty()) {
            updateSeparator(
                    getArglist().get(getArglist().size() - 1).getExpr(),
                    hasVariableArguments());
        }
        setArglist(expandVariableArguments(getArglist(), hasVariableArguments()));
        replaceVariablesForChildren();
        MixinNodeHandler.traverse(this);
    }

}
