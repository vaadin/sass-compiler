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
 * Single CSS3 type selector such as "p" or "div".
 * 
 * See also {@link SimpleSelectorSequence} and {@link Selector}.
 */
public class TypeSelector extends SimpleSelector {
    private StringInterpolationSequence localName;

    public TypeSelector(StringInterpolationSequence value) {
        localName = value;
    }

    public StringInterpolationSequence getValue() {
        return localName;
    }

    @Override
    public String toString() {
        return localName.toString();
    }

    @Override
    public TypeSelector replaceVariables(ScssContext context) {
        return new TypeSelector(localName.replaceVariables(context));
    }
}
