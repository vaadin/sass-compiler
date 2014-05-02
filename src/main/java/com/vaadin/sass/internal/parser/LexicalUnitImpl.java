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

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.expression.exception.IncompatibleUnitsException;
import com.vaadin.sass.internal.parser.function.AbsFunctionGenerator;
import com.vaadin.sass.internal.parser.function.CeilFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DarkenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DefaultFunctionGenerator;
import com.vaadin.sass.internal.parser.function.FloorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.LightenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListAppendFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListJoinFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListLengthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListNthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.PercentageFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RGBFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RectFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RoundFunctionGenerator;
import com.vaadin.sass.internal.parser.function.SCSSFunctionGenerator;
import com.vaadin.sass.internal.parser.function.TypeOfFunctionGenerator;
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

    private LexicalUnitImpl next;

    private short type;
    private int line;
    private int column;

    private int i;
    private float f;
    private short dimension;
    private String sdimension;
    private String s;
    private String fname;
    private SassList params;

    LexicalUnitImpl(short type, int line, int column, LexicalUnitImpl p) {
        if (p != null) {
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

    private void setLexicalUnitType(short type) {
        this.type = type;
    }

    @Override
    public LexicalUnitImpl getNextLexicalUnit() {
        return next;
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
            return f + "";
        }
    }

    private void setFloatValue(float f) {
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

    private void setStringValue(String str) {
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
        LexicalUnitImpl copy = new LexicalUnitImpl(type, line, column, null);
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

    private void setParameterList(SassList params) {
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
        LexicalUnitImpl result = new LexicalUnitImpl(line, column, previous,
                SAC_PERCENTAGE, null, v);

        if (Math.round(v * 100 * PRECISION) == (((int) v) * 100 * PRECISION)) {
            result.setIntegerValue((int) v);
        }

        return result;
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

    public static LexicalUnitImpl createRawIdent(int line, int column, String s) {
        return new LexicalUnitImpl(line, column, null, SAC_IDENT, s);
    }

    public static LexicalUnitImpl createIdent(int line, int column,
            LexicalUnitImpl previous, String s) {
        if ("null".equals(s)) {
            return createNull(line, column, previous);
        }

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

    public static LexicalUnitImpl createAttr(int line, int column,
            LexicalUnitImpl previous, String s) {
        return new LexicalUnitImpl(line, column, previous, SAC_ATTR, s);
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
        return new LexicalUnitImpl(SAC_FUNCTION, line, column, previous, fname,
                params);
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

    public static LexicalUnitImpl createIdent(String s) {
        return new LexicalUnitImpl(0, 0, null, SAC_IDENT, s);
    }

    @Override
    public SassListItem replaceVariables(Collection<VariableNode> variables) {
        // TODO simplify
        SassListItem result = this;
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
        SassList params = item.getParameterList();
        if (params != null) {
            SassList newParams = params.replaceVariables(Collections
                    .singletonList(node));
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
            if (stringValue != null && stringValue.contains(interpolation)) {
                LexicalUnitImpl copy = unit.copy();
                copy.setStringValue(stringValue.replaceAll(Pattern
                        .quote(interpolation), node.getExpr().unquotedString()));
                item = copy;
            }
        }
        return item;
    }

    @Override
    public SassListItem replaceFunctions() {
        if (params != null) {
            SCSSFunctionGenerator generator = getGenerator(getFunctionName());
            LexicalUnitImpl copy = createFunction(line, column, null, fname,
                    params.replaceFunctions());
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
        list.add(new CeilFunctionGenerator());
        list.add(new DarkenFunctionGenerator());
        list.add(new FloorFunctionGenerator());
        list.add(new LightenFunctionGenerator());
        list.add(new ListAppendFunctionGenerator());
        list.add(new ListJoinFunctionGenerator());
        list.add(new ListLengthFunctionGenerator());
        list.add(new ListNthFunctionGenerator());
        list.add(new PercentageFunctionGenerator());
        list.add(new RectFunctionGenerator());
        list.add(new RGBFunctionGenerator());
        list.add(new RoundFunctionGenerator());
        list.add(new TypeOfFunctionGenerator());
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
                int[] rgb = getRgb();
                if (rgb != null) {
                    text = ColorUtil.rgbToHexColor(rgb, 6);
                    break;
                }
                // else fall through to the function branch
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
        for (SCSSFunctionGenerator serializer : initSerializers()) {
            SERIALIZERS.put(serializer.getFunctionName(), serializer);
        }
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    @Override
    public LexicalUnitImpl evaluateArithmeticExpressions() {
        return this;
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
    public SassListItem replaceChains() {
        return next == null ? this : new SassExpression(this);
    }

}
