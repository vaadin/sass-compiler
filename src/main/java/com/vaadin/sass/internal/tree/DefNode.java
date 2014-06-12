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

import java.util.Collection;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;

/**
 * DefNode defines the shared functionality of mixin and function definition
 * nodes. This includes the handling of parameter lists.
 * 
 * @author Vaadin
 * 
 */
public abstract class DefNode extends Node implements IVariableNode {
    private String name;
    private FormalArgumentList arglist;

    public DefNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        super();
        this.name = name;
        arglist = new FormalArgumentList(args, hasVariableArgs);
    }

    public String getName() {
        return name;
    }

    public FormalArgumentList getArglist() {
        return arglist;
    }

    public boolean hasVariableArguments() {
        return arglist.hasVariableArguments();
    }

    @Override
    public void replaceVariables() {
        arglist = arglist.replaceVariables();
    }

    public void replacePossibleArguments(ActualArgumentList actualArgumentList) {
        // TODO instead of modifying def, return a VariableArgumentList?
        arglist = arglist.replaceFormalArguments(actualArgumentList, true);
    }

    @Override
    public void traverse() {
        // this is not used for definition nodes
    }

}
