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

package com.vaadin.sass.internal.expression;

import org.w3c.css.sac.LexicalUnit;

import com.vaadin.sass.internal.expression.exception.ArithmeticException;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;

public enum BinaryOperator {
    OR(LexicalUnitImpl.SCSS_OPERATOR_OR, 1) {
        @Override
        public SassListItem eval(SassListItem leftValue, SassListItem rightValue) {
            if (isTrue(leftValue)) {
                return leftValue;
            } else {
                return rightValue;
            }
        }
    },
    AND(LexicalUnitImpl.SCSS_OPERATOR_AND, 2) {
        @Override
        public SassListItem eval(SassListItem leftValue, SassListItem rightValue) {
            if (isTrue(leftValue)) {
                return rightValue;
            } else {
                return leftValue;
            }
        }
    },
    EQUALS(LexicalUnitImpl.SCSS_OPERATOR_EQUALS, 3) {
        @Override
        public SassListItem eval(SassListItem leftValue, SassListItem rightValue) {
            boolean value = leftValue.unquotedString().equals(
                    rightValue.unquotedString());
            return createBooleanUnit(value);
        }
    },
    NOT_EQUAL(LexicalUnitImpl.SCSS_OPERATOR_NOT_EQUAL, 3) {
        @Override
        public SassListItem eval(SassListItem leftValue, SassListItem rightValue) {
            boolean value = !leftValue.unquotedString().equals(
                    rightValue.unquotedString());
            return createBooleanUnit(value);
        }
    },
    LESS_THAN(LexicalUnitImpl.SAC_OPERATOR_LT, 3) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return createBooleanUnit(getFloatValue(leftValue) < getFloatValue(rightValue));
        }
    },
    GREATER_THAN(LexicalUnitImpl.SAC_OPERATOR_GT, 3) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return createBooleanUnit(getFloatValue(leftValue) > getFloatValue(rightValue));
        }
    },
    LESS_THAN_OR_EQUALS(LexicalUnitImpl.SAC_OPERATOR_LE, 3) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return createBooleanUnit(getFloatValue(leftValue) <= getFloatValue(rightValue));
        }
    },
    GREATER_THAN_OR_EQUALS(LexicalUnitImpl.SAC_OPERATOR_GE, 3) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return createBooleanUnit(getFloatValue(leftValue) >= getFloatValue(rightValue));
        }
    },
    ADD(LexicalUnit.SAC_OPERATOR_PLUS, 4) {
        @Override
        public LexicalUnitImpl eval(SassListItem leftValue,
                SassListItem rightValue) {
            return add(leftValue, rightValue);
        }

        private LexicalUnitImpl add(SassListItem left, SassListItem right) {
            if (left instanceof LexicalUnitImpl
                    && right instanceof LexicalUnitImpl
                    && !LexicalUnitImpl.checkLexicalUnitType(left,
                            LexicalUnitImpl.SAC_STRING_VALUE,
                            LexicalUnitImpl.SAC_IDENT)
                    && !LexicalUnitImpl.checkLexicalUnitType(right,
                            LexicalUnitImpl.SAC_STRING_VALUE,
                            LexicalUnitImpl.SAC_IDENT)) {
                return ((LexicalUnitImpl) left).add((LexicalUnitImpl) right);
            } else {
                String leftValue;
                if (LexicalUnitImpl.checkLexicalUnitType(left,
                        LexicalUnitImpl.SAC_STRING_VALUE,
                        LexicalUnitImpl.SAC_IDENT)) {
                    leftValue = ((LexicalUnitImpl) left).getStringValue();
                } else {
                    leftValue = left.printState();
                }
                String rightValue;
                if (LexicalUnitImpl.checkLexicalUnitType(right,
                        LexicalUnitImpl.SAC_STRING_VALUE,
                        LexicalUnitImpl.SAC_IDENT)) {
                    rightValue = ((LexicalUnitImpl) right).getStringValue();
                } else {
                    rightValue = right.printState();
                }
                String stringValue = leftValue + rightValue;
                boolean quotedResult = (LexicalUnitImpl.checkLexicalUnitType(
                        left, LexicalUnitImpl.SAC_STRING_VALUE) || (!LexicalUnitImpl
                        .checkLexicalUnitType(left, LexicalUnitImpl.SAC_IDENT) && LexicalUnitImpl
                        .checkLexicalUnitType(right,
                                LexicalUnitImpl.SAC_STRING_VALUE)));
                if (quotedResult) {
                    return LexicalUnitImpl.createString(stringValue);
                } else {
                    return LexicalUnitImpl.createIdent(stringValue);
                }

            }
        }
    },
    MINUS(LexicalUnit.SAC_OPERATOR_MINUS, 4) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return leftValue.minus(rightValue);
        }
    },
    MUL(LexicalUnit.SAC_OPERATOR_MULTIPLY, 5) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return leftValue.multiply(rightValue);
        }
    },
    DIV(LexicalUnit.SAC_OPERATOR_SLASH, 5) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return leftValue.divide(rightValue);
        }
    },
    MOD(LexicalUnit.SAC_OPERATOR_MOD, 5) {
        @Override
        public LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
                LexicalUnitImpl rightValue) {
            return leftValue.modulo(rightValue);
        }
    };

    public final short type;
    public final int precedence;

    BinaryOperator(short type, int precedence) {
        this.type = type;
        this.precedence = precedence;
    }

    public static boolean isTrue(SassListItem item) {
        if (LexicalUnitImpl.checkLexicalUnitType(item,
                LexicalUnitImpl.SCSS_NULL)) {
            return false;
        }
        return !"false".equals(item.printState());
    }

    private static float getFloatValue(LexicalUnitImpl unit) {
        if (!unit.isNumber()) {
            throw new ParseException(
                    "The arguments of arithmetic expressions must be numbers:",
                    unit);
        }
        return unit.getFloatValue();
    }

    private static LexicalUnitImpl createBooleanUnit(boolean value) {
        return LexicalUnitImpl.createIdent(String.valueOf(value));
    }

    /**
     * Evaluates an arithmetic expression. The parameters leftValue and
     * rightValue must not be list-valued.
     * 
     * @see #eval(SassListItem, SassListItem)
     */
    protected LexicalUnitImpl evalInternal(LexicalUnitImpl leftValue,
            LexicalUnitImpl rightValue) {
        return null;
    }

    /**
     * Returns the result of applying the operator to the operands.
     * 
     * The default implementation verifies that the operands are simple
     * LexicalUnitImpl instances and calls evalInternal() on them. Either eval()
     * or evalInternal() can be overridden depending on the allowed operand
     * types for the operator.
     */
    public SassListItem eval(SassListItem leftOperand, SassListItem rightOperand) {
        if (!(leftOperand instanceof LexicalUnitImpl)) {
            throw new ArithmeticException(
                    "Left operand of the operator is not a simple value",
                    leftOperand);
        }
        if (!(rightOperand instanceof LexicalUnitImpl)) {
            throw new ArithmeticException(
                    "Right operand of the operator is not a simple value",
                    rightOperand);
        }
        return evalInternal((LexicalUnitImpl) leftOperand,
                (LexicalUnitImpl) rightOperand);
    }
}
