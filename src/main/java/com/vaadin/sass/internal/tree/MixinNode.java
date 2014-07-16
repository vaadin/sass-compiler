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
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.visitor.MixinNodeHandler;

/**
 * Node for including a Mixin.
 * 
 * MixinNode handles argument lists with support for variable arguments. When
 * variable arguments are used, a MixinNode expands a list into separate
 * arguments, whereas a DefNode packs several arguments into a list. The
 * corresponding definition node is {@link MixinDefNode}.
 * 
 * @author Vaadin
 */
public class MixinNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 4725008226813110658L;

    // these are the actual parameter values, not whether the definition node
    // uses varargs
    private ActualArgumentList arglist;
    private String name;

    public MixinNode(String name) {
        this(name, new ArrayList<Variable>(), false);
    }

    public MixinNode(String name, Collection<Variable> args,
            boolean hasVariableArgs) {
        this.name = name;
        arglist = new ActualArgumentList(SassList.Separator.COMMA, args,
                hasVariableArgs);
    }

    private MixinNode(MixinNode nodeToCopy) {
        super(nodeToCopy);
        arglist = nodeToCopy.arglist;
        name = nodeToCopy.name;
    }

    public ActualArgumentList getArglist() {
        return arglist;
    }

    protected void expandVariableArguments() {
        arglist = arglist.expandVariableArguments();
    }

    public String getName() {
        return name;
    }

    /**
     * Replace variable references with their values in the argument list and
     * name.
     */
    @Override
    public void replaceVariables(ScssContext context) {
        arglist = arglist.replaceVariables(context);
        arglist = arglist.evaluateFunctionsAndExpressions(context, true);
    }

    @Override
    public String printState() {
        return "name: " + getName() + " args: " + getArglist();
    }

    @Override
    public String toString() {
        return "Mixin node [" + printState() + "]";
    }

    protected void replaceVariablesForChildren(ScssContext context) {
        for (Node child : getChildren()) {
            if (child instanceof IVariableNode) {
                ((IVariableNode) child).replaceVariables(context);
            }
        }
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        try {
            replaceVariables(context);
            expandVariableArguments();
            // for the content block, use the scope where it is defined
            // (consistent with sass-lang)
            replaceVariablesForChildren(context);
            // inner scope is managed by MixinNodeHandler
            return MixinNodeHandler.traverse(context, this);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            // TODO is ignoring this exception appropriate?
            return Collections.emptyList();
        }
    }

    @Override
    public MixinNode copy() {
        return new MixinNode(this);
    }

}
