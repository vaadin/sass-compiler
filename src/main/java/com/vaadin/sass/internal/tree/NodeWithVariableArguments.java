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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.VariableArgumentList;

/**
 * NodeWithVariableArguments is used as a superclass for nodes that handle
 * argument lists with support for variable arguments. When variable arguments
 * are used, a NodeWithVariableArguments expands a list into separate arguments,
 * whereas a DefNode packs several arguments into a list.
 * NodeWithVariableArguments is currently used as a superclass for MixinNode and
 * FunctionNode. The corresponding definition nodes are subclasses of DefNode.
 * 
 * @author Vaadin
 * 
 */
public abstract class NodeWithVariableArguments extends Node implements
        IVariableNode {

    // these are the actual parameter values, not whether the definition node
    // uses varargs
    private VariableArgumentList arglist;
    private String name;

    public NodeWithVariableArguments(String name,
            Collection<VariableNode> args, boolean hasVariableArgs) {
        ArrayList<SassListItem> unnamed = new ArrayList<SassListItem>();
        ArrayList<VariableNode> named = new ArrayList<VariableNode>();
        if (args != null && !args.isEmpty()) {
            for (VariableNode arg : args) {
                if (arg.getName() == null) {
                    unnamed.add(arg.getExpr());
                } else {
                    named.add(arg.copy());
                }
            }
        }
        this.name = name;
        arglist = new VariableArgumentList(SassList.Separator.COMMA, unnamed,
                named, hasVariableArgs);
    }

    public NodeWithVariableArguments(String name, SassList parameterList) {
        this.name = name;
        if (parameterList instanceof VariableArgumentList) {
            arglist = (VariableArgumentList) parameterList;
        } else {
            arglist = new VariableArgumentList(parameterList, false);
        }
    }

    public boolean hasVariableArguments() {
        return arglist.hasVariableArguments();
    }

    public VariableArgumentList getArglist() {
        return arglist;
    }

    protected void expandVariableArguments() {
        arglist = arglist.expandVariableArguments();
    }

    public SassList.Separator getSeparator() {
        return arglist.getSeparator();
    }

    public String getName() {
        return name;
    }

    /**
     * Replace variable references with their values in the argument list and
     * name.
     */
    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        arglist = arglist.replaceVariables(variables);
        arglist = arglist.evaluateFunctionsAndExpressions(true);
    }

    @Override
    public void traverse() {
        // limit variable scope
        Map<String, VariableNode> variableScope = ScssStylesheet
                .openVariableScope();
        try {
            doTraverse();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        } finally {
            ScssStylesheet.closeVariableScope(variableScope);
        }
    }

    protected abstract void doTraverse() throws Exception;

}