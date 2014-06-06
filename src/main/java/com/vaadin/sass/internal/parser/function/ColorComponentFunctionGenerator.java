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
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.ColorUtil;

public class ColorComponentFunctionGenerator extends AbstractFunctionGenerator {

    public ColorComponentFunctionGenerator() {
        super("red", "green", "blue", "hue", "saturation", "lightness");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        boolean hslComponent = false;
        int componentNumber = 0; // the index of the wanted component in an RGB
                                 // array
        if ("red".equals(function.getFunctionName())) {
            componentNumber = 0;
        } else if ("green".equals(function.getFunctionName())) {
            componentNumber = 1;
        } else if ("blue".equals(function.getFunctionName())) {
            componentNumber = 2;
        } else if ("hue".equals(function.getFunctionName())) {
            componentNumber = 0;
            hslComponent = true;
        } else if ("saturation".equals(function.getFunctionName())) {
            componentNumber = 1;
            hslComponent = true;
        } else if ("lightness".equals(function.getFunctionName())) {
            componentNumber = 2;
            hslComponent = true;
        }
        checkParameters(function);
        LexicalUnitImpl color = function.getParameterList().get(0)
                .getContainedValue();
        if (hslComponent) {
            float[] components = ColorUtil.colorToHsl(color);
            if (componentNumber == 0) {
                return LexicalUnitImpl.createDEG(color.getLineNumber(),
                        color.getColumnNumber(), components[componentNumber]);
            } else {
                return LexicalUnitImpl.createPercentage(color.getLineNumber(),
                        color.getColumnNumber(), components[componentNumber]);
            }
        } else {
            int[] components = ColorUtil.colorToRgb(color);
            return LexicalUnitImpl.createInteger(color.getLineNumber(),
                    color.getColumnNumber(), components[componentNumber]);
        }
    }

    private void checkParameters(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        if (params.size() != 1 || !(params.get(0) instanceof LexicalUnitImpl)) {
            throw new ParseException("Function " + function.getFunctionName()
                    + " must have exactly one single value parameter", function);
        }
        LexicalUnitImpl firstParam = (LexicalUnitImpl) params.get(0);
        if (!ColorUtil.isColor(firstParam) && !ColorUtil.isRgba(firstParam)) {
            throw new ParseException("The parameter of the function "
                    + function.getFunctionName() + " must be a valid color",
                    function);
        }
    }
}