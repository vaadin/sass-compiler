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

package com.vaadin.sass.internal.expression;

import java.util.List;
import java.util.Stack;

import com.vaadin.sass.internal.expression.exception.ArithmeticException;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SCSSLexicalUnit;
import com.vaadin.sass.internal.parser.SassExpression;
import com.vaadin.sass.internal.parser.SassListItem;

public class ArithmeticExpressionEvaluator {
    private static ArithmeticExpressionEvaluator instance;

    public static ArithmeticExpressionEvaluator get() {
        if (instance == null) {
            instance = new ArithmeticExpressionEvaluator();
        }
        return instance;
    }

    private void createNewOperand(BinaryOperator operator,
            Stack<Object> operands) {
        Object rightOperand = operands.pop();
        operands.push(new BinaryExpression(operands.pop(), operator,
                rightOperand));
    }

    private Object createExpression(List<SassListItem> terms) {
        LexicalUnitImpl current = null;
        boolean afterOperand = false;
        Stack<Object> operands = new Stack<Object>();
        Stack<Object> operators = new Stack<Object>();
        inputTermLoop: for (int i = 0; i < terms.size(); ++i) {
            if (!(terms.get(i) instanceof LexicalUnitImpl)) {
                throw new ParseException("Illegal value in expression",
                        terms.get(i));
            }
            current = (LexicalUnitImpl) terms.get(i);
            if (SassExpression.isWhitespace(current)) {
                continue;
            }
            if (afterOperand) {
                if (current.getLexicalUnitType() == SCSSLexicalUnit.SCSS_OPERATOR_RIGHT_PAREN) {
                    Object operator = null;
                    while (!operators.isEmpty()
                            && ((operator = operators.pop()) != Parentheses.LEFT)) {
                        createNewOperand((BinaryOperator) operator, operands);
                    }
                    continue;
                }
                afterOperand = false;
                for (BinaryOperator operator : BinaryOperator.values()) {
                    if (current.getLexicalUnitType() == operator.type) {
                        while (!operators.isEmpty()
                                && (operators.peek() != Parentheses.LEFT)
                                && (((BinaryOperator) operators.peek()).precedence >= operator.precedence)) {
                            createNewOperand((BinaryOperator) operators.pop(),
                                    operands);
                        }
                        operators.push(operator);

                        continue inputTermLoop;
                    }
                }
                throw new ArithmeticException("Illegal arithmetic expression",
                        current);
            }
            if (current.getLexicalUnitType() == SCSSLexicalUnit.SCSS_OPERATOR_LEFT_PAREN) {
                operators.push(Parentheses.LEFT);
                continue;
            }
            afterOperand = true;

            operands.push(current);
        }

        while (!operators.isEmpty()) {
            Object operator = operators.pop();
            if (operator == Parentheses.LEFT) {
                throw new ArithmeticException("Unexpected \"(\" found", current);
            }
            createNewOperand((BinaryOperator) operator, operands);
        }
        Object expression = operands.pop();
        if (!operands.isEmpty()) {
            LexicalUnitImpl operand = (LexicalUnitImpl) operands.peek();
            throw new ArithmeticException("Unexpected operand "
                    + operand.toString() + " found", current);
        }
        return expression;
    }

    public LexicalUnitImpl evaluate(List<SassListItem> terms) {
        Object result = ArithmeticExpressionEvaluator.get().createExpression(
                terms);
        if (result instanceof BinaryExpression) {
            return ((BinaryExpression) result).eval();
        }
        // createExpression returns either a BinaryExpression or a
        // LexicalUnitImpl
        return (LexicalUnitImpl) terms;
    }
}