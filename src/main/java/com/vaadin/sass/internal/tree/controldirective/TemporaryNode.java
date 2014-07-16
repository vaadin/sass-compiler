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

package com.vaadin.sass.internal.tree.controldirective;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.tree.Node;

/**
 * Temporary node used when expanding loops.
 */
public class TemporaryNode extends Node {

    private Node parentNode;

    public TemporaryNode(Node parent) {
        parentNode = parent;
    }

    public TemporaryNode(Node parent, Collection<Node> children) {
        parentNode = parent;
        setChildren(children);
    }

    @Override
    public Node getParentNode() {
        return parentNode;
    }

    @Override
    public String toString() {
        return "Temporary Node: { " + getChildren().size() + " children }";
    }

    @Override
    public Collection<Node> traverseChildren(ScssContext context) {
        return traverseChildren(context, false);
    }

    // ugly but restricts the scope of manipulation of node hierarchy
    public void appendAndTraverse(ScssContext context, Node node) {
        // node needs to be appended before traversal so it can access its
        // parent and grandparent
        appendChild(node);
        replaceNode(node, new ArrayList<Node>(node.traverse(context)));
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        // this is like traverseChildren(false) except that this does not modify
        // the child list of the node
        ArrayList<Node> children = new ArrayList<Node>(getChildren());
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node node : children) {
            result.addAll(node.traverse(context));
        }
        return result;
    }

    @Override
    public Node copy() {
        throw new UnsupportedOperationException(
                "TemporaryNode cannot be copied");
    }

}
