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
package com.vaadin.sass.internal.parser;

import java.io.Serializable;
import java.util.Collection;

import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.tree.VariableNode;

public class BinaryBooleanExpression implements SassListItem, Serializable {

    private SassListItem left;
    private LexicalUnitImpl operator;
    private SassListItem right;

    public BinaryBooleanExpression(SassListItem left, LexicalUnitImpl operator,
            SassListItem right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public int getLineNumber() {
        return left.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return left.getColumnNumber();
    }

    public static SassListItem createExpression(SassListItem expr) {
        return expr.replaceChains();
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return left.containsArithmeticalOperator()
                || right.containsArithmeticalOperator();
    }

    public static BinaryBooleanExpression createExpression(SassListItem left,
            LexicalUnitImpl operator, SassListItem right) {
        return (new BinaryBooleanExpression(left, operator, right))
                .replaceChains();
    }

    @Override
    public BinaryBooleanExpression replaceFunctions() {
        return new BinaryBooleanExpression(left.replaceFunctions(), operator,
                right.replaceFunctions());
    }

    @Override
    public BinaryBooleanExpression replaceVariables(
            Collection<VariableNode> variables) {
        return new BinaryBooleanExpression(left.replaceVariables(variables),
                operator, right.replaceVariables(variables));
    }

    public boolean evaluate() {
        boolean value;
        switch (operator.getLexicalUnitType()) {
        case LexicalUnitImpl.SCSS_OPERATOR_NOT_EQUAL:
        case LexicalUnitImpl.SCSS_OPERATOR_EQUALS:
        case LexicalUnitImpl.SAC_OPERATOR_GT:
        case LexicalUnitImpl.SAC_OPERATOR_GE:
        case LexicalUnitImpl.SAC_OPERATOR_LT:
        case LexicalUnitImpl.SAC_OPERATOR_LE:
            value = evaluateComparison();
            break;
        default:
            throw new ParseException(
                    "Implementation error in the evaluation of a boolean expression: "
                            + this);

        }
        return value;
    }

    private boolean evaluateComparison() {
        boolean value;
        switch (operator.getLexicalUnitType()) {
        case LexicalUnitImpl.SAC_OPERATOR_LT:
        case LexicalUnitImpl.SAC_OPERATOR_GT:
        case LexicalUnitImpl.SAC_OPERATOR_LE:
        case LexicalUnitImpl.SAC_OPERATOR_GE:
            value = evaluateMagnitudeComparison();
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_EQUALS:
            value = left.printState().equals(right.printState());
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_NOT_EQUAL:
            value = !left.printState().equals(right.printState());
            break;
        default:
            throw new ParseException(
                    "Implementation error in BinaryBooleanExpression.evaluateComparison");
        }
        return value;
    }

    public boolean evaluateMagnitudeComparison() {
        boolean value;
        LexicalUnitImpl leftUnit = left.getContainedValue();
        LexicalUnitImpl rightUnit = right.getContainedValue();
        if (!leftUnit.isNumber() || !rightUnit.isNumber()) {
            throw new ParseException(
                    "The arguments of arithmetic expressions must be numbers: "
                            + this + ", in line " + getLineNumber()
                            + ", column " + getColumnNumber());
        }
        switch (operator.getLexicalUnitType()) {
        case LexicalUnitImpl.SAC_OPERATOR_LT:
            value = leftUnit.getFloatValue() < rightUnit.getFloatValue();
            break;
        case LexicalUnitImpl.SAC_OPERATOR_LE:
            value = leftUnit.getFloatValue() <= rightUnit.getFloatValue();
            break;
        case LexicalUnitImpl.SAC_OPERATOR_GT:
            value = leftUnit.getFloatValue() > rightUnit.getFloatValue();
            break;
        case LexicalUnitImpl.SAC_OPERATOR_GE:
            value = leftUnit.getFloatValue() >= rightUnit.getFloatValue();
            break;
        default:
            throw new ParseException(
                    "Implementation error in BinaryBooleanExpression.evaluateComparison");
        }
        return value;
    }

    public static boolean evaluate(final SassListItem expression) {
        if (expression instanceof BinaryBooleanExpression) {
            return ((BinaryBooleanExpression) expression).evaluate();
        }
        if (LexicalUnitImpl.checkLexicalUnitType(expression,
                LexicalUnitImpl.SCSS_NULL)) {
            return false;
        }
        return Boolean.valueOf(expression.printState());
    }

    @Override
    public BinaryBooleanExpression evaluateArithmeticExpressions() {
        return new BinaryBooleanExpression(
                left.evaluateArithmeticExpressions(), operator,
                right.evaluateArithmeticExpressions());
    }

    @Override
    public void updateUrl(String prefix) {
        left.updateUrl(prefix);
        right.updateUrl(prefix);
    }

    @Override
    public String printState() {
        return buildString(Node.PRINT_STRATEGY);
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        return strategy.build(left) + strategy.build(operator)
                + strategy.build(right);
    }

    @Override
    public String toString() {
        String result = "BinaryBooleanExpression[";
        result += buildString(Node.TO_STRING_STRATEGY);
        return result + "]";
    }

    @Override
    public String unquotedString() {
        return printState();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        throw new ParseException(
                "getContainedValue() cannot be used for a binary boolean expression, in line "
                        + getLineNumber() + ", column " + getColumnNumber());
    }

    @Override
    public BinaryBooleanExpression replaceChains() {
        SassListItem newLeft = left.replaceChains();
        SassListItem newRight = right.replaceChains();
        return new BinaryBooleanExpression(newLeft, operator, newRight);
    }

}
