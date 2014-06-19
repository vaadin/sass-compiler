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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.DeepCopy;

public abstract class Node implements Serializable {

    public static BuildStringStrategy PRINT_STRATEGY = new PrintStrategy();

    public static BuildStringStrategy TO_STRING_STRATEGY = new ToStringStrategy();

    private static final long serialVersionUID = 5914711715839294816L;

    private ArrayList<Node> children;

    private Node parentNode;

    /**
     * Nodes based on which this node was generated - either replacing them or
     * appended after them. This is used to append new nodes after all already
     * appended nodes based on the same node.
     */
    private Collection<Node> originalNodes = new ArrayList<Node>(
            Collections.singleton(this));

    public Node() {
        children = new ArrayList<Node>();
    }

    /**
     * Replace the child oldNode with a collection of nodes.
     * 
     * @param oldChild
     *            child to replace
     * @param newNodes
     *            replacing nodes, can be an empty collection
     */
    public void replaceNode(Node oldChild, Collection<? extends Node> newNodes) {
        appendAfterNode(oldChild, newNodes);
        oldChild.removeFromParent();
    }

    public void appendAfterNode(Node after, Collection<? extends Node> newNodes) {
        if (newNodes != null && !newNodes.isEmpty()) {
            // try to find last node with "after" as its original node and
            // append after it
            for (int i = children.size() - 1; i >= 0; --i) {
                Node node = children.get(i);
                if (node == after || node.originalNodes.contains(after)) {
                    children.addAll(i + 1, newNodes);
                    for (final Node child : newNodes) {
                        child.removeFromParent();
                        child.setParentNode(this);
                        child.originalNodes.addAll(after.originalNodes);
                    }
                    return;
                }
            }
            throw new ParseException("after-node was not found", after);
        }
    }

    /**
     * Append a new child node to the end of the child list. This method should
     * only be used when constructing the Node tree, not when modifying it.
     * 
     * @param node
     *            new child to append
     */
    public void appendChild(Node node) {
        if (node != null) {
            children.add(node);
            node.removeFromParent();
            node.setParentNode(this);
        }
    }

    /**
     * Remove this node from its parent (if any).
     */
    public void removeFromParent() {
        if (getParentNode() != null) {
            getParentNode().children.remove(this);
            setParentNode(null);
        }
    }

    // users should avoid relying on this being mutable - currently
    // ExtendNodeHandler does
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Method for manipulating the data contained within the {@link Node}.
     * 
     * Traversing a node is allowed to modify the node, replace it with one or
     * more nodes at the same or later position in its parent and modify the
     * children of the node, but not modify or remove preceding nodes in its
     * parent.
     */
    public abstract void traverse();

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * Result value could be null.
     * 
     * @return State as a string
     */
    public String printState() {
        return null;
    }

    public Node getParentNode() {
        return parentNode;
    }

    private void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public Node copy() {
        // these do not need to be copied
        Node oldParent = parentNode;
        Collection<Node> oldOriginalNodes = originalNodes;
        parentNode = null;
        originalNodes.clear();

        Node copy = (Node) DeepCopy.copy(this);
        copy.parentNode = oldParent;

        parentNode = oldParent;
        originalNodes = oldOriginalNodes;
        return copy;
    }

    public void traverseChildren() {
        ScssStylesheet.openVariableScope();
        try {
            for (Node child : new ArrayList<Node>(getChildren())) {
                child.traverse();
            }
        } finally {
            ScssStylesheet.closeVariableScope();
        }
    }

    public static interface BuildStringStrategy {

        String build(Node node);

        String build(SassListItem expr);

        String build(ActualArgumentList expr);
    }

    public static class PrintStrategy implements BuildStringStrategy {

        @Override
        public String build(Node node) {
            return node.printState();
        }

        @Override
        public String build(SassListItem expr) {
            return expr.printState();
        }

        @Override
        public String build(ActualArgumentList expr) {
            return expr.printState();
        }

    }

    public static class ToStringStrategy implements BuildStringStrategy {

        @Override
        public String build(Node node) {
            return node.toString();
        }

        @Override
        public String build(SassListItem expr) {
            return expr.toString();
        }

        @Override
        public String build(ActualArgumentList expr) {
            return expr.toString();
        }

    }

}
