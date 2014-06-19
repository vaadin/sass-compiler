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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.SassList;

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
    private ActualArgumentList arglist;
    private String name;

    public NodeWithVariableArguments(String name,
            Collection<VariableNode> args, boolean hasVariableArgs) {
        this.name = name;
        arglist = new ActualArgumentList(SassList.Separator.COMMA, args,
                hasVariableArgs);
    }

    public NodeWithVariableArguments(String name,
            ActualArgumentList parameterList) {
        this.name = name;
        arglist = parameterList;
    }

    public boolean hasVariableArguments() {
        return arglist.hasVariableArguments();
    }

    public ActualArgumentList getArglist() {
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
    public void replaceVariables() {
        arglist = arglist.replaceVariables();
        arglist = arglist.evaluateFunctionsAndExpressions(true);
    }

    @Override
    public void traverse() {
        // limit variable scope
        ScssStylesheet.openVariableScope();
        try {
            doTraverse();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        } finally {
            ScssStylesheet.closeVariableScope();
        }
    }

    protected abstract void doTraverse() throws Exception;

}