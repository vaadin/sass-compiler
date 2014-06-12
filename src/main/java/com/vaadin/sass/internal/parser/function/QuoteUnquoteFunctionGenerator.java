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

public class QuoteUnquoteFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {
    private static String[] argumentNames = { "string" };

    public QuoteUnquoteFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "quote", "unquote");
    }

    @Override
    protected LexicalUnitImpl computeForParam(String functionName,
            LexicalUnitImpl firstParam) {
        if (!LexicalUnitImpl.checkLexicalUnitType(firstParam,
                LexicalUnitImpl.SAC_IDENT, LexicalUnitImpl.SAC_STRING_VALUE,
                LexicalUnitImpl.SAC_URI)) {
            throw new ParseException("The parameter of " + functionName
                    + "() must be a string", firstParam);
        }
        String result = firstParam.printState();
        if ("quote".equals(functionName)) {
            if (isQuoted(result)) {
                return firstParam;
            } else {
                return LexicalUnitImpl.createString(firstParam.getLineNumber(),
                        firstParam.getColumnNumber(), result);
            }
        } else {
            result = unquote(result);
            return LexicalUnitImpl.createIdent(firstParam.getLineNumber(),
                    firstParam.getColumnNumber(), result);
        }
    }

    private String unquote(String string) {
        if (!isQuoted(string)) {
            return string;
        } else {
            return string.substring(1, string.length() - 1);
        }
    }

    private boolean isQuoted(String string) {
        return (string.length() > 1 && ((string.charAt(0) == '"' && string
                .charAt(string.length() - 1) == '"') || (string.charAt(0) == '\'' && string
                .charAt(string.length() - 1) == '\'')));
    }
}