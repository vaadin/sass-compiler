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

import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

public class TypeOfFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "value" };

    public TypeOfFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "type-of");
    }

    @Override
    protected SassListItem computeForArgumentList(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem param = getParam(actualArguments, 0);
        String type = "string";

        if (param instanceof SassList) {
            type = "list";
        } else if (param instanceof LexicalUnitImpl) {
            LexicalUnitImpl unit = (LexicalUnitImpl) param;
            if (unit.getLexicalUnitType() == LexicalUnitImpl.SCSS_NULL) {
                type = "null";
            } else if (isNumber(unit)) {
                type = "number";
            } else if (isBoolean(unit)) {
                type = "bool";
            } else if (unit.getLexicalUnitType() == LexicalUnitImpl.SAC_RGBCOLOR) {
                type = "color";
            } else if (unit.getLexicalUnitType() == LexicalUnitImpl.SAC_IDENT
                    && unit.getStringValue().matches(
                            "#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})")) {
                // TODO support also named colors
                type = "color";
            } else if (unit.getLexicalUnitType() == LexicalUnitImpl.SAC_FUNCTION) {
                if ("rgb".equals(unit.getFunctionName())
                        || "rgba".equals(unit.getFunctionName())
                        || "hsl".equals(unit.getFunctionName())
                        || "hsla".equals(unit.getFunctionName())) {
                    type = "color";
                }
            }
        }

        return createIdent(function, type);
    }

    private boolean isBoolean(LexicalUnitImpl unit) {
        if (unit.getLexicalUnitType() != LexicalUnitImpl.SAC_IDENT) {
            return false;
        }
        return "true".equals(unit.getStringValue())
                || "false".equals(unit.getStringValue());
    }

    private boolean isNumber(LexicalUnitImpl unit) {
        return unit.isNumber();
    }

    private LexicalUnitImpl createIdent(LexicalUnitImpl function,
            String paramType) {
        return LexicalUnitImpl.createRawIdent(function.getLineNumber(),
                function.getColumnNumber(), paramType);
    }

}
