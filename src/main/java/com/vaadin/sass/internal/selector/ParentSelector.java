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
 * Single SCSS parent selector "&".
 * 
 * The parent selector is not passed to CSS as-is but is replaced with the
 * selectors from the parent block in the case of nested blocks.
 * 
 * See also {@link SimpleSelectorSequence} and {@link Selector}.
 */
public class ParentSelector extends TypeSelector {

    public static final ParentSelector it = new ParentSelector();

    private ParentSelector() {
        super(new StringInterpolationSequence("&"));
    }

    @Override
    public ParentSelector replaceVariables(ScssContext context) {
        return this;
    }
}
