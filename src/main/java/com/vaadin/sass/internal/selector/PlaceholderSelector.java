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
 * Simple placeholder selector of the form "%abc". Placeholder selectors are not
 * output as-is but only when used via "@extend".
 */
public class PlaceholderSelector extends SimpleSelector {
    private StringInterpolationSequence value;

    public PlaceholderSelector(StringInterpolationSequence value) {
        this.value = value;
    }

    public StringInterpolationSequence getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "%" + getValue();
    }

    @Override
    public PlaceholderSelector replaceVariables(ScssContext context) {
        return new PlaceholderSelector(value.replaceVariables(context));
    }
}
