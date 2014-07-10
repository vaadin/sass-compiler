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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.SassList.Separator;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;

/**
 * ActualArgumentList is used for representing the actual arguments of an @include
 * or a function call. ActualArgumentList contains a list of named and unnamed
 * variables and an optional variable argument. The variable argument differs
 * from the ordinary arguments so that if it is list-valued, its contents are
 * expanded into separate arguments.
 * 
 * @author Vaadin
 * 
 */
public class ActualArgumentList implements Serializable {
    private ArgumentList arglist;
    // variableArgument is not duplicated in arglist and can be null. A variable
    // argument is always unnamed in an actual argument list.
    private SassListItem variableArgument = null;

    public ActualArgumentList(ArgumentList list, SassListItem variableArgument) {
        arglist = list;
        this.variableArgument = variableArgument;
    }

    public ActualArgumentList(Separator separator, List<SassListItem> list,
            List<Variable> named, SassListItem variableArgument) {
        arglist = new ArgumentList(separator, list, named);
        this.variableArgument = variableArgument;
    }

    public ActualArgumentList(Separator sep, SassListItem... items) {
        arglist = new ArgumentList(sep, items);
    }

    public ActualArgumentList(Separator separator,
            List<SassListItem> newParamValues) {
        arglist = new ArgumentList(separator, newParamValues);
    }

    public ActualArgumentList(Separator separator, Collection<Variable> args,
            boolean hasVariableArguments) {
        ArrayList<SassListItem> unnamed = new ArrayList<SassListItem>();
        ArrayList<Variable> named = new ArrayList<Variable>();
        if (args != null) {
            for (Variable arg : args) {
                if (arg.getName() == null) {
                    unnamed.add(arg.getExpr());
                } else {
                    named.add(arg.copy());
                }
            }
        }
        SassListItem varArg = null;
        if (hasVariableArguments) {
            varArg = unnamed.get(unnamed.size() - 1);
            unnamed.remove(unnamed.size() - 1);
        }
        arglist = new ArgumentList(separator, unnamed, named);
        variableArgument = varArg;
    }

    public ActualArgumentList expandVariableArguments() {
        /*
         * Returns a new ActualArgumentList that is obtained from this by
         * expanding the variable argument into separate arguments. If there is
         * no variable argument, returns this. In either case, the result does
         * not contain variable argument.
         * 
         * Note that the separator character should be preserved when an
         * 
         * @include expands variables into separate arguments and the
         * corresponding @mixin packs them again into a list. Some cases have
         * not yet been verified to work as they should.
         * 
         * To illustrate the cases, suppose that there is a mixin with variable
         * arguments @mixin foo($a1, $a2, ..., $ak...). That is used by an
         * include with variable arguments: @include foo($b1, $b2, ..., $bl...).
         * Then the include will expand the argument bl into separate arguments,
         * if bl is a list. The mixin will pack possibly several arguments into
         * a list ak. The cases are then
         * 
         * 1) k = l. Then ak will be a list equal to bl. To retain the
         * separator, it needs to be taken from the list bl.
         * 
         * 2) l < k. Now ak will be a sublist of bl, the first elements of bl
         * will be used for the parameters a(l+1), ..., a(k-1). If a list should
         * retain the separator, its sublist should also have the same
         * separator.
         * 
         * 3) l > k, the uncertain and only partially verified case. Now, ak
         * will be a list that contains the parameters b(k+1), ..., b(l-1) and
         * the contents of the list bl. Using the separator of the list bl means
         * that the same separator will also separate the parameters b(k+1)...
         * from each other in the list ak. That is the approach adopted here,
         * but it is only based on a limited amount of testing.
         * 
         * The separator of a one-element list is considered to be a comma here.
         * 
         * 
         * Also note that the named and unnamed parameters are stored in two
         * separate lists. The named parameters packed into a variable argument
         * list cannot be accessed inside the mixin. While this is unexpected,
         * it seems to be the desired behavior, although only a limited amount
         * of testing has been done to verify this.
         */
        if (hasVariableArguments()) {
            List<SassListItem> unnamedArgs = new ArrayList<SassListItem>(
                    arglist.getItems());
            List<Variable> namedArgs = new ArrayList<Variable>(
                    arglist.getNamedVariables());
            if (variableArgument instanceof SassList) {
                SassList lastList = (SassList) variableArgument;
                for (SassListItem item : lastList) {
                    unnamedArgs.add(item);
                }
            }
            // Append any remaining variable name-value pairs to the argument
            // list
            if (variableArgument instanceof ArgumentList) {
                for (Variable namedVar : ((ArgumentList) variableArgument)
                        .getNamedVariables()) {
                    namedArgs.add(namedVar.copy());
                }
            }
            return new ActualArgumentList(
                    arglist.getSeparator(variableArgument), unnamedArgs,
                    namedArgs, null);
        }
        return this;
    }

    public ActualArgumentList replaceVariables(ScssContext context) {
        ArgumentList newArgList = arglist.replaceVariables(context);
        SassListItem newVarArg = null;
        if (hasVariableArguments()) {
            newVarArg = variableArgument.replaceVariables(context);
        }
        return new ActualArgumentList(newArgList, newVarArg);
    }

    public ActualArgumentList evaluateFunctionsAndExpressions(
            ScssContext context, boolean evaluateArithmetics) {
        ArgumentList newArgList = arglist.evaluateFunctionsAndExpressions(
                context, evaluateArithmetics);
        SassListItem newVarArg = null;
        if (hasVariableArguments()) {
            newVarArg = variableArgument.evaluateFunctionsAndExpressions(
                    context, evaluateArithmetics);
        }
        return new ActualArgumentList(newArgList, newVarArg);
    }

    public boolean hasVariableArguments() {
        return variableArgument != null;
    }

    public String buildString(BuildStringStrategy strategy) {
        return arglist.buildString(strategy);
    }

    public String printState() {
        return arglist.printState();
    }

    @Override
    public String toString() {
        String result = "Actual argument list [" + arglist.toString();
        if (variableArgument != null) {
            result += ", variable argument: " + variableArgument.toString();
        }
        result += "]";
        return result;
    }

    /**
     * Returns the number of unnamed arguments contained in this list.
     */
    public int size() {
        return arglist.size();
    }

    /**
     * Returns the unnamed argument at index i.
     */
    public SassListItem get(int i) {
        return arglist.get(i);
    }

    public Separator getSeparator() {
        return arglist.getSeparator();
    }

    public List<Variable> getNamedVariables() {
        return arglist.getNamedVariables();
    }
}
