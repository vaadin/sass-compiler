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
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.ColorUtil;

public class AdjustColorFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "color", "red", "green", "blue",
            "hue", "saturation", "lightness", "alpha" };

    @Override
    protected boolean checkForUnsetParameters() {
        return false;
    }

    public AdjustColorFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "adjust-color",
                "scale-color");
    }

    @Override
    protected SassListItem computeForArgumentList(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        String functionName = function.getFunctionName();
        checkParams(function, actualArguments);
        LexicalUnitImpl color = getColor(function, actualArguments);
        float alpha = 1;
        if (ColorUtil.isRgba(color) || ColorUtil.isHsla(color)) {
            int lastIndex = color.getParameterList().size() - 1;
            alpha = color.getParameterList().get(lastIndex).getContainedValue()
                    .getFloatValue();
        }
        Float[] adjustBy = getAdjustments(function, actualArguments);
        if (adjustBy[6] != null) {
            if ("adjust-color".equals(functionName)) {
                alpha += adjustBy[6];
            } else {
                float diff = adjustBy[6] > 0 ? 1 - alpha : alpha;
                float adjustAmount = diff * adjustBy[6] / 100;
                alpha = alpha + adjustAmount;
            }
            alpha = Math.min(1, Math.max(0, alpha));
        }
        boolean adjustRGB = anySet(adjustBy, 0, 3);
        boolean adjustHsl = anySet(adjustBy, 3, 6);
        if (adjustRGB && adjustHsl) {
            throw new ParseException(
                    "The function adjust-color cannot modify both RGB and HSL values",
                    function);
        }
        if (adjustRGB) {
            int[] rgb = ColorUtil.colorToRgb(color);
            if ("adjust-color".equals(functionName)) {
                adjustRgb(rgb, adjustBy);
            } else {
                scaleRgb(rgb, adjustBy);
            }
            return ColorUtil.createRgbaOrHexColor(rgb, alpha,
                    function.getLineNumber(), function.getColumnNumber());
        }
        if (adjustHsl) {
            float[] hsl = ColorUtil.colorToHsl(color);
            if ("adjust-color".equals(functionName)) {
                adjustHsl(hsl, adjustBy);
            } else {
                scaleHsl(hsl, adjustBy);
            }
            return ColorUtil.createHslaOrHslColor(hsl, alpha,
                    function.getLineNumber(), function.getColumnNumber());
        }
        // Only alpha modified, preserve whether an RGB or HSL color.
        if (ColorUtil.isHsla(color) || ColorUtil.isHslColor(color)) {
            return ColorUtil
                    .createHslaOrHslColor(ColorUtil.colorToHsl(color), alpha,
                            function.getLineNumber(),
                            function.getColumnNumber());
        } else {
            return ColorUtil
                    .createRgbaOrHexColor(ColorUtil.colorToRgb(color), alpha,
                            function.getLineNumber(),
                            function.getColumnNumber());
        }
    }

    private void scaleHsl(float[] hsl, Float[] adjustBy) {
        // Only saturation and lightness can be scaled
        for (int i = 1; i < 3; i++) {
            Float adjustment = adjustBy[3 + i];
            if (adjustment != null) {
                float diff = adjustment > 0 ? 100 - hsl[i] : hsl[i];
                float adjustAmount = diff * adjustment / 100;
                float newValue = hsl[i] + adjustAmount;
                hsl[i] = newValue;
            }
        }
    }

    private void adjustHsl(float[] hsl, Float[] adjustBy) {
        hsl[0] += adjustBy[3] == null ? 0 : adjustBy[3];
        hsl[0] = ((hsl[0] % 360) + 360) % 360;
        hsl[1] += adjustBy[4] == null ? 0 : adjustBy[4];
        hsl[1] = Math.min(100, Math.max(0, hsl[1]));
        hsl[2] += adjustBy[5] == null ? 0 : adjustBy[5];
        hsl[2] = Math.min(100, Math.max(0, hsl[2]));
    }

    private void scaleRgb(int[] rgb, Float[] adjustBy) {
        for (int i = 0; i < 3; i++) {
            if (adjustBy[i] != null) {
                int diff = (adjustBy[i] > 0 ? 255 - rgb[i] : rgb[i]);
                float adjustAmount = diff * adjustBy[i] / 100;
                float newValue = rgb[i] + adjustAmount;
                rgb[i] = (int) newValue;
            }
        }
    }

    private void adjustRgb(int[] rgb, Float[] adjustBy) {
        rgb[0] += adjustBy[0] == null ? 0 : adjustBy[0];
        rgb[0] = Math.min(255, Math.max(0, rgb[0]));
        rgb[1] += adjustBy[1] == null ? 0 : adjustBy[1];
        rgb[1] = Math.min(255, Math.max(0, rgb[1]));
        rgb[2] += adjustBy[2] == null ? 0 : adjustBy[2];
        rgb[2] = Math.min(255, Math.max(0, rgb[2]));
    }

    private LexicalUnitImpl getColor(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem resultItem = getParam(actualArguments, "color");
        if (!(resultItem instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "The color argument must represent a valid color",
                    function);
        }
        LexicalUnitImpl result = (LexicalUnitImpl) resultItem;
        if (!ColorUtil.isColor(result) && !ColorUtil.isRgba(result)
                && !ColorUtil.isHsla(result)) {
            throw new ParseException(
                    "The color argument must represent a valid color",
                    function);
        }
        return result;
    }

    /**
     * Gets the adjustment amounts from the parameter list actualArguments.
     * Values that are not to be adjusted are represented as null. The value
     * result[i] corresponds to the parameter with name argumentNames[i + 1].
     * 
     * @param actualArguments
     * 
     */
    private Float[] getAdjustments(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        Float[] result = new Float[7];
        for (int i = 0; i < 7; i++) {
            SassListItem valueItem = getParam(actualArguments, i + 1);
            if (valueItem == null) {
                continue;
            }
            if (!(valueItem instanceof LexicalUnitImpl)
                    || !((LexicalUnitImpl) valueItem).isNumber()) {
                throw new ParseException(
                        "The parameters of adjust-color must be numeric values",
                        function);
            }
            result[i] = ((LexicalUnitImpl) valueItem).getFloatValue();
        }
        return result;
    }

    /**
     * For scale-color function, checks that all values are percentages and that
     * there is no argument called hue.
     * 
     * @param actualArguments
     */
    private void checkParams(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {

        for (int i = 1; i < argumentNames.length; i++) {
            SassListItem value = getParam(actualArguments, i);
            if ("scale-color".equals(function.getFunctionName())
                    && value != null) {
                if (!(value instanceof LexicalUnitImpl)
                        || !LexicalUnitImpl.checkLexicalUnitType(value,
                                LexicalUnitImpl.SAC_PERCENTAGE)) {
                    throw new ParseException(
                            "The parameters of scale-color must be percentage values",
                            function);
                }
            }
        }
        if ("scale-color".equals(function.getFunctionName())
                && getParam(actualArguments, "hue") != null) {
            throw new ParseException(
                    "There is no parameter hue for scale-color", function);
        }
    }

    /**
     * Returns true if at least one of values[i], where i ranges from 'from'
     * (inclusive) to 'to' (exclusive), is not null.
     * 
     * @return whether there is a non-null value in values[from...to-1].
     */
    private boolean anySet(Object[] values, int from, int to) {
        for (int i = from; i < to; i++) {
            if (values[i] != null) {
                return true;
            }
        }
        return false;
    }
}