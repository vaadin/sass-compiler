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
import java.util.Collections;

import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.VariableNode;

/**
 * Base class for handlers of all kinds of looping nodes (@for, @while, @each).
 */
public abstract class LoopNodeHandler {

    /**
     * Replace a loop node (e.g. ForNode) with the expanded set of nodes.
     * 
     * @param loopNode
     *            node to replace
     * @param loopVariables
     *            iterable of the loop variable instances for each iteration -
     *            typically a collection for a fixed iteration count loop
     */
    protected static void replaceLoopNode(Node loopNode,
            Iterable<VariableNode> loopVariables) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (final VariableNode var : loopVariables) {
            nodes.addAll(iteration(loopNode, var));
        }
        loopNode.getParentNode().replaceNode(loopNode, nodes);

        for (Node node : nodes) {
            node.traverse();
        }
    }

    private static ArrayList<Node> iteration(Node loopNode, VariableNode loopVar) {
        Collection<VariableNode> variables = Collections.singleton(loopVar);

        ArrayList<Node> nodes = new ArrayList<Node>();
        for (final Node child : loopNode.getChildren()) {
            Node copy = child.copy();
            replaceLoopVariable(copy, variables);
            nodes.add(copy);
        }
        return nodes;
    }

    private static void replaceLoopVariable(Node copy,
            Collection<VariableNode> variables) {
        if (copy instanceof IVariableNode) {
            IVariableNode n = (IVariableNode) copy;
            n.replaceVariables(variables);
        }

        for (Node c : copy.getChildren()) {
            replaceLoopVariable(c, variables);
        }
    }
}
