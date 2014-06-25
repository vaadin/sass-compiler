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
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.vaadin.sass.internal.tree.FunctionDefNode;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.tree.VariableNode;

/**
 * Nestable scope for variables, functions and mixins.
 */
public class Scope {

    private static class DefinitionScope<T extends Definition> {
        private DefinitionScope<T> parent;
        // TODO optimize by not creating map if empty?
        private HashMap<String, T> definitions = new HashMap<String, T>();

        public DefinitionScope(DefinitionScope<T> parent) {
            this.parent = parent;
        }

        /**
         * Sets a definition value in the largest scope where it is already
         * defined. If the variable isn't defined, set it in the current scope.
         * 
         * @param node
         *            definition to set
         */
        public void set(T node) {
            if (parent == null || !parent.setIfPresent(node)) {
                add(node);
            }
        }

        /**
         * Sets a definition in the current scope without checking parent
         * scopes.
         * 
         * @param node
         *            definition to set
         */
        public void add(T node) {
            definitions.put(node.getName(), node);
        }

        /**
         * Sets a definition and returns true if it is already defined in the
         * scope or its parents. Otherwise returns false.
         * 
         * @param node
         *            definition to set
         * @return true if the definition was set
         */
        private boolean setIfPresent(T node) {
            if (parent != null && parent.setIfPresent(node)) {
                return true;
            }
            if (definitions.containsKey(node.getName())) {
                definitions.put(node.getName(), node);
                return true;
            }
            return false;
        }

        public T get(String name) {
            if (definitions.containsKey(name)) {
                return definitions.get(name);
            } else if (parent != null) {
                return parent.get(name);
            } else {
                return null;
            }
        }

        /**
         * Returns an {@link Iterable} of all variables defined in this scope
         * and its parents. Variables that are masked by a similarly named copy
         * in an inner scope are not returned, but only the innermost instance
         * is used.
         * 
         * @return iterable over all definitions in scope, generated iterators
         *         are unmodifiable
         */
        public Iterable<T> getIterable() {
            // no need to copy contents in the top-level scope
            if (parent == null) {
                return Collections.unmodifiableCollection(definitions.values());
            }

            LinkedHashMap<String, T> result = new LinkedHashMap<String, T>();
            // parent first so that this scope can override its variables
            for (T var : parent.getIterable()) {
                result.put(var.getName(), var);
            }
            result.putAll(definitions);
            return Collections.unmodifiableCollection(result.values());
        }

        @Override
        public String toString() {
            return definitions.keySet().toString() + ", parent = " + parent;
        }

    }

    private Scope parent;
    private DefinitionScope<VariableNode> variables;
    private DefinitionScope<FunctionDefNode> functions;
    private DefinitionScope<MixinDefNode> mixins;

    public Scope() {
        variables = new DefinitionScope<VariableNode>(null);
        functions = new DefinitionScope<FunctionDefNode>(null);
        mixins = new DefinitionScope<MixinDefNode>(null);
    }

    public Scope(Scope parent) {
        this.parent = parent;
        variables = new DefinitionScope<VariableNode>(parent.variables);
        functions = new DefinitionScope<FunctionDefNode>(parent.functions);
        mixins = new DefinitionScope<MixinDefNode>(parent.mixins);
    }

    public Scope getParent() {
        return parent;
    }

    /**
     * Sets a variable value in the largest scope where it is already defined.
     * If the variable isn't defined, set it in the current scope.
     * 
     * @param node
     *            variable to set
     */
    public void setVariable(VariableNode node) {
        variables.set(node);
    }

    /**
     * Sets a variable in the current scope without checking parent scopes.
     * 
     * @param node
     *            variable to set
     */
    public void addVariable(VariableNode node) {
        variables.add(node);
    }

    public VariableNode getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Returns an {@link Iterable} of all variables defined in this scope and
     * its parents. Variables that are masked by a similarly named copy in an
     * inner scope are not returned, but only the innermost instance is used.
     * 
     * @return iterable over all variables in scope, generated iterators are
     *         unmodifiable
     */
    public Iterable<VariableNode> getVariables() {
        return variables.getIterable();
    }

    public void defineFunction(FunctionDefNode function) {
        functions.add(function);
    }

    public void defineMixin(MixinDefNode mixin) {
        mixins.add(mixin);
    }

    public FunctionDefNode getFunctionDefinition(String name) {
        return functions.get(name);
    }

    public MixinDefNode getMixinDefinition(String name) {
        return mixins.get(name);
    }

    @Override
    public String toString() {
        return "Variables: " + variables.toString() + "\nFunctions: "
                + functions.toString() + "\nMixins: " + mixins.toString();
    }

}
