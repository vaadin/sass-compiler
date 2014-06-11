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
package com.vaadin.sass.internal.parser.function;

import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;

public class IfFunctionGenerator extends AbstractFunctionGenerator {

    public IfFunctionGenerator() {
        super("if");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        if (params.size() != 3) {
            throw new ParseException(
                    "Function if() requires exactly 3 parameters", function);
        }
        SassListItem firstParam = params.get(0)
                .evaluateFunctionsAndExpressions(true);
        if (BinaryOperator.isTrue(firstParam)) {
            return params.get(1).evaluateFunctionsAndExpressions(true);
        } else {
            return params.get(2).evaluateFunctionsAndExpressions(true);
        }
    }
}