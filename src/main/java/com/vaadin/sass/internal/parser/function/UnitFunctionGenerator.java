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

import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;

public class UnitFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "number" };

    public UnitFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "unit");
    }

    @Override
    protected LexicalUnitImpl computeForParam(String functionName,
            LexicalUnitImpl firstParam) {
        if (!firstParam.isNumber()) {
            throw new ParseException("The parameter of " + functionName
                    + "() must be a number", firstParam);
        }
        String unit = firstParam.getDimensionUnitText();
        return LexicalUnitImpl.createString(firstParam.getLineNumber(),
                firstParam.getColumnNumber(), unit);
    }
}