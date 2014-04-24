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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.sass.internal.expression.ArithmeticExpressionEvaluator;
import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.tree.VariableNode;

/**
 * SassExpressions are used for representing and evaluating arithmetic
 * expressions.
 * 
 * @author Vaadin
 * 
 */
public class SassExpression implements SassListItem {

    private List<SassListItem> items;
    private int line = 0;
    private int column = 0;

    public SassExpression(LexicalUnitImpl chain) {
        if (chain != null) {
            line = chain.getLineNumber();
            column = chain.getColumnNumber();
        }
        items = new ArrayList<SassListItem>();
        while (chain != null) {
            items.add(chain.copy());
            chain = chain.getNextLexicalUnit();
        }
    }

    protected SassExpression(List<SassListItem> items) {
        if (!items.isEmpty()) {
            line = items.get(0).getLineNumber();
            column = items.get(0).getColumnNumber();
        }
        this.items = items;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    public boolean containsArithmeticalOperator() {
        for (SassListItem item : items) {
            if (item.containsArithmeticalOperator()) {
                return true;
            }
        }
        if (items.size() < 3) {
            return false;
        }
        for (int i = 0; i < items.size() - 2; ++i) {
            SassListItem previous = items.get(i);
            SassListItem current = items.get(i + 1);
            SassListItem next = items.get(i + 2);
            if (!(current instanceof LexicalUnitImpl)) {
                continue;
            }
            short currentType = ((LexicalUnitImpl) current)
                    .getLexicalUnitType();
            if (currentType == BinaryOperator.DIV.type) {
                /*
                 * '/' is treated as an arithmetical operator when one of its
                 * operands is Variable, or there is another binary operator.
                 * Otherwise, '/' is treated as a CSS operator.
                 */
                if (isVariable(previous) || isVariable(next)) {
                    return true;
                }
            } else if (isOperator(currentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperator(short type) {
        for (BinaryOperator operator : BinaryOperator.values()) {
            if (type == operator.type) {
                return true;
            }
        }
        return false;
    }

    private boolean isVariable(SassListItem item) {
        return item instanceof LexicalUnitImpl
                && ((LexicalUnitImpl) item).getLexicalUnitType() == LexicalUnitImpl.SCSS_VARIABLE;
    }

    @Override
    public SassListItem evaluateArithmeticExpressions() {
        if (items.size() == 0) {
            return this;
        }
        return ArithmeticExpressionEvaluator.get().evaluate(items);
    }

    @Override
    public SassExpression replaceFunctions() {
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : items) {
            list.add(item.replaceFunctions());
        }
        return new SassExpression(list);
    }

    @Override
    public SassExpression replaceVariables(Collection<VariableNode> variables) {
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : items) {
            list.add(item.replaceVariables(variables));
        }
        return new SassExpression(list);
    }

    @Override
    public void updateUrl(String prefix) {
        for (SassListItem item : items) {
            item.updateUrl(prefix);
        }
    }

    @Override
    public String printState() {
        return buildString(Node.PRINT_STRATEGY);
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        String result = "";
        Iterator<SassListItem> it = items.iterator();
        while (it.hasNext()) {
            SassListItem item = it.next();
            result += strategy.build(item);
            if (it.hasNext()) {
                result += " ";
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "SassExpression[";
        result += buildString(Node.TO_STRING_STRATEGY);
        return result + "]";
    }

    @Override
    public String unquotedString() {
        if (items.size() == 1 && items.get(0) instanceof LexicalUnitImpl) {
            return ((LexicalUnitImpl) items.get(0)).printState();
        }
        return printState();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        if (items.size() != 1 || !(items.get(0) instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "getContainedValue() can only be used for an expression that contains one simple value. Actual value: "
                            + toString());
        }
        return (LexicalUnitImpl) items.get(0);
    }

    @Override
    public SassListItem replaceChains() {
        return this;
    }

}
