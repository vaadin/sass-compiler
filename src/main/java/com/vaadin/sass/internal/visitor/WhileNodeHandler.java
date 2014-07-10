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
package com.vaadin.sass.internal.visitor;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.TemporaryNode;
import com.vaadin.sass.internal.tree.controldirective.WhileNode;

public class WhileNodeHandler {

    /**
     * Replace a WhileNode with the expanded set of nodes.
     * 
     * @param whileNode
     *            node to replace
     */
    public static Collection<Node> traverse(WhileNode whileNode) {
        Node parent = whileNode.getParentNode();
        ArrayList<Node> result = new ArrayList<Node>();
        while (evaluateCondition(whileNode)) {
            ArrayList<Node> nodes = iteration(whileNode);
            if (nodes.size() == 0) {
                throw new ParseException(
                        "@while loop iteration did nothing, infinite loop",
                        whileNode);
            }
            TemporaryNode temp = new TemporaryNode(parent, nodes);
            result.addAll(temp.traverse());
        }
        return result;
    }

    private static boolean evaluateCondition(WhileNode whileNode) {
        SassListItem condition = whileNode.getCondition();
        ScssContext context = whileNode.getContext();
        condition = condition.replaceVariables(context);
        condition = condition.evaluateFunctionsAndExpressions(context, true);
        return BinaryOperator.isTrue(condition);
    }

    private static ArrayList<Node> iteration(WhileNode whileNode) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (final Node child : whileNode.getChildren()) {
            Node copy = child.copy();
            replaceVariables(copy);
            nodes.add(copy);
        }
        return nodes;
    }

    private static void replaceVariables(Node copy) {
        if (copy instanceof IVariableNode) {
            IVariableNode n = (IVariableNode) copy;
            n.replaceVariables();
        }

        for (Node c : copy.getChildren()) {
            replaceVariables(c);
        }
    }

}
