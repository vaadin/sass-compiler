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
package com.vaadin.sass.internal.parser;

import java.io.Serializable;

import com.vaadin.sass.internal.Definition;

/**
 * Variable with an immutable name, a modifiable value and an optional flag
 * indicating whether it is guarded.
 * 
 * Guarded variables are not overwritten by further assignments to the same
 * variable (except possibly for assignment of the value null).
 */
public class Variable implements Definition, Serializable {

    private final String name;
    private SassListItem expr = null;
    private final boolean guarded;

    public Variable(String name, SassListItem expr) {
        this(name, expr, false);
    }

    public Variable(String name, SassListItem expr, boolean guarded) {
        this.name = name;
        this.expr = expr;
        this.guarded = guarded;
    }

    public SassListItem getExpr() {
        return expr;
    }

    public void setExpr(SassListItem expr) {
        this.expr = expr;
    }

    public String getName() {
        return name;
    }

    public boolean isGuarded() {
        return guarded;
    }

    public Variable copy() {
        return new Variable(name, expr, guarded);
    }

    /**
     * Replaces each occurrence of ${name} in the parameter. Only evaluates the
     * variable if at least one such occurrence, and then only evaluates it
     * once.
     */
    public String replaceInterpolation(String s) {
        final String interpolation = "#{$" + getName() + "}";
        if (s.contains(interpolation)) {
            return s.replace(interpolation, getExpr().unquotedString());
        } else {
            return s;
        }
    }

}
