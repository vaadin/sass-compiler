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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.Scope;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.Variable;

/**
 * Transient class representing a function call to a custom (user-defined)
 * function. This class is used to evaluate the function call and is discarded
 * after use. A FunctionCall does not have a parent in the stylesheet node tree.
 */
public class FunctionCall {

    public static SassListItem evaluate(ScssContext context,
            FunctionDefNode def, LexicalUnitImpl invocation) {
        ActualArgumentList invocationArglist = invocation.getParameterList()
                .expandVariableArguments();
        SassListItem value = null;
        // only parameters are evaluated in current scope, body in
        // top-level scope
        try {
            FormalArgumentList arglist = def.getArglist();
            arglist = arglist.replaceFormalArguments(invocationArglist, true);
            // replace variables in default values of parameters
            arglist = arglist.replaceVariables(context);

            // copying is necessary as traversal modifies the parent of the
            // node
            // TODO in the long term, should avoid full copy
            FunctionDefNode defCopy = def.copy();

            // limit variable scope to the scope where the function was defined
            Scope previousScope = context.openVariableScope(def
                    .getDefinitionScope());
            try {
                for (Variable param : arglist) {
                    context.addVariable(param);
                }

                // only contains variable nodes, return nodes and control
                // structures
                while (defCopy.getChildren().size() > 0) {
                    Node firstChild = defCopy.getChildren().get(0);
                    if (firstChild instanceof ReturnNode) {
                        ReturnNode returnNode = ((ReturnNode) firstChild);
                        value = returnNode.evaluate(context);
                        break;
                    }
                    defCopy.replaceNode(firstChild, new ArrayList<Node>(
                            firstChild.traverse(context)));
                }
            } finally {
                context.closeVariableScope(previousScope);
            }
        } catch (Exception e) {
            Logger.getLogger(FunctionCall.class.getName()).log(Level.SEVERE,
                    null, e);
        }
        if (value == null) {
            throw new ParseException("Function " + invocation.getFunctionName()
                    + " did not return a value", invocation);
        }
        return value;
    }

}
