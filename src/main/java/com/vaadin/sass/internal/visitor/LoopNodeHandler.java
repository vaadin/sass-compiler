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

import java.util.Collection;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.TemporaryNode;

/**
 * Base class for handlers of all kinds of looping nodes (@for, @while, @each).
 */
public abstract class LoopNodeHandler {

    /**
     * Replace a loop node (e.g. ForNode) with the expanded set of nodes.
     * 
     * @param context
     *            current compilation context
     * @param loopNode
     *            node to replace
     * @param loopVariables
     *            iterable of the loop variable instances for each iteration -
     *            typically a collection for a fixed iteration count loop
     */
    protected static Collection<Node> replaceLoopNode(ScssContext context,
            Node loopNode, Iterable<Variable> loopVariables) {
        // the type of this node does not matter much as long as it can have
        // children that can be traversed
        TemporaryNode tempParent = new TemporaryNode(loopNode.getParentNode());
        for (final Variable var : loopVariables) {
            iteration(context, loopNode.getChildren(), tempParent, var);
        }
        // the newly created nodes have already been traversed
        return tempParent.getChildren();
    }

    private static void iteration(ScssContext context, List<Node> loopChildren,
            TemporaryNode newParent, Variable loopVar) {
        context.openVariableScope();
        try {
            context.addVariable(loopVar);
            for (final Node child : loopChildren) {
                Node copy = child.copy();
                newParent.appendAndTraverse(context, copy);
            }
        } finally {
            context.closeVariableScope();
        }
    }
}
