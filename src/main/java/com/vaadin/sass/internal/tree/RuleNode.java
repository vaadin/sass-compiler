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
import java.util.regex.Pattern;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassListItem;

public class RuleNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 6653493127869037022L;

    String variable;
    SassListItem value = null;
    String comment;
    private boolean important;

    public RuleNode(String variable, SassListItem value, boolean important,
            String comment) {
        this.variable = variable;
        setValue(value);
        this.important = important;
        this.comment = comment;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public SassListItem getValue() {
        return value;
    }

    private void setValue(SassListItem value) {
        if (value != null) {
            this.value = value.replaceChains();
        } else {
            this.value = null;
        }
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Rule node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void replaceVariables(ArrayList<VariableNode> variables) {
        for (final VariableNode node : variables) {

            String interpolation = "#{$" + node.getName() + "}";

            if (variable != null && variable.contains(interpolation)) {
                variable = variable.replaceAll(Pattern.quote(interpolation),
                        node.getExpr().unquotedString());
            }
        }
        value = value.replaceVariables(variables);
        value = value.replaceFunctions();
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
        if (value.containsArithmeticalOperator()) {
            replaceVariables(ScssStylesheet.getVariables());
            value = value.evaluateArithmeticExpressions();
        } else {
            replaceVariables(ScssStylesheet.getVariables());
        }
    }

    private String buildString(BuildStringStrategy strategy) {
        String stringValue = strategy.build(value)
                + (important ? " !important" : "");
        StringBuilder builder = new StringBuilder();
        if (!"".equals(stringValue.trim())) {
            builder.append(variable);
            builder.append(": ");
            builder.append(stringValue);
            builder.append(';');
        }

        if (comment != null) {
            builder.append(comment);
        }
        return builder.toString();
    }
}