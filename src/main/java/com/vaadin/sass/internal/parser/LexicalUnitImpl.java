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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.css.sac.LexicalUnit;

import com.vaadin.sass.internal.expression.ArithmeticExpressionEvaluator;
import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.expression.exception.IncompatibleUnitsException;
import com.vaadin.sass.internal.parser.function.AbsFunctionGenerator;
import com.vaadin.sass.internal.parser.function.CeilFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DarkenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DefaultFunctionGenerator;
import com.vaadin.sass.internal.parser.function.FloorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.LightenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.PercentageFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RoundFunctionGenerator;
import com.vaadin.sass.internal.parser.function.SCSSFunctionGenerator;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.util.DeepCopy;
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

    LexicalUnitImpl prev;
    LexicalUnitImpl next;

    short type;
    int line;
    int column;

    int i;
    float f;
    short dimension;
    String sdimension;
    String s;
    String fname;
    SassList params;

    LexicalUnitImpl(short type, int line, int column, LexicalUnitImpl p) {
        if (p != null) {
            prev = p;
            p.next = this;
        }
        this.line = line;
        this.column = column - 1;
        this.type = type;
    }

    LexicalUnitImpl(int line, int column, LexicalUnitImpl previous, int i) {
        this(SAC_INTEGER, line, column, previous);
        this.i = i;
        f = i;
    }

    LexicalUnitImpl(int line, int column, LexicalUnitImpl previous,
            short dimension, String sdimension, float f) {
        this(dimension, line, column, previous);
        this.f = f;
        i = (int) f;
        this.dimension = dimension;
        this.sdimension = sdimension;
    }

    LexicalUnitImpl(int line, int column, LexicalUnitImpl previous, short type,
            String s) {
        this(type, line, column, previous);
        this.s = s;
    }

    LexicalUnitImpl(short type, int line, int column, LexicalUnitImpl previous,
            String fname, SassList params) {
        this(type, line, column, previous);
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

    public void setLexicalUnitType(short type) {
        this.type = type;
    }

    public void getLexicalUnitType(short type) {
        this.type = type;
    }

    @Override
    public LexicalUnitImpl getNextLexicalUnit() {
        return next;
    }

    public void setNextLexicalUnit(LexicalUnitImpl n) {
        next = n;
    }

    @Override
    public LexicalUnitImpl getPreviousLexicalUnit() {
        return prev;
    }

    @Deprecated
    public void setPrevLexicalUnit(LexicalUnitImpl n) {
        prev = n;
    }

    @Override
    public int getIntegerValue() {
        return i;
    }

    public void setIntegerValue(int i) {
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
            return f + "";
        }
    }

    public void setFloatValue(float f) {
        this.f = f;
        i = (int) f;
    }

    @Override
    public String getDimensionUnitText() {
        switch (type) {
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
        return s;
    }

    public void setStringValue(String str) {
        s = str;
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

    public SassList getParameterList() {
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

    @Override
    public LexicalUnitImpl divide(LexicalUnitImpl denominator) {
        if (denominator.getLexicalUnitType() != SAC_INTEGER
                && denominator.getLexicalUnitType() != SAC_REAL
                && getLexicalUnitType() != denominator.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        setFloatValue(getFloatValue() / denominator.getFloatValue());
        if (getLexicalUnitType() == denominator.getLexicalUnitType()) {
            setLexicalUnitType(SAC_REAL);
        }
        setNextLexicalUnit(denominator.getNextLexicalUnit());
        return this;
    }

    @Override
    public LexicalUnitImpl add(LexicalUnitImpl another) {
        checkAndSetUnit(another);
        setFloatValue(getFloatValue() + another.getFloatValue());
        return this;
    }

    @Override
    public LexicalUnitImpl minus(LexicalUnitImpl another) {
        checkAndSetUnit(another);
        setFloatValue(getFloatValue() - another.getFloatValue());
        return this;
    }

    @Override
    public LexicalUnitImpl multiply(LexicalUnitImpl another) {
        checkAndSetUnit(another);
        setFloatValue(getFloatValue() * another.getIntegerValue());
        return this;
    }

    protected void checkAndSetUnit(LexicalUnitImpl another) {
        if (getLexicalUnitType() != SAC_INTEGER
                && getLexicalUnitType() != SAC_REAL
                && another.getLexicalUnitType() != SAC_INTEGER
                && another.getLexicalUnitType() != SAC_REAL
                && getLexicalUnitType() != another.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        if (another.getLexicalUnitType() != SAC_INTEGER
                && another.getLexicalUnitType() != SAC_REAL) {
            setLexicalUnitType(another.getLexicalUnitType());
        }
        setNextLexicalUnit(another.getNextLexicalUnit());
    }

    @Override
    public LexicalUnitImpl modulo(LexicalUnitImpl another) {
        if (getLexicalUnitType() != another.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        setIntegerValue(getIntegerValue() % another.getIntegerValue());
        setNextLexicalUnit(another.getNextLexicalUnit());
        return this;
    }

    public void replaceValue(LexicalUnitImpl another) {
        // shouldn't modify 'another' directly, should only modify its copy.
        LexicalUnitImpl deepCopyAnother = (LexicalUnitImpl) DeepCopy
                .copy(another);
        type = deepCopyAnother.getLexicalUnitType();
        i = deepCopyAnother.getIntegerValue();
        f = deepCopyAnother.getFloatValue();
        s = deepCopyAnother.getStringValue();
        fname = deepCopyAnother.getFunctionName();
        prev = deepCopyAnother.getPreviousLexicalUnit();
        dimension = deepCopyAnother.getDimension();
        sdimension = deepCopyAnother.getSdimension();
        params = deepCopyAnother.getParameterList();

        LexicalUnitImpl finalNextInAnother = deepCopyAnother;
        while (finalNextInAnother.getNextLexicalUnit() != null) {
            finalNextInAnother = finalNextInAnother.getNextLexicalUnit();
        }

        finalNextInAnother.setNextLexicalUnit(next);
        next = deepCopyAnother.next;
    }

    public void setParameterList(SassList params) {
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
            LexicalUnitImpl previous, String name) {
        return new LexicalUnitImpl(line, column, previous, SCSS_VARIABLE, name);
    }

    public static LexicalUnitImpl createNull(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(line, column, previous, SCSS_NULL, "null");
    }

    public static LexicalUnitImpl createNumber(int line, int column,
            LexicalUnitImpl previous, float v) {
        int i = (int) v;
        if (v == i) {
            return new LexicalUnitImpl(line, column, previous, i);
        } else {
            return new LexicalUnitImpl(line, column, previous, SAC_REAL, "", v);
        }
    }

    public static LexicalUnitImpl createInteger(int line, int column,
            LexicalUnitImpl previous, int i) {
        return new LexicalUnitImpl(line, column, previous, i);
    }

    public static LexicalUnitImpl createPercentage(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_PERCENTAGE,
                null, v);
    }

    static LexicalUnitImpl createEMS(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_EM, null, v);
    }

    static LexicalUnitImpl createLEM(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous,
                SCSSLexicalUnit.SAC_LEM, null, v);
    }

    static LexicalUnitImpl createREM(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous,
                SCSSLexicalUnit.SAC_REM, null, v);
    }

    static LexicalUnitImpl createEXS(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_EX, null, v);
    }

    public static LexicalUnitImpl createPX(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_PIXEL, null, v);
    }

    public static LexicalUnitImpl createCM(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_CENTIMETER,
                null, v);
    }

    static LexicalUnitImpl createMM(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_MILLIMETER,
                null, v);
    }

    static LexicalUnitImpl createIN(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_INCH, null, v);
    }

    static LexicalUnitImpl createPT(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_POINT, null, v);
    }

    static LexicalUnitImpl createPC(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_PICA, null, v);
    }

    static LexicalUnitImpl createDEG(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_DEGREE, null, v);
    }

    static LexicalUnitImpl createRAD(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_RADIAN, null, v);
    }

    static LexicalUnitImpl createGRAD(int line, int column,
            LexicalUnitImpl previous, float v) {
        return new LexicalUnitImpl(line, column, previous, SAC_GRADIAN, null, v);
    }

    static LexicalUnitImpl createMS(int line, int column,
            LexicalUnitImpl previous, float v) {
        if (v < 0) {
            throw new ParseException("Time values may not be negative",
                    previous);
        }
        return new LexicalUnitImpl(line, column, previous, SAC_MILLISECOND,
                null, v);
    }

    static LexicalUnitImpl createS(int line, int column,
            LexicalUnitImpl previous, float v) {
        if (v < 0) {
            throw new ParseException("Time values may not be negative",
                    previous);
        }
        return new LexicalUnitImpl(line, column, previous, SAC_SECOND, null, v);
    }

    static LexicalUnitImpl createHZ(int line, int column,
            LexicalUnitImpl previous, float v) {
        if (v < 0) {
            throw new ParseException("Frequency values may not be negative",
                    previous);
        }
        return new LexicalUnitImpl(line, column, previous, SAC_HERTZ, null, v);
    }

    static LexicalUnitImpl createKHZ(int line, int column,
            LexicalUnitImpl previous, float v) {
        if (v < 0) {
            throw new ParseException("Frequency values may not be negative",
                    previous);
        }
        return new LexicalUnitImpl(line, column, previous, SAC_KILOHERTZ, null,
                v);
    }

    static LexicalUnitImpl createDimen(int line, int column,
            LexicalUnitImpl previous, float v, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_DIMENSION, s, v);
    }

    static LexicalUnitImpl createInherit(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(line, column, previous, SAC_INHERIT,
                "inherit");
    }

    public static LexicalUnitImpl createIdent(int line, int column,
            LexicalUnitImpl previous, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_IDENT, s);
    }

    public static LexicalUnitImpl createString(String s) {
        return new LexicalUnitImpl(0, 0, null, SAC_STRING_VALUE, s);
    }

    static LexicalUnitImpl createString(int line, int column,
            LexicalUnitImpl previous, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_STRING_VALUE, s);
    }

    static LexicalUnitImpl createURL(int line, int column,
            LexicalUnitImpl previous, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_URI, s);
    }

    static LexicalUnitImpl createAttr(int line, int column,
            LexicalUnitImpl previous, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_ATTR, s);
    }

    static LexicalUnitImpl createCounter(int line, int column,
            LexicalUnitImpl previous, SassList params) {
        return new LexicalUnitImpl(SAC_COUNTER_FUNCTION, line, column,
                previous, "counter", params);
    }

    public static LexicalUnitImpl createCounters(int line, int column,
            LexicalUnitImpl previous, SassList params) {
        return new LexicalUnitImpl(SAC_COUNTERS_FUNCTION, line, column,
                previous, "counters", params);
    }

    public static LexicalUnitImpl createRGBColor(int line, int column,
            LexicalUnitImpl previous, SassList params) {
        return new LexicalUnitImpl(SAC_RGBCOLOR, line, column, previous, "rgb",
                params);
    }

    public static LexicalUnitImpl createRect(int line, int column,
            LexicalUnitImpl previous, SassList params) {
        return new LexicalUnitImpl(SAC_RECT_FUNCTION, line, column, previous,
                "rect", params);
    }

    public static LexicalUnitImpl createFunction(int line, int column,
            LexicalUnitImpl previous, String fname, SassList params) {
        if ("rgb".equals(fname)) {
            // this is a RGB declaration (e.g. rgb(255, 50%, 0) )
            if (params.size() != 3) {
                throw new ParseException(
                        "The function rgb() requires exactly 3 parameters",
                        line, column);
            }
            for (int i = 0; i < 3; ++i) {
                SassListItem item = params.get(i);
                if (checkLexicalUnitType(item, SCSS_VARIABLE)) {
                    return new LexicalUnitImpl(SAC_FUNCTION, line, column,
                            previous, fname, params);
                }

                if (!checkLexicalUnitType(item, SAC_INTEGER, SAC_PERCENTAGE)) {
                    throw new ParseException(
                            "Invalid parameter to the function rgb(): "
                                    + item.toString(), line, column);
                }
            }
            return LexicalUnitImpl.createRGBColor(line, column, previous,
                    params);
        } else if ("counter".equals(fname)) {
            if (params.size() == 1 || params.size() == 2) {
                boolean ok = true;
                if (!checkLexicalUnitType(params.get(0), LexicalUnit.SAC_IDENT)) {
                    ok = false;
                }
                if (params.size() >= 2) {
                    if (!checkLexicalUnitType(params.get(1),
                            LexicalUnit.SAC_IDENT)) {
                        ok = false;
                    }
                }
                if (ok) {
                    return LexicalUnitImpl.createCounter(line, column,
                            previous, params);
                }
            }
        } else if ("counters".equals(fname)) {
            if (params.size() == 2 || params.size() == 3) {
                boolean ok = true;
                if (!checkLexicalUnitType(params.get(0), LexicalUnit.SAC_IDENT)) {
                    ok = false;
                }
                if (params.size() >= 2) {
                    if (!checkLexicalUnitType(params.get(1),
                            LexicalUnit.SAC_STRING_VALUE)) {
                        ok = false;
                    }
                }
                if (params.size() >= 3) {
                    if (!checkLexicalUnitType(params.get(2),
                            LexicalUnit.SAC_IDENT)) {
                        ok = false;
                    }
                }
                if (ok) {
                    return LexicalUnitImpl.createCounters(line, column,
                            previous, params);
                }
            }
        } else if ("attr".equals(fname)) {
            if (params.size() == 1
                    && checkLexicalUnitType(params.get(0),
                            LexicalUnit.SAC_IDENT)) {
                return LexicalUnitImpl.createAttr(line, column, previous,
                        ((LexicalUnitImpl) params.get(0)).getStringValue());
            }
        } else if ("rect".equals(fname)) {
            int i = 0;
            boolean ok = true;
            while (ok && i < 4 && i < params.size()) {
                SassListItem item = params.get(i);
                if (!(item instanceof LexicalUnitImpl)) {
                    throw new ParseException(
                            "Only simple values are allowed as rect() parameters: "
                                    + params);
                }
                LexicalUnitImpl lui = (LexicalUnitImpl) item;
                short luiType = lui.getLexicalUnitType();
                if (luiType == SCSSLexicalUnit.SAC_INTEGER
                        && lui.getIntegerValue() != 0) {
                    ok = false;
                } else if (luiType == SCSSLexicalUnit.SAC_IDENT
                        && !"auto".equals(lui.getStringValue())) {
                    ok = false;
                } else if ((luiType != LexicalUnit.SAC_EM)
                        && (luiType != LexicalUnit.SAC_EX)
                        && (luiType != LexicalUnit.SAC_PIXEL)
                        && (luiType != LexicalUnit.SAC_CENTIMETER)
                        && (luiType != LexicalUnit.SAC_MILLIMETER)
                        && (luiType != LexicalUnit.SAC_INCH)
                        && (luiType != LexicalUnit.SAC_POINT)
                        && (luiType != LexicalUnit.SAC_PICA)) {
                    ok = false;
                }
                i++;
            }
            if (params.size() == 4 && ok) {
                return LexicalUnitImpl.createRect(line, column, previous,
                        params);
            }
        }
        return new LexicalUnitImpl(SAC_FUNCTION, line, column, previous, fname,
                params);
    }

    private static boolean checkLexicalUnitType(SassListItem item,
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
            LexicalUnit previous, LexicalUnit params) {
        // @@ return new LexicalUnitImpl(line, column, previous, null,
        // SAC_UNICODERANGE, params);
        return null;
    }

    public static LexicalUnitImpl createComma(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_COMMA, line, column, previous);
    }

    public static LexicalUnitImpl createSlash(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_SLASH, line, column, previous);
    }

    public static LexicalUnitImpl createAdd(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_PLUS, line, column, previous);
    }

    public static LexicalUnitImpl createMinus(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_MINUS, line, column, previous);
    }

    public static LexicalUnitImpl createMultiply(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_MULTIPLY, line, column,
                previous);
    }

    public static LexicalUnitImpl createModulo(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SAC_OPERATOR_MOD, line, column, previous);
    }

    public static LexicalUnitImpl createLeftParenthesis(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SCSS_OPERATOR_LEFT_PAREN, line, column,
                previous);
    }

    public static LexicalUnitImpl createRightParenthesis(int line, int column,
            LexicalUnitImpl previous) {
        return new LexicalUnitImpl(SCSS_OPERATOR_LEFT_PAREN, line, column,
                previous);
    }

    /**
     * Tries to return the value for this {@link LexicalUnitImpl} without
     * considering any related units.
     * 
     * @return
     */
    public Object getValue() {
        if (s != null) {
            return s;
        } else if (i != -1) {
            return i;
        } else if (f != -1) {
            return f;
        } else {
            return null;
        }
    }

    public String getValueAsString() {
        Object value = getValue();
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    public void setFunctionName(String functionName) {
        fname = functionName;
    }

    public static LexicalUnitImpl createIdent(String s) {
        return new LexicalUnitImpl(0, 0, null, SAC_IDENT, s);
    }

    public static void replaceValues(LexicalUnitImpl unit,
            LexicalUnitImpl replaceWith) {
        unit.setLexicalUnitType(replaceWith.getLexicalUnitType());
        unit.setStringValue(replaceWith.getStringValue());
        unit.setIntegerValue(replaceWith.getIntegerValue());
        unit.setFloatValue(replaceWith.getFloatValue());
        unit.setFunctionName(replaceWith.getFunctionName());

        if (replaceWith.getParameterList() != null) {
            unit.setParameterList(replaceWith.getParameterList());
        }

    }

    @Override
    public SassListItem replaceVariables(Collection<VariableNode> variables) {
        // TODO remove when LUI is immutable
        SassListItem result = (LexicalUnitImpl) DeepCopy.copy(this);
        Iterator<VariableNode> it = variables.iterator();
        while (it.hasNext() && result instanceof LexicalUnitImpl) {
            LexicalUnitImpl lui = (LexicalUnitImpl) result;
            result = lui.replaceVariable(lui, it.next());
        }
        if (!(result instanceof LexicalUnitImpl)) {
            result = result.replaceVariables(variables);
        }
        return result;
    }

    private SassListItem replaceVariable(LexicalUnitImpl lexUnit,
            VariableNode node) {
        SassListItem replacement = lexUnit;
        String interpolation = "#{$" + node.getName() + "}";
        SassList params = lexUnit.getParameterList();
        if (params != null) {
            SassList newParams = params.replaceVariables(Collections
                    .singletonList(node));
            lexUnit.setParameterList(newParams);
        }
        String stringValue = lexUnit.getStringValue();
        if (lexUnit.getLexicalUnitType() == LexicalUnitImpl.SCSS_VARIABLE
                && node.getName().equals(stringValue)) {
            SassListItem expr = node.getExpr();
            if (!(expr instanceof LexicalUnitImpl)) {
                // Check that the replacement does not break any links
                if (lexUnit.getPreviousLexicalUnit() != null
                        || lexUnit.getNextLexicalUnit() != null
                        || lexUnit.getParameters() != null) {
                    throw new ParseException(
                            "Expected a single value for a variable, actual value was a list. "
                                    + "Variable: " + lexUnit.toString()
                                    + ". : " + expr.toString(), lexUnit);
                }
                replacement = (SassListItem) DeepCopy.copy(expr);
            } else { // expr is a LexicalUnitImpl, replace the values of lexUnit
                replaceValues(lexUnit, (LexicalUnitImpl) expr);
            }
        } else if (stringValue != null && stringValue.contains(interpolation)) {
            replaceInterpolation(lexUnit, node);
        }
        if (lexUnit.next != null) {
            // If the replacement of the next unit succeeds, the call returns
            // a LexicalUnitImpl.
            lexUnit.setNextLexicalUnit((LexicalUnitImpl) replaceVariable(
                    lexUnit.next, node));
        }
        return replacement;
    }

    public void replaceInterpolation(LexicalUnitImpl unit, VariableNode node) {
        String interpolation = "#{$" + node.getName() + "}";
        while (unit != null) {
            if (unit.getValueAsString().contains(interpolation)) {
                unit.setStringValue(unit.getValueAsString().replaceAll(
                        Pattern.quote(interpolation),
                        node.getExpr().unquotedString()));
            }
            unit = unit.getNextLexicalUnit();
        }
    }

    @Override
    public SassListItem replaceFunctions() {
        if (params != null) {
            LexicalUnitImpl copy = createFunction(line, column, prev, fname,
                    params.replaceFunctions());
            SCSSFunctionGenerator generator = getGenerator(getFunctionName());
            return generator.compute(copy);
        }
        return this;
    }

    private static SCSSFunctionGenerator getGenerator(String funcName) {
        SCSSFunctionGenerator serializer = SERIALIZERS.get(funcName);
        if (serializer == null) {
            return DEFAULT_SERIALIZER;
        } else {
            return serializer;
        }
    }

    private static List<SCSSFunctionGenerator> initSerializers() {
        List<SCSSFunctionGenerator> list = new LinkedList<SCSSFunctionGenerator>();
        list.add(new AbsFunctionGenerator());
        list.add(new CeilFunctionGenerator());
        list.add(new DarkenFunctionGenerator());
        list.add(new FloorFunctionGenerator());
        list.add(new LightenFunctionGenerator());
        list.add(new RoundFunctionGenerator());
        list.add(new PercentageFunctionGenerator());
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
            case LexicalUnit.SAC_COUNTER_FUNCTION:
            case LexicalUnit.SAC_COUNTERS_FUNCTION:
            case LexicalUnit.SAC_RECT_FUNCTION:
            case LexicalUnit.SAC_FUNCTION:
                text = fname + "(" + params.buildString(strategy) + ")";
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
        if (getNextLexicalUnit() != null) {
            if (getNextLexicalUnit().getLexicalUnitType() == SAC_OPERATOR_COMMA) {
                return text + strategy.build(getNextLexicalUnit());
            }
            return text + ' ' + strategy.build(getNextLexicalUnit());
        } else {
            return text;
        }
    }

    static {
        for (SCSSFunctionGenerator serializer : initSerializers()) {
            SERIALIZERS.put(serializer.getFunctionName(), serializer);
        }
    }

    @Override
    public boolean containsArithmeticalOperator() {
        LexicalUnitImpl current = this;
        while (current != null) {
            for (BinaryOperator operator : BinaryOperator.values()) {
                /*
                 * '/' is treated as an arithmetical operator when one of its
                 * operands is Variable, or there is another binary operator.
                 * Otherwise, '/' is treated as a CSS operator.
                 */
                if (current.getLexicalUnitType() == operator.type) {
                    if (current.getLexicalUnitType() != BinaryOperator.DIV.type) {
                        return true;
                    } else {
                        if (current.getPreviousLexicalUnit()
                                .getLexicalUnitType() == SCSS_VARIABLE
                                || current.getNextLexicalUnit()
                                        .getLexicalUnitType() == SCSS_VARIABLE) {
                            return true;
                        }
                    }
                }
            }
            current = current.getNextLexicalUnit();
        }
        return false;
    }

    @Override
    public LexicalUnitImpl evaluateArithmeticExpressions() {
        return ArithmeticExpressionEvaluator.get().evaluate(this);
    }

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
        if (getNextLexicalUnit() != null) {
            getNextLexicalUnit().updateUrl(prefix);
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

    // The following methods are used to make a LexicalUnitImpl behave like a
    // list.
    @Override
    public SassList.Separator getSeparator() {
        return SassList.Separator.SPACE;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public LexicalUnitImpl get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Illegal index: " + index
                    + ". Number of elements: " + size() + ".");
        }
        return this;
    }

    @Override
    public Iterator<SassListItem> iterator() {
        SassList wrapped = new SassList(this);
        return wrapped.iterator();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        return this;
    }

    public SassList addAllItems(SassListItem items) {
        return new SassList(items.getSeparator(), this).addAllItems(items);
    }

    @Override
    public SassList removeAllItems(SassListItem items) {
        return new SassList(getSeparator(), this).removeAllItems(items);
    }

    @Override
    public boolean containsAllItems(SassListItem items) {
        return new SassList(getSeparator(), this).containsAllItems(items);
    }

    @Override
    public SassListItem flatten() {
        return this;
    }

}
