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

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.visitor.VariableNodeHandler;

public class VariableNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 7003372557547748734L;

    private String name;
    private SassListItem expr = null;
    private boolean guarded;

    public VariableNode(String name, SassListItem expr, boolean guarded) {
        super();
        this.name = name;
        setExpr(expr);
        this.guarded = guarded;
    }

    public SassListItem getExpr() {
        return expr;
    }

    public void setExpr(SassListItem expr) {
        this.expr = expr;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isGuarded() {
        return guarded;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Variable node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    public void setGuarded(boolean guarded) {
        this.guarded = guarded;
    }

    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        expr = expr.replaceVariables(variables);
    }

    /**
     * Replaces each occurrence of ${name} in the parameter. Only evaluates the
     * variable if at least one such occurrence, and then only evaluates it
     * once.
     */
    public String replaceInterpolation(String s) {
        final String interpolation = "#{$" + getName() + "}";
        if (s.contains(interpolation)) {
            return s.replace(interpolation, getExpr().unquotedString());
        } else {
            return s;
        }
    }

    @Override
    public void traverse() {
        /*
         * "replaceVariables(ScssStylesheet.getVariables());" seems duplicated
         * and can be extracted out of if, but it is not.
         * containsArithmeticalOperator must be called before replaceVariables.
         * Because for the "/" operator, it needs to see if its predecessor or
         * successor is a Variable or not, to determine it is an arithmetic
         * operator.
         */
        boolean hasOperator = expr.containsArithmeticalOperator();
        replaceVariables(ScssStylesheet.getVariables());
        expr = expr.evaluateFunctionsAndExpressions(hasOperator);
        VariableNodeHandler.traverse(this);
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder builder = new StringBuilder("$");
        builder.append(name).append(": ").append(strategy.build(expr));
        return builder.toString();
    }

    @Override
    public VariableNode copy() {
        return new VariableNode(name, expr, guarded);
    }
}