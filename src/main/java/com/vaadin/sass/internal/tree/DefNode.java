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

/**
 * DefNode defines the shared functionality of mixin and function definition
 * nodes. This includes the handling of parameter lists.
 * 
 * @author Vaadin
 * 
 */
public abstract class DefNode extends Node implements IVariableNode {
    private String name;
    private ArrayList<VariableNode> arglist;
    private boolean hasVariableArguments = false;

    public DefNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        super();
        this.name = name;
        arglist = (args != null) ? new ArrayList<VariableNode>(args)
                : new ArrayList<VariableNode>();
        hasVariableArguments = hasVariableArgs;
    }

    public String getName() {
        return name;
    }

    public ArrayList<VariableNode> getArglist() {
        return arglist;
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }

    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        for (final VariableNode var : variables) {
            for (final VariableNode arg : new ArrayList<VariableNode>(arglist)) {
                if (arg.getName().equals(var.getName())
                        && arg.getExpr() == null) {
                    arglist.set(arglist.indexOf(arg), var.copy());
                }
            }
        }
    }

    @Override
    public void traverse() {
        if (!arglist.isEmpty()) {
            for (final VariableNode arg : arglist) {
                if (arg.getExpr() != null) {
                    ScssStylesheet.addVariable(arg);
                }
            }
        }
    }
}
