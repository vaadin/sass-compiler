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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;

/**
 * Interpolation represents a single instance of interpolation. The string
 * representation of an Interpolation object is "#{<expression>}" (without
 * quotes) where <expression> is the textual representation of the expression of
 * the interpolation.
 * 
 * An Interpolation object is evaluated by calling replaceInterpolation(). The
 * result of the evaluation is the value of the expression, a SassListItem. If
 * the result is a single textual value with quotation marks, the quotation
 * marks are removed from the result. Variables should be replaced with their
 * values and functions and expressions evaluated before calling
 * replaceInterpolation().
 * 
 * @author Vaadin
 * 
 */
public class Interpolation implements SassListItem, Serializable {
    private int lineNumber;
    private int columnNumber;
    private SassListItem expression;
    private boolean evaluateArithmetics;

    public Interpolation(SassListItem expression, int line, int column) {
        this.expression = expression;
        lineNumber = line;
        columnNumber = column;
        evaluateArithmetics = expression.containsArithmeticalOperator();
    }

    private Interpolation(SassListItem expression, int line, int column,
            boolean evaluateArithmetics) {
        this.expression = expression;
        lineNumber = line;
        columnNumber = column;
        this.evaluateArithmetics = evaluateArithmetics;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return evaluateArithmetics;
    }

    @Override
    public Interpolation evaluateFunctionsAndExpressions(ScssContext context,
            boolean evaluateArithmetics) {
        // Interpolation ignores evaluateArithmetics - whether there are
        // arithmetic operations has been determined in the constructor.
        return new Interpolation(expression.evaluateFunctionsAndExpressions(
                context, this.evaluateArithmetics), getLineNumber(),
                getColumnNumber());
    }

    @Override
    public Interpolation replaceVariables(ScssContext context) {
        return new Interpolation(expression.replaceVariables(context),
                getLineNumber(), getColumnNumber(), evaluateArithmetics);
    }

    @Override
    public void updateUrl(String prefix) {
        expression.updateUrl(prefix);
    }

    @Override
    public String printState() {
        return "#{" + expression.printState() + "}";
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        return printState();
    }

    @Override
    public String toString() {
        return printState();
    }

    @Override
    public String unquotedString() {
        throw new ParseException(
                "unquotedString() is not supported for interpolation");
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        throw new ParseException(
                "getContainedValue() is not supported for interpolation");
    }

    /**
     * Returns the value of the expression, with quotation marks removed if the
     * value is a single textual value. Before this method the methods
     * replaceVariables and evaluateFunctionsAndExpressions should be called to
     * determine the evaluated value of the expression.
     * 
     * @return The value of the evaluated expression, a SassListItem.
     */
    public SassListItem replaceInterpolation() {
        if (expression instanceof LexicalUnitImpl) {
            String unquotedString = expression.unquotedString();
            return new LexicalUnitImpl(getLineNumber(), getLineNumber(),
                    LexicalUnitImpl.SAC_IDENT, unquotedString);
        } else {
            return expression;
        }
    }

    @Override
    public boolean containsVariable() {
        return expression.containsVariable();
    }

    /**
     * Returns true if this and o are equal interpolation objects. Two
     * interpolation objects are considered to be equal if their expressions are
     * equal.
     * 
     * For comparing the results of the interpolation instead of the
     * expressions, the objects should be expanded to SassListitems using
     * replaceInterpolation() after replacing all variables occurring in the
     * interpolation objects.
     * 
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Interpolation)) {
            return false;
        }
        Interpolation other = (Interpolation) o;
        return expression.equals(other.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}