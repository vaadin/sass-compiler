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

/**
 * Single CSS3 class selector such as ".abc".
 * 
 * See also {@link SimpleSelectorSequence} and {@link Selector}.
 */
public class ClassSelector extends SimpleSelector {

    private StringInterpolationSequence classValue;

    public ClassSelector(StringInterpolationSequence classValue) {
        this.classValue = classValue;
    }

    public StringInterpolationSequence getClassValue() {
        return classValue;
    }

    @Override
    public String toString() {
        return "." + getClassValue();
    }

    @Override
    public ClassSelector replaceVariables(ScssContext context) {
        return new ClassSelector(classValue.replaceVariables(context));
    }
}
