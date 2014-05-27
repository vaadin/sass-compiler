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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.ColorUtil;

public class TransparencyModificationFunctionGenerator extends
        AbstractFunctionGenerator {

    public TransparencyModificationFunctionGenerator() {
        super("transparentize", "fade-out", "opacify", "fade-in");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        checkParameters(function);
        float factor = 1.0f; // for opacify/fade-in
        if ("fade-out".equals(function.getFunctionName())
                || "transparentize".equals(function.getFunctionName())) {
            factor = -1.0f;
        }
        ActualArgumentList params = function.getParameterList();
        float amount = getFloat(params, 1);
        LexicalUnitImpl color = (LexicalUnitImpl) params.get(0);
        int[] rgb = null;
        float opacity = 1.0f;
        if (ColorUtil.isRgba(color)) {
            ActualArgumentList colorComponents = color.getParameterList();
            if (colorComponents.size() == 2) {
                color = (LexicalUnitImpl) colorComponents.get(0);
                opacity = getFloat(colorComponents, 1);
            } else {
                rgb = new int[] { getInteger(colorComponents, 0),
                        getInteger(colorComponents, 1),
                        getInteger(colorComponents, 2) };
                opacity = getFloat(colorComponents, 3);
                color = null;
            }
        }
        if (color != null && ColorUtil.isColor(color)) {
            rgb = ColorUtil.colorToRgb(color);
        }

        List<SassListItem> newParamValues = new ArrayList<SassListItem>();
        newParamValues.add(createNumber(function, rgb[0]));
        newParamValues.add(createNumber(function, rgb[1]));
        newParamValues.add(createNumber(function, rgb[2]));
        opacity += factor * amount;
        opacity = Math.min(1, Math.max(0, opacity));
        if (opacity == 1.0f) {
            ActualArgumentList newParams = new ActualArgumentList(
                    SassList.Separator.COMMA, newParamValues);
            return LexicalUnitImpl.createRGBColor(function.getLineNumber(),
                    function.getColumnNumber(), newParams);
        }
        newParamValues.add(LexicalUnitImpl.createNumber(
                function.getLineNumber(), function.getColumnNumber(), opacity));
        ActualArgumentList newParams = new ActualArgumentList(
                SassList.Separator.COMMA, newParamValues);
        LexicalUnitImpl result = LexicalUnitImpl.createFunction(
                function.getLineNumber(), function.getColumnNumber(), "rgba",
                newParams);
        return result;

    }

    private void checkParameters(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        if (params.size() != 2) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + "requires exactly two parameters", function);
        }
        if (!(params.get(0) instanceof LexicalUnitImpl)
                || (!ColorUtil.isColor(params.get(0).getContainedValue()) && !ColorUtil
                        .isRgba(params.get(0).getContainedValue()))) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + "requires a valid color as its first parameter", function);
        }
        if (!(params.get(1) instanceof LexicalUnitImpl)
                || !LexicalUnitImpl.checkLexicalUnitType(params.get(1),
                        LexicalUnitImpl.SAC_INTEGER, LexicalUnitImpl.SAC_REAL)) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + "requires a number as its second parameter", function);
        }
        float amount = params.get(1).getContainedValue().getFloatValue();
        if (amount < 0.0 || amount > 1.0) {
            throw new ParseException(
                    "The function "
                            + function.getFunctionName()
                            + "requires a number in the range [0, 1] as its second parameter",
                    function);
        }
    }

    private float getFloat(ActualArgumentList params, int i) {
        return params.get(i).getContainedValue().getFloatValue();
    }

    private int getInteger(ActualArgumentList colorComponents, int i) {
        return colorComponents.get(i).getContainedValue().getIntegerValue();
    }

    private LexicalUnitImpl createNumber(LexicalUnitImpl parent, float value) {
        return LexicalUnitImpl.createNumber(parent.getLineNumber(),
                parent.getColumnNumber(), value);
    }

    private LexicalUnitImpl createNumber(LexicalUnitImpl parent, int value) {
        return LexicalUnitImpl.createNumber(parent.getLineNumber(),
                parent.getColumnNumber(), value);
    }
}
