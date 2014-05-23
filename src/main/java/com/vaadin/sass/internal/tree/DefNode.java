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
package com.vaadin.sass.internal.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.VariableArgumentList;

/**
 * DefNode defines the shared functionality of mixin and function definition
 * nodes. This includes the handling of parameter lists.
 * 
 * @author Vaadin
 * 
 */
public abstract class DefNode extends Node implements IVariableNode {
    private String name;
    private ArrayList<VariableNode> arglist;
    private boolean hasVariableArguments = false;

    public DefNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        super();
        this.name = name;
        arglist = (args != null) ? new ArrayList<VariableNode>(args)
                : new ArrayList<VariableNode>();
        hasVariableArguments = hasVariableArgs;
    }

    public String getName() {
        return name;
    }

    public List<VariableNode> getArglist() {
        return arglist;
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }

    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        for (final VariableNode var : variables) {
            for (final VariableNode arg : new ArrayList<VariableNode>(arglist)) {
                if (arg.getName().equals(var.getName())
                        && arg.getExpr() == null) {
                    arglist.set(arglist.indexOf(arg), var.copy());
                }
            }
        }
    }

    /**
     * We have to replace all the mixin or function parameters. This is done in
     * two phases. First phase replaces all the named parameters while the
     * second replaces in order of remaining unmodified parameters.
     * 
     * If there are variable arguments (the last argument is of the form $x...),
     * any remaining arguments are packaged into a list.
     */
    // TODO instead of modifying def, return a VariableArgumentList?
    // -- should then perhaps also change DefNode to use these? (not yet done)
    public void replacePossibleArguments(
            NodeWithVariableArguments actualArgumentNode) {
        List<VariableNode> actualArguments = actualArgumentNode.getArglist()
                .getVariableNodeList();
        if (actualArguments.size() > 0) {
            ArrayList<VariableNode> remainingDefArguments = new ArrayList<VariableNode>(
                    getArglist());
            ArrayList<VariableNode> remainingActualArguments = new ArrayList<VariableNode>(
                    actualArguments);
            String varArgName = null;
            if (hasVariableArguments()) {
                varArgName = remainingDefArguments.get(
                        remainingDefArguments.size() - 1).getName();
            }
            for (final VariableNode unit : actualArguments) {
                if (unit.getName() != null) {
                    for (final VariableNode var : getArglist()) {
                        if (!var.getName().equals(varArgName)
                                && var.getName().equals(unit.getName())) {
                            var.setExpr(unit.getExpr());
                            remainingDefArguments.remove(var);
                            remainingActualArguments.remove(unit);
                            break;
                        }
                    }
                }
            }
            if (!hasVariableArguments()) {
                checkExtraParameters(remainingDefArguments.size(),
                        remainingActualArguments.size());
            }
            for (int i = 0; i < remainingDefArguments.size()
                    && i < remainingActualArguments.size(); i++) {
                VariableNode unit = remainingActualArguments.get(i);
                remainingDefArguments.get(i).setExpr(unit.getExpr());
            }
            checkForUnsetParameters();

            // If the mixin or function takes a variable number of arguments,
            // the last argument and any remaining arguments are packaged into
            // (one or two) lists. The unnamed and named arguments form separate
            // lists.
            if (hasVariableArguments()) {
                List<SassListItem> unnamed = new ArrayList<SassListItem>();
                List<VariableNode> named = new ArrayList<VariableNode>();
                int lastIndex = getArglist().size() - 1;
                SassListItem last = getArglist().get(lastIndex).getExpr();
                if (last != null) {
                    unnamed.add(last);
                }

                for (int i = remainingDefArguments.size(); i < remainingActualArguments
                        .size(); i++) {
                    VariableNode unit = remainingActualArguments.get(i).copy();
                    if (unit.getName() == null) {
                        unnamed.add(unit.getExpr());
                    } else {
                        named.add(new VariableNode(unit.getName(), unit
                                .getExpr(), false));
                        // The named arguments cannot be used inside the mixin
                        // or function but they can be passed to another one
                        // using variable arguments in an @include or function
                        // call.
                    }
                }
                VariableArgumentList remaining = new VariableArgumentList(
                        actualArgumentNode.getSeparator(), unnamed, named,
                        false);
                getArglist().get(lastIndex).setExpr(remaining);
            }
        }
    }

    private void checkExtraParameters(int remainingDefArguments,
            int remainingActualArguments) {
        if (remainingActualArguments > remainingDefArguments) {
            String errorMessage = "More parameters than expected, in "
                    + getName();
            throw new ParseException(errorMessage, this);
        }
    }

    /**
     * Checks whether all parameters of the definition node defNode have been
     * set. Raises an exception if there are unset parameters.
     */
    private void checkForUnsetParameters() {
        List<VariableNode> arglist = getArglist();
        for (int i = 0; i < arglist.size() - 1; i++) {
            if (arglist.get(i) == null) {
                throw new ParseException("Less parameters than expected for "
                        + getName(), this);
            }
        }
        if (!hasVariableArguments() && arglist.get(arglist.size() - 1) == null) {
            throw new ParseException("Less parameters than expected for "
                    + getName(), this);
        }
    }

    @Override
    public void traverse() {
        if (!arglist.isEmpty()) {
            for (final VariableNode arg : arglist) {
                if (arg.getExpr() != null) {
                    ScssStylesheet.addVariable(arg);
                }
            }
        }
    }
}
