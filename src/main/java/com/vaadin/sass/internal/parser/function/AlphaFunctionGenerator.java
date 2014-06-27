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

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.ColorUtil;

public class AlphaFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "color" };

    public AlphaFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "alpha", "opacity");
    }

    @Override
    protected SassListItem computeForArgumentList(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        checkParameters(function, actualArguments);
        LexicalUnitImpl color = (LexicalUnitImpl) getParam(actualArguments,
                "color");
        float opacity = 1.0f;
        if (ColorUtil.isRgba(color) || ColorUtil.isHsla(color)) {
            ActualArgumentList parameterList = color.getParameterList();
            SassListItem last = parameterList.get(parameterList.size() - 1);
            opacity = ((LexicalUnitImpl) last).getFloatValue();
        }
        return LexicalUnitImpl.createNumber(function.getLineNumber(),
                function.getColumnNumber(), opacity);
    }

    private void checkParameters(LexicalUnitImpl function,
            FormalArgumentList args) {
        LexicalUnitImpl color = (LexicalUnitImpl) getParam(args, "color");
        if (!(color instanceof LexicalUnitImpl)
                || (!ColorUtil.isColor(color) && !ColorUtil.isRgba(color) && !ColorUtil
                        .isHsla(color))) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires a color as its first parameter", function);
        }
    }
}
