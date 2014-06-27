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
import com.vaadin.sass.internal.util.ColorUtil;

public class GrayscaleFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "color" };

    public GrayscaleFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "grayscale");
    }

    @Override
    protected LexicalUnitImpl computeForParam(String functionName,
            LexicalUnitImpl firstParam) {
        if (!ColorUtil.isColor(firstParam) && !ColorUtil.isRgba(firstParam)
                && !ColorUtil.isHsla(firstParam)) {
            throw new ParseException(
                    "The argument of grayscale() must be a valid color",
                    firstParam);
        }
        float[] hsl = ColorUtil.colorToHsl(firstParam);
        hsl[1] = 0;
        float alpha = ColorUtil.getAlpha(firstParam);
        return ColorUtil.createHslaOrHslColor(hsl, alpha,
                firstParam.getLineNumber(), firstParam.getColumnNumber());
    }
}