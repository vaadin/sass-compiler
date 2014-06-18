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
import java.util.Arrays;
import java.util.List;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.util.ColorUtil;

public class AdjustColorFunctionGenerator extends AbstractFunctionGenerator {

    private String[] optionalParams = { "red", "green", "blue", "hue",
            "saturation", "lightness", "alpha" };

    public AdjustColorFunctionGenerator() {
        super("adjust-color", "scale-color");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        String functionName = function.getFunctionName();
        checkParams(function);
        LexicalUnitImpl color = getColor(function);
        float alpha = 1;
        if (ColorUtil.isRgba(color) || ColorUtil.isHsla(color)) {
            int lastIndex = color.getParameterList().size() - 1;
            alpha = color.getParameterList().get(lastIndex).getContainedValue()
                    .getFloatValue();
        }
        Float[] adjustBy = getAdjustments(function);
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
        int[] rgb = ColorUtil.colorToRgb(color);
        if (adjustRGB) {
            if ("adjust-color".equals(functionName)) {
                adjustRgb(rgb, adjustBy);
            } else {
                scaleRgb(rgb, adjustBy);
            }
        }
        if (adjustHsl) {
            float[] hsl = ColorUtil.colorToHsl(color);
            if ("adjust-color".equals(functionName)) {
                adjustHsl(hsl, adjustBy);
            } else {
                scaleHsl(hsl, adjustBy);
            }
            rgb = ColorUtil.hslToRgb(hsl);
        }
        color = ColorUtil.createHexColor(rgb[0], rgb[1], rgb[2],
                function.getLineNumber(), function.getColumnNumber());
        if (alpha == 1.0f) {
            return color;
        } else {
            return ColorUtil.createRgbaColor(rgb[0], rgb[1], rgb[2], alpha,
                    function.getLineNumber(), function.getColumnNumber());
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

    private LexicalUnitImpl getColor(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        SassListItem resultItem = null;
        if (params.size() > 0) {
            resultItem = params.get(0);
        } else {
            for (VariableNode node : params.getNamedVariables()) {
                if (node.getName().equals("color")) {
                    resultItem = node.getExpr();
                    break;
                }
            }
        }
        if (!(resultItem instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "The color argument of adjust-color must represent a valid color",
                    function);
        }
        LexicalUnitImpl result = (LexicalUnitImpl) resultItem;
        if (!ColorUtil.isColor(result) && !ColorUtil.isRgba(result)
                && !ColorUtil.isHsla(result)) {
            throw new ParseException(
                    "The color argument of adjust-color must represent a valid color",
                    function);
        }
        return result;
    }

    /**
     * Gets the adjustment amounts from the parameter list of function. Values
     * that are not to be adjusted are represented as null. The value result[i]
     * corresponds to the parameter with name optionalParams[i].
     * 
     */
    private Float[] getAdjustments(LexicalUnitImpl function) {
        ActualArgumentList parameters = function.getParameterList();
        Float[] result = new Float[7];
        for (VariableNode node : parameters.getNamedVariables()) {
            String name = node.getName();
            for (int i = 0; i < optionalParams.length; i++) {
                if (name.equals(optionalParams[i])) {
                    if (result[i] != null) {
                        throw new ParseException("The parameter " + name
                                + " was set more than once for adjust-color",
                                function);
                    }
                    SassListItem valueItem = node.getExpr();
                    if (!(valueItem instanceof LexicalUnitImpl)
                            || !((LexicalUnitImpl) valueItem).isNumber()) {
                        throw new ParseException(
                                "The parameters of adjust-color must be numeric values",
                                function);
                    }
                    float value = ((LexicalUnitImpl) valueItem).getFloatValue();
                    result[i] = value;
                }
            }
        }
        return result;
    }

    /**
     * Checks that there are no named parameters that are not supported by the
     * function. If the function is scale-color, also checks that all values are
     * percentages.
     */
    private void checkParams(LexicalUnitImpl function) {
        List<String> optionalParamsList = new ArrayList<String>(
                Arrays.asList(optionalParams));
        optionalParamsList.add("color");
        if ("scale-color".equals(function.getFunctionName())) {
            optionalParamsList.remove("hue");
        }
        for (VariableNode node : function.getParameterList()
                .getNamedVariables()) {
            if (!optionalParamsList.contains(node.getName())) {
                throw new ParseException("There is no parameter "
                        + node.getName() + " for function "
                        + function.getFunctionName(), function);
            }
            if ("scale-color".equals(function.getFunctionName())
                    && !"color".equals(node.getName())) {
                SassListItem value = node.getExpr();
                if (!(value instanceof LexicalUnitImpl)
                        || !LexicalUnitImpl.checkLexicalUnitType(value,
                                LexicalUnitImpl.SAC_PERCENTAGE)) {
                    throw new ParseException(
                            "The parameters of scale-color must be percentage values",
                            function);
                }
            }
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
