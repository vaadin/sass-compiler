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
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.ColorUtil;

public class RGBComponentFunctionGenerator extends AbstractFunctionGenerator {

    public RGBComponentFunctionGenerator() {
        super("red", "green", "blue");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        int componentNumber; // the index of the wanted component in an RGB
                             // array
        if ("red".equals(function.getFunctionName())) {
            componentNumber = 0;
        } else if ("green".equals(function.getFunctionName())) {
            componentNumber = 1;
        } else {
            componentNumber = 2;
        }
        checkParameters(function);
        LexicalUnitImpl color = function.getParameterList().get(0)
                .getContainedValue();
        int[] rgb;
        if (ColorUtil.isRgba(color)) {
            SassList rgbaComponents = color.getParameterList();
            if (rgbaComponents.size() == 2) { // the first component is a hex
                                              // color
                rgb = ColorUtil.colorToRgb((LexicalUnitImpl) rgbaComponents
                        .get(0));
            } else { // the components are red, green, blue, alpha
                return rgbaComponents.get(componentNumber);
            }
        } else { // handle a non-rgba color
            rgb = ColorUtil.colorToRgb(color);
        }
        return LexicalUnitImpl.createInteger(color.getLineNumber(),
                color.getColumnNumber(), rgb[componentNumber]);
    }

    private void checkParameters(LexicalUnitImpl function) {
        SassList params = function.getParameterList();
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