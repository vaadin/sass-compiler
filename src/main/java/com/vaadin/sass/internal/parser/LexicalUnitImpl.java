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
/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: LexicalUnitImpl.java,v 1.3 2000/02/15 02:08:19 plehegar Exp $
 */
package com.vaadin.sass.internal.parser;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.css.sac.LexicalUnit;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.expression.exception.IncompatibleUnitsException;
import com.vaadin.sass.internal.parser.function.AbsFunctionGenerator;
import com.vaadin.sass.internal.parser.function.AdjustColorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.AlphaFunctionGenerator;
import com.vaadin.sass.internal.parser.function.CeilFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ColorComponentFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DarkenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DefaultFunctionGenerator;
import com.vaadin.sass.internal.parser.function.FloorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.IfFunctionGenerator;
import com.vaadin.sass.internal.parser.function.LightenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListAppendFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListJoinFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListLengthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListNthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.MinMaxFunctionGenerator;
import com.vaadin.sass.internal.parser.function.PercentageFunctionGenerator;
import com.vaadin.sass.internal.parser.function.QuoteUnquoteFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RGBFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RectFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RoundFunctionGenerator;
import com.vaadin.sass.internal.parser.function.SCSSFunctionGenerator;
import com.vaadin.sass.internal.parser.function.TransparencyModificationFunctionGenerator;
import com.vaadin.sass.internal.parser.function.TypeOfFunctionGenerator;
import com.vaadin.sass.internal.parser.function.UnitFunctionGenerator;
import com.vaadin.sass.internal.parser.function.UnitlessFunctionGenerator;
import com.vaadin.sass.internal.tree.FunctionDefNode;
import com.vaadin.sass.internal.tree.FunctionNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.util.ColorUtil;
import com.vaadin.sass.internal.util.StringUtil;

/**
 * @version $Revision: 1.3 $
 * @author Philippe Le Hegaret
 * 
 * @modified Sebastian Nyholm @ Vaadin Ltd
 */
