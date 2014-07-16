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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.StringInterpolationSequence;

public class RuleNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 6653493127869037022L;

    StringInterpolationSequence variable;
    SassListItem value = null;
    String comment;
    private boolean important;

    public RuleNode(StringInterpolationSequence variable, SassListItem value,
            boolean important, String comment) {
        this.variable = variable;
        setValue(value);
        this.important = important;
        this.comment = comment;
    }

    private RuleNode(RuleNode nodeToCopy) {
        super(nodeToCopy);
        variable = nodeToCopy.variable;
        value = nodeToCopy.value;
        comment = nodeToCopy.comment;
        important = nodeToCopy.important;
    }

    public StringInterpolationSequence getVariable() {
        return variable;
    }

    public void setVariable(StringInterpolationSequence variable) {
        this.variable = variable;
    }

    public SassListItem getValue() {
        return value;
    }

    private void setValue(SassListItem value) {
        this.value = value;
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
    public void replaceVariables(ScssContext context) {
        variable = variable.replaceVariables(context);
        value = value.replaceVariables(context);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        /*
         * containsArithmeticalOperator() must be called before
         * replaceVariables. Because for the "/" operator, it needs to see if
         * its predecessor or successor is a Variable or not, to determine it is
         * an arithmetic operator.
         */
        boolean hasOperators = value.containsArithmeticalOperator();
        replaceVariables(context);
        value = value.evaluateFunctionsAndExpressions(context, hasOperators);
        return Collections.singleton((Node) this);
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

    @Override
    public RuleNode copy() {
        return new RuleNode(this);
    }
}
