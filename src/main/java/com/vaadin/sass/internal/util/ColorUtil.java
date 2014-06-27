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

package com.vaadin.sass.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.css.sac.LexicalUnit;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassList.Separator;

public class ColorUtil {

    private static Map<String, String> colorNameToHex = new HashMap<String, String>();
    private static Map<String, String> hexToColorName = new HashMap<String, String>();

    static {
        colorNameToHex.put("aqua", "#00ffff");
        colorNameToHex.put("black", "#000000");
        colorNameToHex.put("blue", "#0000ff");
        colorNameToHex.put("fuchsia", "#ff00ff");
        colorNameToHex.put("gray", "#808080");
        colorNameToHex.put("green", "#008000");
        colorNameToHex.put("lime", "#00ff00");
        colorNameToHex.put("maroon", "#800000");
        colorNameToHex.put("navy", "#000080");
        colorNameToHex.put("olive", "#808000");
        colorNameToHex.put("orange", "#ffa500");
        colorNameToHex.put("purple", "#800080");
        colorNameToHex.put("red", "#ff0000");
        colorNameToHex.put("silver", "#c0c0c0");
        colorNameToHex.put("teal", "#008080");
        colorNameToHex.put("white", "#ffffff");
        colorNameToHex.put("yellow", "#ffff00");

        for (Entry<String, String> entry : colorNameToHex.entrySet()) {
            hexToColorName.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Returns true if the lexical unit represents a valid color (hexadecimal,
     * rgb(), color name etc.), false otherwise.
     * 
     * Note that rgba() and hsla() are not considered to be normal colors. To
     * detect it, call isRgba() or isHsla().
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a color
     */
    public static boolean isColor(LexicalUnitImpl unit) {
        return isHexColor(unit) || isHslColor(unit) || isRgbFunction(unit)
                || isColorName(unit);
    }

    /**
     * Returns true if the lexical unit represents a valid rgb() method call ,
     * false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an RGB method call
     */
    public static boolean isRgbFunction(LexicalUnitImpl unit) {
        if (!LexicalUnitImpl.checkLexicalUnitType(unit,
                LexicalUnit.SAC_FUNCTION, LexicalUnit.SAC_RGBCOLOR)) {
            return false;
        }
        if (unit.getParameterList().size() != 3
                || !"rgb".equals(unit.getFunctionName())) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the lexical unit represents a valid RGBA value, false
     * otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an RGBA value
     */
    public static boolean isRgba(LexicalUnitImpl unit) {
        return unit.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION
                && "rgba".equals(unit.getFunctionName())
                && (unit.getParameterList().size() == 2 || unit
                        .getParameterList().size() == 4);
    }

    /**
     * Returns true if the lexical unit represents a valid HSLA value, false
     * otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an HSLA value
     */
    public static boolean isHsla(LexicalUnitImpl unit) {
        return unit.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION
                && "hsla".equals(unit.getFunctionName())
                && unit.getParameterList().size() == 4;
    }

    /**
     * Returns true if the lexical unit represents a valid color in the
     * hexadecimal form (three or six digits), false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a hexadecimal color
     */
    public static boolean isHexColor(LexicalUnitImpl unit) {
        return unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT
                && isHexColor(unit.getStringValue());
    }

    /**
     * Returns true if the lexical unit represents a valid color name, false
     * otherwise.
     * 
     * Currently, the 17 standard color names from the HTML and CSS color
     * specification are supported.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a color name
     */
    public static boolean isColorName(LexicalUnitImpl unit) {
        return unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT
                && colorNameToHex.containsKey(unit.getStringValue());
    }

    /**
     * Returns true if the lexical unit represents a valid hsl() color function
     * call, false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents as HSL color
     */
    public static boolean isHslColor(LexicalUnitImpl unit) {
        return unit.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION
                && "hsl".equals(unit.getFunctionName())
                && unit.getParameterList().size() == 3;
    }

    /**
     * Returns the alpha component of the color. For colors that do not have an
     * explicit alpha component, returns 1.
     * 
     * @param color
     *            An object representing a valid color.
     * @return The alpha component of color.
     */
    public static float getAlpha(LexicalUnitImpl color) {
        if (isHsla(color) || isRgba(color)) {
            ActualArgumentList params = color.getParameterList();
            return params.get(params.size() - 1).getContainedValue()
                    .getFloatValue();
        } else if (isColor(color)) {
            return 1;
        }
        throw new ParseException("The parameter is not a valid color: "
                + color.toString(), color);
    }

    /**
     * Converts a color into an array of its RGB components.
     * 
     * In the case of an RGBA value, the alpha channel is ignored.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return RGB components or null if not a color
     */
    public static int[] colorToRgb(LexicalUnitImpl color) {
        if (isRgba(color)) {
            if (color.getParameterList().size() == 2
                    && color.getParameterList().get(0) instanceof LexicalUnitImpl) {
                return colorToRgb((LexicalUnitImpl) color.getParameterList()
                        .get(0));
            } else {
                int red = color.getParameterList().get(0).getContainedValue()
                        .getIntegerValue();
                int green = color.getParameterList().get(1).getContainedValue()
                        .getIntegerValue();
                int blue = color.getParameterList().get(2).getContainedValue()
                        .getIntegerValue();
                return new int[] { red, green, blue };
            }
        } else if (isHsla(color)) {
            return hslToRgb(color);
        } else if (isHexColor(color)) {
            return hexColorToRgb(color);
        } else if (isHslColor(color)) {
            return hslToRgb(color);
        } else if (isRgbFunction(color)) {
            return rgbFunctionToRgb(color);
        } else if (isColorName(color)) {
            return colorNameToRgb(color);
        }
        return null;
    }

    /**
     * Converts an array of RGB components to a string representing the color.
     * 
     * @param rgb
     *            the RGB components of a color
     * @return a valid string representation of the color
     */
    public static String rgbToColorString(int[] rgb) {
        String colorString = rgbToHexColor(rgb, 6);
        if (hexToColorName.containsKey(colorString)) {
            colorString = hexToColorName.get(colorString);
        }
        return colorString;
    }

    /**
     * Converts an array of HSL components to a string representing the color.
     * 
     * @param hsl
     *            the HSL components of a color
     * @return a valid string representation of the color
     */
    public static int[] hslToRgb(float[] hsl) {
        float h = ((hsl[0] % 360) + 360) % 360 / 360f;
        float s = hsl[1] / 100;
        float l = hsl[2] / 100;
        float m2, m1;
        int[] rgb = new int[3];
        m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
        m1 = l * 2 - m2;
        rgb[0] = Math.round(hueToRgb(m1, m2, h + 1f / 3) * 255);
        rgb[1] = Math.round(hueToRgb(m1, m2, h) * 255);
        rgb[2] = Math.round(hueToRgb(m1, m2, h - 1f / 3) * 255);
        return rgb;
    }

    /**
     * Converts a color into an array of its HSL (hue, saturation, lightness)
     * components.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return HSL components or null if not a color
     */
    public static float[] colorToHsl(LexicalUnitImpl color) {
        if (isHslColor(color) || isHsla(color)) {
            float hue = color.getParameterList().get(0).getContainedValue()
                    .getFloatValue();
            float saturation = color.getParameterList().get(1)
                    .getContainedValue().getFloatValue();
            float lightness = color.getParameterList().get(2)
                    .getContainedValue().getFloatValue();
            return new float[] { hue, saturation, lightness };
        }
        // TODO shortcut path for hsl()? need to take percent vs. integer vs.
        // real into account
        // TODO int[] loses precision
        int[] rgb = colorToRgb(color);
        if (rgb == null) {
            return null;
        }
        float hsl[] = calculateHsl(rgb[0], rgb[1], rgb[2]);
        return hsl;
    }

    /**
     * Returns true if the string represents a valid color in the hexadecimal
     * form (three or six digits), false otherwise.
     * 
     * @param string
     *            string that might represent a hex color
     * @return true if string represents a hexadecimal color
     */
    public static boolean isHexColor(String string) {
        return string.matches("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})");
    }

    /**
     * Converts an rgb() function call into an array of its RGB components. The
     * caller must ensure that isRgbFunction(color) returns true for the
     * parameter.
     * 
     * @param color
     *            a lexical unit that represents a function call for rgb()
     * @return RGB components
     */
    private static int[] rgbFunctionToRgb(LexicalUnitImpl color) {
        int red = color.getParameterList().get(0).getContainedValue()
                .getIntegerValue();
        int green = color.getParameterList().get(1).getContainedValue()
                .getIntegerValue();
        int blue = color.getParameterList().get(2).getContainedValue()
                .getIntegerValue();
        return new int[] { red, green, blue };
    }

    /**
     * Converts a hex color (with three or six hex characters) into an array of
     * its RGB components. The caller must ensure that isHexColor(hexColor)
     * returns true for the parameter.
     * 
     * @param hexColor
     *            a hexadecimal representation for a color (in CSS and HTML
     *            color specification format)
     * @return RGB components
     */
    private static int[] hexColorToRgb(String hexColor) {
        String s = hexColor.substring(1);
        int r = 0, g = 0, b = 0;
        if (s.length() == 3) {
            String sh = s.substring(0, 1);
            r = Integer.parseInt(sh + sh, 16);
            sh = s.substring(1, 2);
            g = Integer.parseInt(sh + sh, 16);
            sh = s.substring(2, 3);
            b = Integer.parseInt(sh + sh, 16);
        } else if (s.length() == 6) {
            r = Integer.parseInt(s.substring(0, 2), 16);
            g = Integer.parseInt(s.substring(2, 4), 16);
            b = Integer.parseInt(s.substring(4, 6), 16);
        }
        return new int[] { r, g, b };

    }

    /**
     * Converts a hex color (with three or six hex characters) into an array of
     * its RGB components. The caller must ensure that isHexColor(hexColor)
     * returns true for the parameter.
     * 
     * @param hexColor
     *            a lexical unit containing an identifier that represents a
     *            hexadecimal representation for a color (in CSS and HTML color
     *            specification format)
     * @return RGB components
     */
    private static int[] hexColorToRgb(LexicalUnitImpl hexColor) {
        return hexColorToRgb(hexColor.getStringValue());
    }

    /**
     * Converts a color name into an array of its RGB components. The caller
     * must ensure that isColorName(color) returns true for the parameter.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return RGB components
     */
    private static int[] colorNameToRgb(LexicalUnitImpl color) {
        return hexColorToRgb(colorNameToHex.get(color.getStringValue()));
    }

    private static String rgbToHexColor(int[] rgb, int length) {
        StringBuilder builder = new StringBuilder("#");
        for (int i = 0; i < 3; i++) {
            String color = Integer.toHexString(rgb[i]);
            if (length == 6) {
                if (color.length() == 1) {
                    color = "0" + color;
                }
            }
            if (length == 3) {
                color = color.substring(0, 1);
            }
            builder.append(color);
        }
        return builder.toString();
    }

    private static LexicalUnitImpl hslToColor(LexicalUnitImpl hsl) {
        String colorString = rgbToColorString(hslToRgb(hsl));
        return LexicalUnitImpl.createIdent(colorString);
    }

    private static LexicalUnitImpl colorToHslUnit(LexicalUnitImpl color) {
        float[] hsl = colorToHsl(color);

        return createHslFunction(hsl[0], hsl[1], hsl[2], color.getLineNumber(),
                color.getColumnNumber());
    }

    private static int[] hslToRgb(LexicalUnitImpl hsl) {
        ActualArgumentList hslParam = hsl.getParameterList();
        if (hslParam.size() != 3 && hslParam.size() != 4) {
            throw new ParseException(
                    "The function hsl() requires exactly three parameters", hsl);
        }

        float hue = hslParam.get(0).getContainedValue().getFloatValue();
        float saturation = hslParam.get(1).getContainedValue().getFloatValue();
        float lightness = hslParam.get(2).getContainedValue().getFloatValue();

        return hslToRgb(new float[] { hue, saturation, lightness });
    }

    private static float[] calculateHsl(int red, int green, int blue) {
        float[] hsl = new float[3];

        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;

        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float d = max - min;

        float h = 0f, s = 0f, l = 0f;

        if (max == min) {
            h = 0;
        }
        if (max == r) {
            h = 60 * (g - b) / d;
        } else if (max == g) {
            h = 60 * (b - r) / d + 120;
        } else if (max == b) {
            h = 60 * (r - g) / d + 240;
        }

        l = (max + min) / 2f;

        if (max == min) {
            s = 0;
        } else if (l < 0.5) {
            s = d / (2 * l);
        } else {
            s = d / (2 - 2 * l);
        }

        hsl[0] = h % 360;
        hsl[1] = s * 100;
        hsl[2] = l * 100;
        // If saturation is 0, the hue is not well defined. Use hue 0 in this
        // case.
        if (hsl[1] == 0) {
            hsl[0] = 0;
        }

        return hsl;
    }

    private static float hueToRgb(float m1, float m2, float h) {
        if (h < 0) {
            h = h + 1;
        }
        if (h > 1) {
            h = h - 1;
        }
        if (h * 6 < 1) {
            return m1 + (m2 - m1) * h * 6;
        }
        if (h * 2 < 1) {
            return m2;
        }
        if (h * 3 < 2) {
            return m1 + (m2 - m1) * (2f / 3 - h) * 6;
        }
        return m1;
    }

    public static LexicalUnitImpl createHexColor(int red, int green, int blue,
            int line, int column) {
        return LexicalUnitImpl.createIdent(line, column,
                rgbToColorString(new int[] { red, green, blue }));
    }

    public static LexicalUnitImpl createHexColor(int[] rgb, int line, int column) {
        return createHexColor(rgb[0], rgb[1], rgb[2], line, column);
    }

    public static LexicalUnitImpl createRgbaColor(int red, int green, int blue,
            float alpha, int line, int column) {
        LexicalUnitImpl redUnit = LexicalUnitImpl.createNumber(line, column,
                red);
        LexicalUnitImpl greenUnit = LexicalUnitImpl.createNumber(line, column,
                green);
        LexicalUnitImpl blueUnit = LexicalUnitImpl.createNumber(line, column,
                blue);
        LexicalUnitImpl alphaUnit = LexicalUnitImpl.createNumber(line, column,
                alpha);
        ActualArgumentList args = new ActualArgumentList(
                SassList.Separator.COMMA, redUnit, greenUnit, blueUnit,
                alphaUnit);
        return LexicalUnitImpl.createFunction(line, column, "rgba", args);
    }

    public static LexicalUnitImpl createHslaColor(float hue, float saturation,
            float lightness, float alpha, int line, int column) {
        LexicalUnitImpl hueUnit = LexicalUnitImpl.createNumber(line, column,
                hue);
        LexicalUnitImpl saturationUnit = LexicalUnitImpl.createPercentage(line,
                column, saturation);
        LexicalUnitImpl lightnessUnit = LexicalUnitImpl.createPercentage(line,
                column, lightness);
        LexicalUnitImpl alphaUnit = LexicalUnitImpl.createNumber(line, column,
                alpha);
        ActualArgumentList args = new ActualArgumentList(
                SassList.Separator.COMMA, hueUnit, saturationUnit,
                lightnessUnit, alphaUnit);
        return LexicalUnitImpl.createFunction(line, column, "hsla", args);
    }

    public static LexicalUnitImpl createHslaOrHslColor(float[] hsl,
            float alpha, int line, int column) {
        if (alpha < 1.0f) {
            return createHslaColor(hsl[0], hsl[1], hsl[2], alpha, line, column);
        } else {
            return createHslFunction(hsl[0], hsl[1], hsl[2], line, column);
        }
    }

    /**
     * Creates a hex color if alpha is equal to one. Otherwise creates an RGBA
     * color.
     * 
     * @return An object representing a color.
     */
    public static LexicalUnitImpl createRgbaOrHexColor(int[] rgb, float alpha,
            int line, int column) {
        if (alpha < 1.0f) {
            return createRgbaColor(rgb[0], rgb[1], rgb[2], alpha, line, column);
        } else {
            return createHexColor(rgb, line, column);
        }
    }

    private static LexicalUnitImpl createHslFunction(float hue,
            float saturation, float lightness, int ln, int cn) {
        LexicalUnitImpl hueUnit = LexicalUnitImpl.createNumber(ln, cn, hue);
        LexicalUnitImpl saturationUnit = LexicalUnitImpl.createPercentage(ln,
                cn, saturation);
        LexicalUnitImpl lightnessUnit = LexicalUnitImpl.createPercentage(ln,
                cn, lightness);
        ActualArgumentList hslParams = new ActualArgumentList(Separator.COMMA,
                hueUnit, saturationUnit, lightnessUnit);
        return LexicalUnitImpl.createFunction(ln, cn, "hsl", hslParams);
    }

    private static LexicalUnitImpl adjust(LexicalUnitImpl color,
            float amountByPercent, ColorOperation op) {

        float[] hsl = colorToHsl(color);
        if (op == ColorOperation.Darken) {
            hsl[2] = hsl[2] - amountByPercent;
            hsl[2] = hsl[2] < 0 ? 0 : hsl[2];
        } else if (op == ColorOperation.Lighten) {
            hsl[2] = hsl[2] + amountByPercent;
            hsl[2] = hsl[2] > 100 ? 100 : hsl[2];
        }
        float alpha = getAlpha(color);
        return createHslaOrHslColor(hsl, alpha, color.getLineNumber(),
                color.getColumnNumber());
    }

    public static LexicalUnitImpl darken(LexicalUnitImpl color, float amount) {
        return adjust(color, amount, ColorOperation.Darken);
    }

    public static LexicalUnitImpl lighten(LexicalUnitImpl color, float amount) {
        return adjust(color, amount, ColorOperation.Lighten);
    }

    private static float getAmountValue(ActualArgumentList params) {
        float amount = 10f;
        if (params.size() > 1) {
            amount = params.get(1).getContainedValue().getFloatValue();
        }
        return amount;
    }

    private enum ColorOperation {
        Darken, Lighten
    }
}
