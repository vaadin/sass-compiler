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
package com.vaadin.sass.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.tree.FunctionDefNode;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.visitor.Extension;

public class ScssContext {

    private Scope scope = new Scope();

    /**
     * Collection of mappings from an @extend-selector (its simple selector
     * sequence) to a containing block's selectors. E.g. the following
     * extensions:
     * 
     * a { @extend b; ... }; b { @extend b,c; ... }
     * 
     * corresponds to the following set of mappings:
     * 
     * { (b, a), (b, b), (c, b) }
     */
    private Set<Extension> extendsSet = new LinkedHashSet<Extension>();

    public void defineFunction(FunctionDefNode function) {
        scope.defineFunction(function);
    }

    public void defineMixin(MixinDefNode mixin) {
        scope.defineMixin(mixin);
    }

    /**
     * Switch to a new sub-scope of a specific scope. Any variables created
     * after opening a new scope are only valid until the scope is closed,
     * whereas variables from the parent scope(s) that are modified will keep
     * their new values even after closing the inner scope.
     * 
     * When using this method, the scope must be closed with
     * {@link #closeVariableScope(Scope)} with the return value of this method
     * as its parameter.
     * 
     * @return previous scope
     */
    public Scope openVariableScope(Scope parent) {
        Scope previousScope = scope;
        scope = new Scope(parent);
        return previousScope;
    }

    /**
     * End a scope for variables, removing all active variables that only
     * existed in the new scope.
     */
    public void closeVariableScope(Scope newScope) {
        scope = newScope;
    }

    /**
     * Returns the current scope. The returned value should be treated as opaque
     * and only used as a parameter to {@link #openVariableScope(Scope)}.
     * 
     * @return current scope
     */
    public Scope getCurrentScope() {
        return scope;
    }

    /**
     * Start a new scope for variables. Any variables created after opening a
     * new scope are only valid until the scope is closed, at which time they
     * are replaced with their old values, whereas variables from outside the
     * current scope that are modified will keep their new values even after
     * closing the inner scope.
     */
    public void openVariableScope() {
        scope = new Scope(scope);
    }

    /**
     * End a scope for variables, removing all active variables that only
     * existed in the new scope.
     */
    public void closeVariableScope() {
        scope = scope.getParent();
    }

    /**
     * Set the value of a variable that may be in the innermost scope or an
     * outer scope. The new value will be set in the scope in which the variable
     * was defined, or in the current scope if the variable was not set.
     * 
     * @param node
     *            variable to set
     */
    public void setVariable(Variable node) {
        scope.setVariable(node);
    }

    /**
     * Add a scope specific local variable, typically a function or mixin
     * parameter.
     * 
     * @param node
     *            variable to add
     */
    public void addVariable(Variable node) {
        scope.addVariable(node);
    }

    public Variable getVariable(String string) {
        return scope.getVariable(string);
    }

    public Iterable<Variable> getVariables() {
        return scope.getVariables();
    }

    public MixinDefNode getMixinDefinition(String name) {
        return scope.getMixinDefinition(name);
    }

    public FunctionDefNode getFunctionDefinition(String name) {
        return scope.getFunctionDefinition(name);
    }

    public void addExtension(Extension extension) {
        extendsSet.add(extension);
    }

    public Iterable<Extension> getExtensions() {
        return Collections.unmodifiableCollection(extendsSet);
    }

}
