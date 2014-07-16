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
import java.util.Collections;

import com.vaadin.sass.internal.Definition;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.visitor.VariableNodeHandler;

public class VariableNode extends Node implements Definition, IVariableNode {
    private static final long serialVersionUID = 7003372557547748734L;

    private final Variable variable;

    public VariableNode(String name, SassListItem expr, boolean guarded) {
        super();
        variable = new Variable(name, expr, guarded);
    }

    public SassListItem getExpr() {
        return variable.getExpr();
    }

    public void setExpr(SassListItem expr) {
        variable.setExpr(expr);
    }

    public String getName() {
        return variable.getName();
    }

    public boolean isGuarded() {
        return variable.isGuarded();
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Variable node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    @Override
    public void replaceVariables(ScssContext context) {
        variable.setExpr(variable.getExpr().replaceVariables(context));
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        /*
         * containsArithmeticalOperator() must be called before
         * replaceVariables. Because for the "/" operator, it needs to see if
         * its predecessor or successor is a Variable or not, to determine it is
         * an arithmetic operator.
         */
        boolean hasOperator = variable.getExpr().containsArithmeticalOperator();
        replaceVariables(context);
        variable.setExpr(variable.getExpr().evaluateFunctionsAndExpressions(
                context, hasOperator));
        VariableNodeHandler.traverse(context, this);
        return Collections.emptyList();
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder builder = new StringBuilder("$");
        builder.append(getName()).append(": ")
                .append(strategy.build(getExpr()));
        return builder.toString();
    }

    @Override
    public VariableNode copy() {
        return new VariableNode(getName(), getExpr(), isGuarded());
    }
}
