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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;

/**
 * FormalArgumentList is used for representing the parameter list of a mixin or
 * a function definition. Formal arguments are always named and may optionally
 * have a default value. FormalArgumentList also supports variable arguments,
 * which means that if there are more actual than formal parameters, all actual
 * parameters not corresponding to an ordinary formal parameter are packed into
 * a list. The list becomes the value of the variable argument. When variable
 * arguments are used, the number of actual arguments can also be one less than
 * the number of formal arguments. In that case the value of the variable
 * argument is an empty list.
 * 
 * @author Vaadin
 * 
 */
public class FormalArgumentList implements Serializable, Iterable<Variable> {

    private ArrayList<Variable> arglist;
    private String variableArgumentName = null;

    public FormalArgumentList(Collection<Variable> args,
            boolean hasVariableArguments) {
        if (args != null) {
            arglist = new ArrayList<Variable>(args);
            if (hasVariableArguments) {
                if (arglist.size() == 0) {
                    throw new ParseException(
                            "Variable arguments are not allowed with an empty formal parameter list.");
                }
                variableArgumentName = arglist.get(args.size() - 1).getName();
            }
        } else {
            arglist = new ArrayList<Variable>();
        }
    }

    public FormalArgumentList replaceVariables(ScssContext context) {
        ArrayList<Variable> result = new ArrayList<Variable>();
        for (final Variable arg : arglist) {
            SassListItem expr = arg.getExpr();
            if (expr != null) {
                expr = expr.replaceVariables(context);
            }
            if (expr == null) {
                Variable var = context.getVariable(arg.getName());
                if (var != null) {
                    expr = var.getExpr();
                }
            }
            result.add(new Variable(arg.getName(), expr));
        }
        return new FormalArgumentList(result, hasVariableArguments());
    }

    /**
     * Returns a new FormalArgumentList that is obtained from this list by
     * replacing all formal arguments with the corresponding actual arguments.
     * Does not modify this list.
     * 
     * The replacement works in several phases. The first phase replaces formal
     * arguments that have the same name as one of the actual arguments. The
     * second phase replaces the remaining unset formal arguments with the
     * values of the unnamed actual arguments. Default values are then given for
     * any remaining formal arguments without a value. Finally, if there are
     * variable arguments, any unused actual arguments are packed into a list,
     * which becomes the value of the variable argument.
     * 
     * Examples:
     * 
     * 1) formal argument list ($a, $b, $c: 10, $d: 7, $e: 5), actual argument
     * list (1, 2, $d: 4, $c: 3). After the first phase the list is ($a: null,
     * $b: null, $c: 3, $d: 4, $e: null). The second phase transforms that to
     * ($a: 1, $b: 2, $c: 3, $d: 4, $e: null). The remaining variable is then
     * given its default value with result ($a: 1, $b: 2, $c: 3, $d: 4, $e: 5).
     * 
     * 2) formal argument list ($a...), actual argument list empty. After
     * replacement the formal argument list is ($a: ()).
     * 
     * 3) formal argument list ($a, $b...), actual argument list (1, 2, 3, $c:
     * 4). After replacement the formal argument list is ($a: 1, $b: (2, 3, $c:
     * 4).
     * 
     * Note that if named arguments are packed into a list for a variable
     * argument as in example 3, they cannot be accessed by the mixin or the
     * function. It is, however, possible to pass them to another function or a
     * mixin using an @include or a function call with variable arguments.
     * 
     * @param actualArgumentList
     *            The actual arguments.
     * @param checkForUnsetParameters
     *            True if it should be checked that all arguments have been set.
     *            The value false should only be used when there are optional
     *            parameters without a default value and it is allowed that
     *            unset parameters remain.
     * @return A FormalArgumentList with the values of the arguments taken from
     *         the actual argument list and from the default values.
     */
    public FormalArgumentList replaceFormalArguments(
            ActualArgumentList actualArgumentList,
            boolean checkForUnsetParameters) {
        ArrayList<Variable> result = initializeArgumentList(arglist);
        List<Variable> unusedNamedActual = replaceNamedArguments(result,
                actualArgumentList);
        List<SassListItem> unusedUnnamedActual = replaceUnnamedAndDefaultArguments(
                result, actualArgumentList, checkForUnsetParameters);
        // Perform sanity checks and handle variable arguments
        if (hasVariableArguments()) {
            ArgumentList varArgContents = new ArgumentList(
                    actualArgumentList.getSeparator(), unusedUnnamedActual,
                    unusedNamedActual);
            Variable varArg = new Variable(variableArgumentName, varArgContents);
            result.set(result.size() - 1, varArg);
        } else {
            if (!unusedNamedActual.isEmpty() || !unusedUnnamedActual.isEmpty()) {
                throw new ParseException(
                        "Substitution error: some actual parameters were not used. Formal parameters: "
                                + this + ", actual parameters: "
                                + actualArgumentList, actualArgumentList);
            }
        }
        return new FormalArgumentList(result, false);
    }

