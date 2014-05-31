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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.DeepCopy;

public abstract class Node implements Serializable {

    public static BuildStringStrategy PRINT_STRATEGY = new PrintStrategy();

    public static BuildStringStrategy TO_STRING_STRATEGY = new ToStringStrategy();

    private static final long serialVersionUID = 5914711715839294816L;

    protected ArrayList<Node> children;

    protected Node parentNode;

    // used to keep track of where to append nodes moved to parent
    // map from node after which you want to add a node to the last node added
    // to it
    // TODO it would be better if this can be eliminated and replaceNode() used
    private Map<Node, Node> lastAdded = null;

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
        appendChildrenAfter(newNodes, oldChild);
        oldChild.removeFromParent();
    }

    private void appendChildrenAfter(
            Collection<? extends Node> childrenNodes, Node after) {
        if (childrenNodes != null && !childrenNodes.isEmpty()) {
            int index = children.indexOf(after);
            if (index != -1) {
                children.addAll(index + 1, childrenNodes);
                for (final Node child : childrenNodes) {
                    child.removeFromParent();
                    child.setParentNode(this);
                }
            } else {
                throw new NullPointerException("after-node was not found");
            }
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

    /**
     * Move the grand child node grandChild to be a direct child of this node,
     * adding it after its old parent and keeping track of other nodes added
     * after the same child.
     * 
     * This method should be avoided if possible, use
     * {@link #replaceNode(Node, Collection)} instead.
     * 
     * @param grandChild
     */
    public void adoptGrandChild(Node grandChild) {
        Node oldParent = grandChild.getParentNode();
        appendChildrenAfter(Collections.singleton(grandChild),
                getLastAdded(oldParent));
        setLastAdded(grandChild, oldParent);
    }

    private Node getLastAdded(Node after) {
        if (lastAdded == null) {
            return after;
        } else {
            Node node = lastAdded.get(after);
            if (node != null) {
                return node;
            } else {
                return after;
            }
        }
    }

    private void setLastAdded(Node added, Node after) {
        if (lastAdded == null) {
            lastAdded = new HashMap<Node, Node>();
        }
        // update the insert points also when doing multi-level un-nesting
        List<Node> nodesToUpdate = new ArrayList<Node>();
        nodesToUpdate.add(after);
        for (Entry<Node, Node> entry : lastAdded.entrySet()) {
            if (entry.getValue() == after) {
                nodesToUpdate.add(entry.getKey());
            }
        }
        for (Node node : nodesToUpdate) {
            lastAdded.put(node, added);
        }
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
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
        // clear map when node is moved
        // this shouldn't happen in the middle of handling children
        lastAdded = null;
    }

    public Node copy() {
        Node oldParent = parentNode;
        parentNode = null;
        Node copy = (Node) DeepCopy.copy(this);
        copy.parentNode = oldParent;
        parentNode = oldParent;
        return copy;
    }

    public void traverseChildren() {
        Map<String, VariableNode> variableScope = ScssStylesheet.openVariableScope();
        try {
            for (Node child : new ArrayList<Node>(getChildren())) {
                child.traverse();
            }
        } finally {
            ScssStylesheet.closeVariableScope(variableScope);
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
