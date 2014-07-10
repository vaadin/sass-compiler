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
 * StringItem is a wrapper class that allows strings to be stored in lists
 * taking SassListItems.
 * 
 * @author Vaadin
 * 
 */
public class StringItem implements SassListItem, Serializable {
    String value;

    public StringItem(String s) {
        value = s;
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public int getColumnNumber() {
        return 0;
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions(ScssContext context,
            boolean evaluateArithmetics) {
        return this;
    }

    @Override
    public SassListItem replaceVariables(ScssContext context) {
        // Handle a quoted string containing simple interpolation.
        if (value.length() > 1
                && ((value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') || (value
                        .charAt(0) == '\'' && value.charAt(value.length() - 1) == '\''))) {
            String stringValue = value;
            for (Variable var : context.getVariables()) {
                stringValue = var.replaceInterpolation(stringValue);
            }
            return new StringItem(stringValue);
        } else {
            return this;
        }
    }

    @Override
    public void updateUrl(String prefix) {
    }

    @Override
    public String printState() {
        return value;
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        return value;
    }

    @Override
    public String unquotedString() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        throw new ParseException(
                "getContainedValue() is not supported by StringValue.");
    }

    @Override
    public boolean containsVariable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringItem)) {
            return false;
        }
        StringItem other = (StringItem) o;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}