    /**
     * Replaces the expressions of the formal arguments corresponding to the
     * named actual arguments. If variable arguments are used, the variable
     * argument is not replaced here. Instead, the actual name-value pair with
     * the name of the variable argument is added to the list of unused named
     * actual arguments. Note that this is an implicit argument for the method;
     * it is used for error reporting and for accessing the variable argument
     * information.
     * 
     * Modifies formalArguments.
     * 
     * Examples: a) formal arguments ($a, $b, $c), actual arguments (1, $c:3,
     * $b:2). After replacing with named actual arguments the formal argument
     * list is ($a, $b: 2, $c: 3). b) formal arguments ($a, $b...), actual
     * arguments (1, 2, $c: 3). No named variables are replaced here, but a list
     * of unused named arguments is returned, i.e. the list ($c: 3).
     * 
     * @return A list of actual arguments for which no corresponding formal
     *         argument was found. This is allowed to be nonempty when variable
     *         arguments are used.
     */
    private List<Variable> replaceNamedArguments(
            ArrayList<Variable> formalArguments,
            ActualArgumentList actualArguments) {
        ArrayList<Variable> unusedNamed = new ArrayList<Variable>();
        for (Variable actualArg : actualArguments.getNamedVariables()) {
            boolean actualUsed = false;
            for (Variable formalArg : formalArguments) {
                if (formalArg.getName().equals(actualArg.getName())
                        && !actualArg.getName().equals(variableArgumentName)) {
                    if (formalArg.getExpr() != null) {
                        throw new ParseException(
                                "The named argument $"
                                        + formalArg.getName()
                                        + "appears more than once in the actual argument list: "
                                        + actualArguments, actualArguments);
                    }
                    actualUsed = true;
                    formalArg.setExpr(actualArg.getExpr());
                }
            }
            if (!actualUsed) {
                if (!hasVariableArguments()) {
                    throw new ParseException(
                            "There is no formal argument corresponding to the actual argument "
                                    + actualArg.getName()
                                    + " in the formal argument list " + this,
                            actualArguments);
                }
                unusedNamed.add(actualArg);
            }
        }
        return unusedNamed;
    }

    /**
     * Replaces the arguments that are specified by position, i.e. the unnamed
     * arguments. Also sets the default values for formal arguments that still
     * have null value after replacing the arguments. This is an implicit
     * parameter for the method; it is used for error reporting and for
     * accessing the default values.
     * 
     * Modifies formalArguments.
     * 
     * The replacement is done in left-to-right order, i.e. the first unset
     * formal argument gets the value of the first unnnamed actual argument.
     * 
     * Example: actual arguments ($a, $b: 2, $c: 6), actual argument list (1,
     * $c:3). The named arguments have already been replaced so this method gets
     * the formal argument list ($a: null, $b: null, $c: 3) as its input.
     * Replacing the positional argument yields ($a: 1, $b: null, $c: 3) and
     * replacing the default value the list ($a: 1, $b: 2, $c: 3).
     * 
     * @param formalArguments
     *            The formal arguments.
     * @param actualArguments
     *            The actual arguments.
     * @param checkForUnsetParameters
     *            True if it should be checked that all arguments have been set.
     * @return A list of unused unnamed parameters. This may be nonempty when
     *         variable arguments are used.
     */
    private List<SassListItem> replaceUnnamedAndDefaultArguments(
            ArrayList<Variable> formalArguments,
            ActualArgumentList actualArguments, boolean checkForUnsetParameters) {
        // Replace unnamed arguments
        int formalIndex = getNextUnset(formalArguments, 0);
        int actualIndex = 0;
        int maxFormalIndex = hasVariableArguments() ? formalArguments.size() - 1
                : formalArguments.size();
        while (formalIndex < maxFormalIndex
                && actualIndex < actualArguments.size()) {
            formalArguments.get(formalIndex).setExpr(
                    actualArguments.get(actualIndex));
            formalIndex = getNextUnset(formalArguments, formalIndex + 1);
            actualIndex++;
        }
        // Replace default values given in the definition node
        while (formalIndex < maxFormalIndex) {
            if (arglist.get(formalIndex).getExpr() == null
                    && checkForUnsetParameters) {
                throw new ParseException(
                        "Argument substitution error: there is no value for the argument "
                                + formalArguments.get(formalIndex).getName()
                                + ". Formal arguments: " + this
                                + ", actual arguments: " + actualArguments,
                        actualArguments);
            }
            formalArguments.get(formalIndex).setExpr(
                    arglist.get(formalIndex).getExpr());
            formalIndex = getNextUnset(formalArguments, formalIndex + 1);
        }
        ArrayList<SassListItem> remainingUnnamed = subList(actualArguments,
                actualIndex, actualArguments.size());
        return remainingUnnamed;
    }

    private static ArrayList<SassListItem> subList(ActualArgumentList list,
            int minIndex, int maxIndex) {
        ArrayList<SassListItem> result = new ArrayList<SassListItem>();
        for (int i = minIndex; i < maxIndex; i++) {
            result.add(list.get(i));
        }
        return result;
    }

    private static int getNextUnset(ArrayList<Variable> named, int i) {
        while (i < named.size() && named.get(i).getExpr() != null) {
            i++;
        }
        return i;
    }

    private static ArrayList<Variable> initializeArgumentList(
            List<Variable> namedParameters) {
        ArrayList<Variable> result = new ArrayList<Variable>();
        for (Variable var : namedParameters) {
            result.add(new Variable(var.getName(), null));
        }
        return result;
    }

    public boolean hasVariableArguments() {
        return variableArgumentName != null;
    }

    public boolean isEmpty() {
        return arglist.isEmpty();
    }

    @Override
    public Iterator<Variable> iterator() {
        return arglist.iterator();
    }

    public int size() {
        return arglist.size();
    }

    public Variable get(int i) {
        return arglist.get(i);
    }

    public List<Variable> getArguments() {
        return Collections.unmodifiableList(arglist);
    }

    @Override
    public String toString() {
        String result = "FormalArgumentList[";
        for (int i = 0; i < arglist.size(); i++) {
            Variable item = arglist.get(i);
            result += "$" + item.getName() + ": ";
            if (item.getExpr() == null) {
                result += "null";
            } else {
                result += item.getExpr().printState();
            }
            if (i < arglist.size() - 1) {
                result += ", ";
            }
        }
        result += "]";
        return result;
    }
}
