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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

/**
 * Transient class representing a function call to a custom (user-defined)
 * function. This class is used to evaluate the function call and is discarded
 * after use. A FunctionNode does not have a parent in the stylesheet node tree.
 */
public class FunctionNode extends Node implements IVariableNode {
    private static final long serialVersionUID = -5383104165955523923L;

    private FunctionDefNode def;
    private LexicalUnitImpl invocation;

    public FunctionNode(FunctionDefNode def, LexicalUnitImpl invocation) {
        super();
        this.def = def;
        this.invocation = invocation;
    }

    @Override
    public String toString() {
        return "Function Node: {name: " + invocation.getFunctionName()
                + ", args: " + invocation.getParameterList() + "}";
    }

    public SassListItem evaluate() {
        SassList params = invocation.getParameterList();
        if (params.size() != def.getArglist().size()) {
            throw new ParseException(
                    "Incorrect number of parameters to the function "
                            + invocation.getFunctionName(), invocation);
        }
        // TODO varargs etc.
        List<VariableNode> actualParams = new ArrayList<VariableNode>();
        for (int i = 0; i < params.size(); ++i) {
            actualParams.add(new VariableNode(
                    def.getArglist().get(i).getName(), params.get(i), false));
        }

        // simple case - no need for a new scope as only a single return
        // statement
        ArrayList<Node> defChildren = def.getChildren();
        if (defChildren.size() == 1 && defChildren.get(0) instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) defChildren.get(0);
            return returnNode.evaluate(actualParams);
        }

        // limit variable scope to the function
        Map<String, VariableNode> variableScope = ScssStylesheet
                .openVariableScope();

        try {
            for (VariableNode param : actualParams) {
                ScssStylesheet.addVariable(param);
            }

            // copying is necessary as traversal modifies the parent of the
            // node
            FunctionDefNode defCopy = def.copy();
            // only contains variable nodes, return nodes and control
            // structures
            for (Node node : new ArrayList<Node>(defCopy.getChildren())) {
                node.traverse();
            }

            ReturnNode returnNode = getReturnNode(defCopy.getChildren());
            // parameters are already in the scope
            return returnNode.evaluate(Collections.<VariableNode> emptyList());
        } finally {
            ScssStylesheet.closeVariableScope(variableScope);
        }
    }

    private ReturnNode getReturnNode(ArrayList<Node> defChildren) {
        if (defChildren.size() != 1
                || !(defChildren.get(0) instanceof ReturnNode)) {
            throw new ParseException(
                    "Function evaluation failed - did not result in one return statement",
                    invocation);
        }
        return (ReturnNode) defChildren.get(0);
    }

    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        // TODO move some of the code here?
    }

    // as the node is not in the tree for an ScssStylesheet, this is currently
    // not called
    @Override
    public void traverse() {
        // replaceVariables(ScssStylesheet.getVariables());
    }

}
