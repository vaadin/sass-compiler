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
import java.util.List;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.VariableNode;

public class ListIndexFunctionGenerator extends AbstractFunctionGenerator {

    public ListIndexFunctionGenerator() {
        super("index");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        checkArguments(function, params);
        SassListItem listItem = getArgument(params, 0, "list");
        SassListItem item = getArgument(params, 1, "value");
        int index = -1;
        if (!(listItem instanceof SassList)) {
            index = listItem.equals(item) ? 1 : -1;
        } else {
            SassList list = (SassList) listItem;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(item)) {
                    index = i + 1;
                    break;
                }
            }
        }
        if (index == -1) {
            return LexicalUnitImpl.createIdent(function.getLineNumber(),
                    function.getColumnNumber(), "false");
        } else {
            return LexicalUnitImpl.createInteger(function.getLineNumber(),
                    function.getColumnNumber(), index);
        }
    }

    /**
     * Returns the unnamed parameter with the given index. If no such parameter
     * exists, returns the named parameter with the given name. If neither of
     * these exists, returns null.
     * 
     */
    private SassListItem getArgument(ActualArgumentList args, int index,
            String name) {
        if (index < args.size()) {
            return args.get(index);
        }
        for (VariableNode node : args.getNamedVariables()) {
            if (node.getName().equals(name)) {
                return node.getExpr();
            }
        }
        return null;
    }

    private void checkArguments(LexicalUnitImpl function,
            ActualArgumentList args) {
        if (args.size() + args.getNamedVariables().size() != 2) {
            throw new ParseException(
                    "The function index() requires exactly two parameters",
                    function);
        }
        List<String> parameterNames = new ArrayList<String>();
        for (VariableNode node : args.getNamedVariables()) {
            String name = node.getName();
            if (!("list".equals(name)) && !("value".equals(name))) {
                throw new ParseException(
                        "The valid parameter names for index() are list and value",
                        function);
            }
            if (parameterNames.contains(name)) {
                throw new ParseException("The parameter " + name
                        + " appears twice in the parameter list of index()",
                        function);
            }
            parameterNames.add(name);
        }
    }
}