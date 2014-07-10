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
package com.vaadin.sass.internal.selector;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.StringInterpolationSequence;
import com.vaadin.sass.internal.parser.Variable;

/**
 * Single CSS3 pseudo-class selector such as ":active" or ":nth-child(2)".
 * 
 * See also {@link SimpleSelectorSequence} and {@link Selector}.
 */
public class PseudoClassSelector extends SimpleSelector {
    private StringInterpolationSequence pseudoClass;
    private String argument = null;

    public PseudoClassSelector(StringInterpolationSequence pseudoClass) {
        this(pseudoClass, null);
    }

    public PseudoClassSelector(StringInterpolationSequence pseudoClass,
            String argument) {
        this.pseudoClass = pseudoClass;
        this.argument = argument;
    }

    public StringInterpolationSequence getClassValue() {
        return pseudoClass;
    }

    @Override
    public String toString() {
        if (argument == null) {
            return ":" + getClassValue();
        } else {
            return ":" + getClassValue() + "(" + argument + ")";
        }
    }

    @Override
    public PseudoClassSelector replaceVariables(ScssContext context) {
        if (argument == null) {
            return new PseudoClassSelector(
                    pseudoClass.replaceVariables(context));
        } else {
            return new PseudoClassSelector(
                    pseudoClass.replaceVariables(context),
                    replaceInterpolation(context, argument));
        }
    }

    private String replaceInterpolation(ScssContext context, String value) {
        String result = value;
        for (Variable var : context.getVariables()) {
            result = var.replaceInterpolation(result);
        }
        return result;
    }
}