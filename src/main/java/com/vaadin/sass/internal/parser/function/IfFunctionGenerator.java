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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;

public class IfFunctionGenerator implements SCSSFunctionGenerator {

    private static String[] argumentNames = { "condition", "if-true",
            "if-false" };

    private String[] functionNames;
    private FormalArgumentList arguments;

    public IfFunctionGenerator() {
        arguments = AbstractFunctionGenerator.createArgumentList(argumentNames,
                false);
        functionNames = new String[] { "if" };
    }

    @Override
    public String[] getFunctionNames() {
        return functionNames;
    }

    @Override
    public SassListItem compute(ScssContext context, LexicalUnitImpl function) {
        ActualArgumentList args = function.getParameterList();
        FormalArgumentList functionArguments;
        try {
            functionArguments = arguments.replaceFormalArguments(args, true);
        } catch (ParseException e) {
            throw new ParseException("Error in parameters of function "
                    + function.getFunctionName() + "(), line "
                    + function.getLineNumber() + ", column "
                    + function.getColumnNumber() + ": [" + e.getMessage() + "]");
        }
        return computeForArgumentList(context, function, functionArguments);
    }

    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem firstParam = AbstractFunctionGenerator.getParam(
                actualArguments, "condition").evaluateFunctionsAndExpressions(
                context, true);
        if (BinaryOperator.isTrue(firstParam)) {
            return AbstractFunctionGenerator.getParam(actualArguments,
                    "if-true").evaluateFunctionsAndExpressions(context, true);
        } else {
            return AbstractFunctionGenerator.getParam(actualArguments,
                    "if-false").evaluateFunctionsAndExpressions(context, true);
        }
    }
}