public class LexicalUnitImpl implements LexicalUnit, SCSSLexicalUnit,
        SassListItem, Serializable {
    private static final long serialVersionUID = -6649833716809789399L;

    public static final long PRECISION = 100000L;

    private static final DecimalFormat CSS_FLOAT_FORMAT = new DecimalFormat(
            "0.0####");

    private short type;
    private int line;
    private int column;

    private int i;
    private float f;
    private short dimension;
    private String sdimension;
    private StringInterpolationSequence s;
    private String fname;
    private ActualArgumentList params;

    LexicalUnitImpl(short type, int line, int column) {
        this.line = line;
        this.column = column - 1;
        this.type = type;
    }

    LexicalUnitImpl(int line, int column, short dimension, String sdimension,
            float f) {
        this(dimension, line, column);
        this.f = f;
        i = (int) f;
        this.dimension = dimension;
        this.sdimension = sdimension;
    }

    LexicalUnitImpl(int line, int column, short dimension, String sdimension,
            int i) {
        this(dimension, line, column);
        this.dimension = dimension;
        this.i = i;
        f = i;
    }

    LexicalUnitImpl(int line, int column, short type, String s) {
        this(line, column, type, new StringInterpolationSequence(s));
    }

    LexicalUnitImpl(int line, int column, short type,
            StringInterpolationSequence s) {
        this(type, line, column);
        this.s = s;
    }

    LexicalUnitImpl(short type, int line, int column, String fname,
            ActualArgumentList params) {
        this(type, line, column);
        this.fname = fname;
        this.params = params;
    }

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return column;
    }

    @Override
    public short getLexicalUnitType() {
        return type;
    }

    private void setLexicalUnitType(short type) {
        this.type = type;
    }

    @Override
    @Deprecated
    public LexicalUnitImpl getNextLexicalUnit() {
        return null;
    }

    @Override
    @Deprecated
    public LexicalUnitImpl getPreviousLexicalUnit() {
        return null;
    }

    public boolean isNumber() {
        short type = getLexicalUnitType();
        switch (type) {
        case LexicalUnit.SAC_INTEGER:
        case LexicalUnit.SAC_REAL:
        case LexicalUnit.SAC_EM:
        case SCSSLexicalUnit.SAC_LEM:
        case SCSSLexicalUnit.SAC_REM:
        case LexicalUnit.SAC_EX:
        case LexicalUnit.SAC_PIXEL:
        case LexicalUnit.SAC_INCH:
        case LexicalUnit.SAC_CENTIMETER:
        case LexicalUnit.SAC_MILLIMETER:
        case LexicalUnit.SAC_POINT:
        case LexicalUnit.SAC_PICA:
        case LexicalUnit.SAC_PERCENTAGE:
        case LexicalUnit.SAC_DEGREE:
        case LexicalUnit.SAC_GRADIAN:
        case LexicalUnit.SAC_RADIAN:
        case LexicalUnit.SAC_MILLISECOND:
        case LexicalUnit.SAC_SECOND:
        case LexicalUnit.SAC_HERTZ:
        case LexicalUnit.SAC_KILOHERTZ:
        case LexicalUnit.SAC_DIMENSION:
            return true;
        default:
            return false;
        }
    }

    @Override
    public int getIntegerValue() {
        return i;
    }

    private void setIntegerValue(int i) {
        this.i = i;
        f = i;
    }

    @Override
    public float getFloatValue() {
        return f;
    }

    /**
     * Returns the float value as a string unless the value is an integer. In
     * that case returns the integer value as a string.
     * 
     * @return a string representing the value, either with or without decimals
     */
    public String getFloatOrInteger() {
        float f = getFloatValue();
        int i = (int) f;
        if ((i) == f) {
            return i + "";
        } else {
            return CSS_FLOAT_FORMAT.format(f);
        }
    }

    private void setFloatValue(float f) {
        this.f = f;
        i = (int) f;
    }

    @Override
    public String getDimensionUnitText() {
        switch (type) {
        case SAC_INTEGER:
        case SAC_REAL:
            return "";
        case SAC_PERCENTAGE:
            return "%";
        case SAC_EM:
            return "em";
        case SCSSLexicalUnit.SAC_LEM:
            return "lem";
        case SCSSLexicalUnit.SAC_REM:
            return "rem";
        case SAC_EX:
            return "ex";
        case SAC_PIXEL:
            return "px";
        case SAC_CENTIMETER:
            return "cm";
        case SAC_MILLIMETER:
            return "mm";
        case SAC_INCH:
            return "in";
        case SAC_POINT:
            return "pt";
        case SAC_PICA:
            return "pc";
        case SAC_DEGREE:
            return "deg";
        case SAC_RADIAN:
            return "rad";
        case SAC_GRADIAN:
            return "grad";
        case SAC_MILLISECOND:
            return "ms";
        case SAC_SECOND:
            return "s";
        case SAC_HERTZ:
            return "Hz";
        case SAC_KILOHERTZ:
            return "kHz";
        case SAC_DIMENSION:
            return sdimension;
        default:
            throw new IllegalStateException("invalid dimension " + type);
        }
    }

    public String getStringValue() {
        return s == null ? null : s.toString();
    }

    private void setStringValue(String str) {
        s = new StringInterpolationSequence(str);
    }

    @Override
    public String getFunctionName() {
        return fname;
    }

    @Override
    public LexicalUnitImpl getParameters() {
        // use getParameterList() instead
        return null;
    }

    public ActualArgumentList getParameterList() {
        return params;
    }

    @Override
    public LexicalUnitImpl getSubValues() {
        // should not be used, this method is only here because of an
        // implemented interface
        return null;
    }

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * Result value could be null.
     * 
     * @return State as a string
     */
    public String printState() {
        return buildString(Node.PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        String result = simpleAsString();
        if (result == null) {
            return "Lexical unit node [" + buildString(Node.TO_STRING_STRATEGY)
                    + "]";
        } else {
            return result;
        }
    }

    // A helper method for sass interpolation
    @Override
    public String unquotedString() {
        String result = printState();
        if (result.length() >= 2
                && ((result.charAt(0) == '"' && result
                        .charAt(result.length() - 1) == '"') || (result
                        .charAt(0) == '\'' && result
                        .charAt(result.length() - 1) == '\''))) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    public LexicalUnitImpl divide(LexicalUnitImpl denominator) {
        if (denominator.getLexicalUnitType() != SAC_INTEGER
                && denominator.getLexicalUnitType() != SAC_REAL
                && getLexicalUnitType() != denominator.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                / denominator.getFloatValue());
        if (getLexicalUnitType() == denominator.getLexicalUnitType()) {
            copy.setLexicalUnitType(SAC_REAL);
        }
        return copy;
    }

    public LexicalUnitImpl add(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                + another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl minus(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                - another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl multiply(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                * another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    protected short checkAndGetUnit(LexicalUnitImpl another) {
        if (getLexicalUnitType() != SAC_INTEGER
                && getLexicalUnitType() != SAC_REAL
                && another.getLexicalUnitType() != SAC_INTEGER
                && another.getLexicalUnitType() != SAC_REAL
                && getLexicalUnitType() != another.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        if (another.getLexicalUnitType() != SAC_INTEGER
                && another.getLexicalUnitType() != SAC_REAL) {
            return another.getLexicalUnitType();
        }
        return getLexicalUnitType();
    }

    public LexicalUnitImpl modulo(LexicalUnitImpl another) {
        if (getLexicalUnitType() != another.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        LexicalUnitImpl copy = copy();
        copy.setIntegerValue(getIntegerValue() % another.getIntegerValue());
        return copy;
    }

    /**
     * Returns a shallow copy of the {@link LexicalUnitImpl} with null as next
     * lexical unit pointer. Parameters are not copied but a reference to the
     * same parameter list is used.
     * 
     * @return copy of this without next
     */
    public LexicalUnitImpl copy() {
        LexicalUnitImpl copy = new LexicalUnitImpl(type, line, column);
        copy.i = i;
        copy.f = f;
        copy.s = s;
        copy.fname = fname;
        copy.dimension = dimension;
        copy.sdimension = sdimension;
        copy.params = params;
        return copy;
    }

    public LexicalUnitImpl copyWithValue(float value) {
        LexicalUnitImpl result = copy();
        result.setFloatValue(value);
        return result;
    }

    private void setParameterList(ActualArgumentList params) {
        this.params = params;
    }

    public short getDimension() {
        return dimension;
    }

    public String getSdimension() {
        return sdimension;
    }

    // here some useful function for creation
    public static LexicalUnitImpl createVariable(int line, int column,
            String name) {
        return new LexicalUnitImpl(line, column, SCSS_VARIABLE, name);
    }

    public static LexicalUnitImpl createNull(int line, int column) {
        return new LexicalUnitImpl(line, column, SCSS_NULL, "null");
    }

    public static LexicalUnitImpl createNumber(int line, int column, float v) {
        int i = (int) v;
        if (v == i) {
            return new LexicalUnitImpl(line, column, SAC_INTEGER, "", i);
        } else {
            return new LexicalUnitImpl(line, column, SAC_REAL, "", v);
        }
    }

    public static LexicalUnitImpl createInteger(int line, int column, int i) {
        return new LexicalUnitImpl(line, column, SAC_INTEGER, "", i);
    }

    public static LexicalUnitImpl createPercentage(int line, int column, float v) {
        LexicalUnitImpl result = new LexicalUnitImpl(line, column,
                SAC_PERCENTAGE, null, v);

        if (Math.round(v * 100 * PRECISION) == (((int) v) * 100 * PRECISION)) {
            result.setIntegerValue((int) v);
        }

        return result;
    }

    static LexicalUnitImpl createEMS(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_EM, null, v);
    }

    static LexicalUnitImpl createLEM(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SCSSLexicalUnit.SAC_LEM, null,
                v);
    }

    static LexicalUnitImpl createREM(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SCSSLexicalUnit.SAC_REM, null,
                v);
    }

    static LexicalUnitImpl createEXS(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_EX, null, v);
    }

    public static LexicalUnitImpl createPX(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_PIXEL, null, v);
    }

    public static LexicalUnitImpl createCM(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_CENTIMETER, null, v);
    }

    static LexicalUnitImpl createMM(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_MILLIMETER, null, v);
    }

    static LexicalUnitImpl createIN(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_INCH, null, v);
    }

    static LexicalUnitImpl createPT(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_POINT, null, v);
    }

    static LexicalUnitImpl createPC(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_PICA, null, v);
    }

    public static LexicalUnitImpl createDEG(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_DEGREE, null, v);
    }

    static LexicalUnitImpl createRAD(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_RADIAN, null, v);
    }

    static LexicalUnitImpl createGRAD(int line, int column, float v) {
        return new LexicalUnitImpl(line, column, SAC_GRADIAN, null, v);
    }

    static LexicalUnitImpl createMS(int line, int column, float v) {
        if (v < 0) {
            throw new ParseException("Time values may not be negative", line,
                    column);
        }
        return new LexicalUnitImpl(line, column, SAC_MILLISECOND, null, v);
    }

    static LexicalUnitImpl createS(int line, int column, float v) {
        if (v < 0) {
            throw new ParseException("Time values may not be negative", line,
                    column);
        }
        return new LexicalUnitImpl(line, column, SAC_SECOND, null, v);
    }

    static LexicalUnitImpl createHZ(int line, int column, float v) {
        if (v < 0) {
            throw new ParseException("Frequency values may not be negative",
                    line, column);
        }
        return new LexicalUnitImpl(line, column, SAC_HERTZ, null, v);
    }

    static LexicalUnitImpl createKHZ(int line, int column, float v) {
        if (v < 0) {
            throw new ParseException("Frequency values may not be negative",
                    line, column);
        }
        return new LexicalUnitImpl(line, column, SAC_KILOHERTZ, null, v);
    }

    static LexicalUnitImpl createDimen(int line, int column, float v, String s) {
        return new LexicalUnitImpl(line, column, SAC_DIMENSION, s, v);
    }

    static LexicalUnitImpl createInherit(int line, int column) {
        return new LexicalUnitImpl(line, column, SAC_INHERIT, "inherit");
    }

    public static LexicalUnitImpl createRawIdent(int line, int column, String s) {
        return new LexicalUnitImpl(line, column, SAC_IDENT, s);
    }

    public static LexicalUnitImpl createIdent(int line, int column, String s) {
        return createIdent(line, column, new StringInterpolationSequence(s));
    }

    public static LexicalUnitImpl createIdent(int line, int column,
            StringInterpolationSequence s) {
        if ("null".equals(s.toString())) {
            return createNull(line, column);
        }
        return new LexicalUnitImpl(line, column, SAC_IDENT, s);
    }

    public static LexicalUnitImpl createString(String s) {
        return new LexicalUnitImpl(0, 0, SAC_STRING_VALUE, s);
    }

    public static LexicalUnitImpl createString(int line, int column, String s) {
        return new LexicalUnitImpl(line, column, SAC_STRING_VALUE, s);
    }

    static LexicalUnitImpl createURL(int line, int column, String s) {
        return new LexicalUnitImpl(line, column, SAC_URI, s);
    }

    public static LexicalUnitImpl createAttr(int line, int column, String s) {
        return new LexicalUnitImpl(line, column, SAC_ATTR, s);
    }

    public static LexicalUnitImpl createRGBColor(int line, int column,
            ActualArgumentList params) {
        return new LexicalUnitImpl(SAC_RGBCOLOR, line, column, "rgb", params);
    }

    public static LexicalUnitImpl createRect(int line, int column,
            ActualArgumentList params) {
        return new LexicalUnitImpl(SAC_RECT_FUNCTION, line, column, "rect",
                params);
    }

    public static LexicalUnitImpl createFunction(int line, int column,
            String fname, ActualArgumentList params) {
        return new LexicalUnitImpl(SAC_FUNCTION, line, column, fname, params);
    }

    public static boolean checkLexicalUnitType(SassListItem item,
            short... lexicalUnitTypes) {
        if (!(item instanceof LexicalUnitImpl)) {
            return false;
        }
        for (short s : lexicalUnitTypes) {
            if (((LexicalUnitImpl) item).getLexicalUnitType() == s) {
                return true;
            }
        }
        return false;
    }

    public static LexicalUnitImpl createUnicodeRange(int line, int column,
            SassList params) {
        // @@ return new LexicalUnitImpl(line, column, previous, null,
        // SAC_UNICODERANGE, params);
        return null;
    }

    public static LexicalUnitImpl createComma(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_COMMA, line, column);
    }

    public static LexicalUnitImpl createSpace(int line, int column) {
        return new LexicalUnitImpl(line, column, SAC_IDENT, " ");
    }

    public static LexicalUnitImpl createSlash(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_SLASH, line, column);
    }

    public static LexicalUnitImpl createAdd(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_PLUS, line, column);
    }

    public static LexicalUnitImpl createMinus(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_MINUS, line, column);
    }

    public static LexicalUnitImpl createMultiply(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_MULTIPLY, line, column);
    }

    public static LexicalUnitImpl createModulo(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_MOD, line, column);
    }

    public static LexicalUnitImpl createLeftParenthesis(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_LEFT_PAREN, line, column);
    }

    public static LexicalUnitImpl createRightParenthesis(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_RIGHT_PAREN, line, column);
    }

    public static LexicalUnitImpl createIdent(String s) {
        return new LexicalUnitImpl(0, 0, SAC_IDENT, s);
    }

    public static LexicalUnitImpl createEquals(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_EQUALS, line, column);
    }

    public static LexicalUnitImpl createNotEqual(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_NOT_EQUAL, line, column);
    }

    public static LexicalUnitImpl createGreaterThan(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_GT, line, column);
    }

    public static LexicalUnitImpl createGreaterThanOrEqualTo(int line,
            int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_GE, line, column);
    }

    public static LexicalUnitImpl createLessThan(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_LT, line, column);
    }

    public static LexicalUnitImpl createLessThanOrEqualTo(int line, int column) {
        return new LexicalUnitImpl(SAC_OPERATOR_LE, line, column);
    }

    public static LexicalUnitImpl createAnd(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_AND, line, column);
    }

    public static LexicalUnitImpl createOr(int line, int column) {
        return new LexicalUnitImpl(SCSS_OPERATOR_OR, line, column);
    }

    @Override
    public SassListItem replaceVariables(Collection<VariableNode> variables) {
        // TODO simplify
        SassListItem result = replaceVariablesForStringValue(variables);
        Iterator<VariableNode> it = variables.iterator();
        while (it.hasNext() && result instanceof LexicalUnitImpl) {
            LexicalUnitImpl lui = (LexicalUnitImpl) result;
            result = lui.replaceVariable(it.next());
        }
        if (!(result instanceof LexicalUnitImpl)) {
            result = result.replaceVariables(variables);
        }
        return result;
    }

    private LexicalUnitImpl replaceVariablesForStringValue(
            Collection<VariableNode> variables) {
        if (s == null || !s.containsInterpolation()) {
            return this;
        } else {
            LexicalUnitImpl copy = copy();
            copy.s = s.replaceVariables(variables);
            return copy;
        }
    }

    private SassListItem replaceVariable(VariableNode node) {
        LexicalUnitImpl replacementUnit = this;
        replacementUnit = replaceParams(replacementUnit, node);
        SassListItem replacement = replaceVariable(replacementUnit, node);
        replacement = replaceInterpolation(replacement, node);
        return replacement;
    }

    private static SassListItem replaceVariable(LexicalUnitImpl unit,
            VariableNode node) {
        if (unit.getLexicalUnitType() == LexicalUnitImpl.SCSS_VARIABLE
                && node.getName().equals(unit.getStringValue())) {
            return node.getExpr();
        } else {
            return unit;
        }
    }

    private static LexicalUnitImpl replaceParams(LexicalUnitImpl item,
            VariableNode node) {
        ActualArgumentList params = item.getParameterList();
        if (params != null) {
            ActualArgumentList newParams = params.replaceVariables(Collections
                    .singletonList(node));
            // TODO both copy and setParameterList set the parameter list -
            // could optimize
            LexicalUnitImpl copy = item.copy();
            copy.setParameterList(newParams);
            return copy;
        } else {
            return item;
        }
    }

    private static SassListItem replaceInterpolation(SassListItem item,
            VariableNode node) {
        if (item instanceof LexicalUnitImpl) {
            LexicalUnitImpl unit = (LexicalUnitImpl) item;
            String interpolation = "#{$" + node.getName() + "}";
            String stringValue = unit.getStringValue();
            if (stringValue != null) {
                SassListItem expr = node.getExpr();
                // strings should be unquoted
                if (stringValue.equals(interpolation)
                        && !checkLexicalUnitType(expr,
                                LexicalUnitImpl.SAC_STRING_VALUE)) {
                    // no more replacements needed, use data type of expr
                    return expr;
                } else if (stringValue.contains(interpolation)) {
                    LexicalUnitImpl copy = unit.copy();
                    copy.setStringValue(stringValue.replaceAll(
                            Pattern.quote(interpolation),
                            Matcher.quoteReplacement(expr.unquotedString())));
                    item = copy;
                }
            }
        }
        return item;
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions(
            boolean evaluateArithmetics) {
        if (params != null) {
            SCSSFunctionGenerator generator = getGenerator(getFunctionName());
            LexicalUnitImpl copy = this;
            if (!"if".equals(getFunctionName())) {
                copy = createFunction(line, column, fname,
                        params.evaluateFunctionsAndExpressions(true));
            }
            if (generator == null) {
                SassListItem result = copy.replaceCustomFunctions();
                if (result != null) {
                    return result;
                }
            }
            if (generator == null) {
                generator = DEFAULT_SERIALIZER;
            }
            return generator.compute(copy);
        } else {
            return this;
        }
    }

    private SassListItem replaceCustomFunctions() {
        FunctionDefNode functionDef = ScssStylesheet
                .getFunctionDefinition(getFunctionName());
        if (functionDef != null) {
            FunctionNode node = new FunctionNode(functionDef, this);
            return node.evaluate();
        }
        return null;
    }

    private static SCSSFunctionGenerator getGenerator(String funcName) {
        return SERIALIZERS.get(funcName);
    }

    private static List<SCSSFunctionGenerator> initSerializers() {
        List<SCSSFunctionGenerator> list = new LinkedList<SCSSFunctionGenerator>();
        list.add(new AbsFunctionGenerator());
        list.add(new AdjustColorFunctionGenerator());
        list.add(new CeilFunctionGenerator());
        list.add(new DarkenFunctionGenerator());
        list.add(new FloorFunctionGenerator());
        list.add(new IfFunctionGenerator());
        list.add(new LightenFunctionGenerator());
        list.add(new ListAppendFunctionGenerator());
        list.add(new ListJoinFunctionGenerator());
        list.add(new ListLengthFunctionGenerator());
        list.add(new ListNthFunctionGenerator());
        list.add(new MinMaxFunctionGenerator());
        list.add(new PercentageFunctionGenerator());
        list.add(new RectFunctionGenerator());
        list.add(new RGBFunctionGenerator());
        list.add(new RoundFunctionGenerator());
        list.add(new TypeOfFunctionGenerator());
        list.add(new AlphaFunctionGenerator());
        list.add(new TransparencyModificationFunctionGenerator());
        list.add(new ColorComponentFunctionGenerator());
        list.add(new UnitFunctionGenerator());
        list.add(new UnitlessFunctionGenerator());
        list.add(new QuoteUnquoteFunctionGenerator());
        return list;
    }

    private static final Map<String, SCSSFunctionGenerator> SERIALIZERS = new HashMap<String, SCSSFunctionGenerator>();

    private static final SCSSFunctionGenerator DEFAULT_SERIALIZER = new DefaultFunctionGenerator();

    private String simpleAsString() {
        short type = getLexicalUnitType();
        String text = null;
        switch (type) {
        case SCSS_VARIABLE:
            text = "$" + s;
            break;
        case SCSS_NULL:
            text = "";
            break;
        case LexicalUnit.SAC_OPERATOR_COMMA:
            text = ",";
            break;
        case LexicalUnit.SAC_OPERATOR_PLUS:
            text = "+";
            break;
        case LexicalUnit.SAC_OPERATOR_MINUS:
            text = "-";
            break;
        case LexicalUnit.SAC_OPERATOR_MULTIPLY:
            text = "*";
            break;
        case LexicalUnit.SAC_OPERATOR_SLASH:
            text = "/";
            break;
        case LexicalUnit.SAC_OPERATOR_MOD:
            text = "%";
            break;
        case LexicalUnit.SAC_OPERATOR_EXP:
            text = "^";
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_LEFT_PAREN:
            text = "(";
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_RIGHT_PAREN:
            text = ")";
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_EQUALS:
            text = "==";
            break;
        case LexicalUnitImpl.SCSS_OPERATOR_NOT_EQUAL:
            text = "!=";
            break;
        case LexicalUnit.SAC_OPERATOR_LT:
            text = "<";
            break;
        case LexicalUnit.SAC_OPERATOR_GT:
            text = ">";
            break;
        case LexicalUnit.SAC_OPERATOR_LE:
            text = "<=";
            break;
        case LexicalUnit.SAC_OPERATOR_GE:
            text = "=>";
            break;
        case LexicalUnit.SAC_OPERATOR_TILDE:
            text = "~";
            break;
        case LexicalUnit.SAC_INHERIT:
            text = "inherit";
            break;
        case LexicalUnit.SAC_INTEGER:
            text = Integer.toString(getIntegerValue(), 10);
            break;
        case LexicalUnit.SAC_REAL:
            text = getFloatOrInteger();
            break;
        case LexicalUnit.SAC_EM:
        case SCSSLexicalUnit.SAC_LEM:
        case SCSSLexicalUnit.SAC_REM:
        case LexicalUnit.SAC_EX:
        case LexicalUnit.SAC_PIXEL:
        case LexicalUnit.SAC_INCH:
        case LexicalUnit.SAC_CENTIMETER:
        case LexicalUnit.SAC_MILLIMETER:
        case LexicalUnit.SAC_POINT:
        case LexicalUnit.SAC_PICA:
        case LexicalUnit.SAC_PERCENTAGE:
        case LexicalUnit.SAC_DEGREE:
        case LexicalUnit.SAC_GRADIAN:
        case LexicalUnit.SAC_RADIAN:
        case LexicalUnit.SAC_MILLISECOND:
        case LexicalUnit.SAC_SECOND:
        case LexicalUnit.SAC_HERTZ:
        case LexicalUnit.SAC_KILOHERTZ:
        case LexicalUnit.SAC_DIMENSION:
            text = getFloatOrInteger() + getDimensionUnitText();
            break;
        }
        return text;
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        short type = getLexicalUnitType();
        String text = simpleAsString();
        if (text == null) {
            switch (type) {
            case LexicalUnit.SAC_URI:
                text = "url(" + getStringValue() + ")";
                break;
            case LexicalUnit.SAC_RGBCOLOR:
                int[] rgb = getRgb();
                if (rgb != null) {
                    text = ColorUtil.rgbToColorString(rgb);
                    break;
                }
                // else fall through to the function branch
            case LexicalUnit.SAC_COUNTER_FUNCTION:
            case LexicalUnit.SAC_COUNTERS_FUNCTION:
            case LexicalUnit.SAC_RECT_FUNCTION:
            case LexicalUnit.SAC_FUNCTION:
                if (ColorUtil.isColor(this)) {
                    text = ColorUtil.rgbToColorString(ColorUtil
                            .colorToRgb(this));
                } else {
                    text = fname + "(" + params.buildString(strategy) + ")";
                }
                break;
            case LexicalUnit.SAC_IDENT:
                text = getStringValue();
                break;
            case LexicalUnit.SAC_STRING_VALUE:
                // @@SEEME. not exact
                text = "\"" + getStringValue() + "\"";
                break;
            case LexicalUnit.SAC_ATTR:
                text = "attr(" + getStringValue() + ")";
                break;
            case LexicalUnit.SAC_UNICODERANGE:
                text = "@@TODO";
                break;
            case LexicalUnit.SAC_SUB_EXPRESSION:
                text = strategy.build(getParameterList());
                break;
            default:
                text = "@unknown";
                break;
            }
        }
        return text;
    }

    private int[] getRgb() {
        if (params.size() != 3
                || !checkLexicalUnitType(params.get(0), SAC_INTEGER)
                || !checkLexicalUnitType(params.get(1), SAC_INTEGER)
                || !checkLexicalUnitType(params.get(2), SAC_INTEGER)) {
            return null;
        }
        int red = ((LexicalUnit) params.get(0)).getIntegerValue();
        int green = ((LexicalUnit) params.get(1)).getIntegerValue();
        int blue = ((LexicalUnit) params.get(2)).getIntegerValue();
        return new int[] { red, green, blue };
    }

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        CSS_FLOAT_FORMAT.setDecimalFormatSymbols(symbols);
        for (SCSSFunctionGenerator serializer : initSerializers()) {
            for (String functionName : serializer.getFunctionNames()) {
                SERIALIZERS.put(functionName, serializer);
            }
        }
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    // TODO mutates this
    @Override
    public void updateUrl(String prefix) {
        if (getLexicalUnitType() == SAC_URI) {
            String path = getStringValue().replaceAll("^\"|\"$", "")
                    .replaceAll("^'|'$", "");
            if (!path.startsWith("/") && !path.contains(":")) {
                path = prefix + path;
                path = StringUtil.cleanPath(path);
                setStringValue(path);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LexicalUnitImpl)) {
            return false;
        }
        LexicalUnitImpl other = (LexicalUnitImpl) o;
        if (getLexicalUnitType() != other.getLexicalUnitType()) {
            return false;
        } else {
            return printState().equals(other.printState());
        }
    }

    @Override
    public int hashCode() {
        return printState().hashCode();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        return this;
    }

    @Override
    public boolean containsVariable() {
        return getLexicalUnitType() == SCSS_VARIABLE;
    }
}
