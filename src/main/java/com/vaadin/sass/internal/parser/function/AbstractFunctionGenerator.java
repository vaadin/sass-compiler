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
package com.vaadin.sass.internal.parser.function;

import java.util.ArrayList;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.Variable;

/**
 * AbstractFunctionGenerator is an abstract base class for implementing built-in
 * Sass functions. It is assumed that all functions implemented by a subclass of
 * AbstractFunctionGenerator have the same formal argument list, i.e. they have
 * the same number of arguments and the arguments of the different functions
 * have the same names.
 * 
 * @author Vaadin
 * 
 */
public abstract class AbstractFunctionGenerator implements
        SCSSFunctionGenerator {

    private String[] functionNames;
    private FormalArgumentList arguments;

    public AbstractFunctionGenerator(FormalArgumentList arguments,
            String... functionNames) {
        this.functionNames = functionNames;
        this.arguments = arguments;
    }

    @Override
    public String[] getFunctionNames() {
        return functionNames;
    }

    protected FormalArgumentList getArguments() {
        return arguments;
    }

    @Override
    public SassListItem compute(ScssContext context, LexicalUnitImpl function) {
        ActualArgumentList args = function.getParameterList();
        FormalArgumentList functionArguments;
        try {
            functionArguments = arguments.replaceFormalArguments(args,
                    checkForUnsetParameters());
        } catch (ParseException e) {
            throw new ParseException("Error in parameters of function "
                    + function.getFunctionName() + "(), line "
                    + function.getLineNumber() + ", column "
                    + function.getColumnNumber() + ": [" + e.getMessage() + "]");
        }
        return computeForArgumentList(function, functionArguments);
    }

    /**
     * Computes the value of the function with the given argument list.
     * 
     * This method is called by compute(), which also forms the parameter list
     * actualArguments by replacing the formal arguments of the function with
     * their actual values.
     * 
     * @param function
     *            The function to be evaluated. The function object can be used
     *            to determine which function to evaluate when a function
     *            generator implements several Sass functions. It is also used
     *            to obtain line and column numbers for error messages.
     * @param actualArguments
     *            The argument list of the function, obtained by replacing all
     *            formal arguments with the corresponding values of the actual
     *            argument list of the function.
     * @return The value of the function.
     */
    protected abstract SassListItem computeForArgumentList(
            LexicalUnitImpl function, FormalArgumentList actualArguments);

    /**
     * Returns true if this function should ensure that all parameters have been
     * set. This is the most common use case. The value false can be used for
     * built-in functions that have optional parameters without a default value.
     * 
     * @return whether this function requires that all its parameters have a
     *         value.
     */
    protected boolean checkForUnsetParameters() {
        return true;
    }

    /**
     * Creates a formal argument list with the given argument names and default
     * values. The arrays argumentNames and defaultValues should have an equal
     * number of elements.
     * 
     * @param argumentNames
     *            The names of the arguments.
     * @param defaultValues
     *            The default values of the arguments.
     * @param hasVariableArguments
     *            True, if the function supports variable arguments, false
     *            otherwise.
     * @return A FormalArgumentList.
     */
    protected static FormalArgumentList createArgumentList(
            String[] argumentNames, SassListItem[] defaultValues,
            boolean hasVariableArguments) {
        ArrayList<Variable> nodes = new ArrayList<Variable>();
        for (int i = 0; i < argumentNames.length; i++) {
            nodes.add(new Variable(argumentNames[i], defaultValues[i]));
        }
        return new FormalArgumentList(nodes, hasVariableArguments);
    }

    /**
     * Creates a new FormalArgumentList without variable arguments.
     * 
     * @param argumentNames
     *            The names of the arguments.
     * @param defaultValues
     *            The default values of the arguments.
     * @return A FormalArgumentList.
     */
    protected static FormalArgumentList createArgumentList(
            String[] argumentNames, SassListItem[] defaultValues) {
        return createArgumentList(argumentNames, defaultValues, false);
    }

    /**
     * Creates a new FormalArgumentList with no default values, i.e. all default
     * values are null.
     * 
     * @param argumentNames
     *            The names of the arguments.
     * @param hasVariableArguments
     *            True, if the function supports variable arguments, false
     *            otherwise.
     * @return A FormalArgumentList.
     */
    protected static FormalArgumentList createArgumentList(
            String[] argumentNames, boolean hasVariableArguments) {
        SassListItem[] nullDefaults = new SassListItem[argumentNames.length];
        return createArgumentList(argumentNames, nullDefaults,
                hasVariableArguments);
    }

    /**
     * Returns the value of the argument with the given name.
     * 
     * @param args
     *            The argument list.
     * @param name
     *            The name of the argument.
     * @return The value of the argument whose name corresponds to the parameter
     *         name.
     */
    protected static SassListItem getParam(FormalArgumentList args, String name) {
        for (Variable var : args.getArguments()) {
            if (var.getName().equals(name)) {
                return var.getExpr();
            }
        }
        throw new ParseException("There is no argument " + name
                + " in the argument list: " + args.toString());
    }

    /**
     * Returns the value of the argument at the given index.
     * 
     * @param args
     *            The argument list.
     * @param index
     *            The position of the argument in the argument list.
     * @return The value of the argument at position index in the argument list
     *         args.
     */
    protected static SassListItem getParam(FormalArgumentList args, int index) {
        if (index < 0 || index >= args.size()) {
            throw new ParseException("Illegal index (" + index
                    + ") for the argument list: " + args.toString());
        }
        return args.get(index).getExpr();
    }
}