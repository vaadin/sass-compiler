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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.controldirective.TemporaryNode;

public abstract class Node implements Serializable {

    public static BuildStringStrategy PRINT_STRATEGY = new PrintStrategy();

    public static BuildStringStrategy TO_STRING_STRATEGY = new ToStringStrategy();

    private static final long serialVersionUID = 5914711715839294816L;

    private ArrayList<Node> children = null;

    private Node parentNode;

    public Node() {
    }

    protected Node(Node nodeToCopy) {
        if (nodeToCopy != null && nodeToCopy.children != null) {
            setChildren(nodeToCopy.copyChildren());
        }
    }

    /**
     * Replace the child oldChild with a collection of nodes.
     * 
     * @param oldChild
     *            child to replace
     * @param newNodes
     *            replacing nodes, can be an empty collection
     */
    public void replaceNode(Node oldChild, Collection<? extends Node> newNodes) {
        appendAfterNode(oldChild, newNodes);
        if (!newNodes.contains(oldChild)) {
            oldChild.removeFromParent();
        }
    }

    private void appendAfterNode(Node after, Collection<? extends Node> newNodes) {
        if (newNodes != null && !newNodes.isEmpty()) {
            // try to find last node with "after" as its original node and
            // append after it
            for (int i = getChildren(false).size() - 1; i >= 0; --i) {
                Node node = getChildren(false).get(i);
                if (node == after) {
                    getChildren(true).addAll(i + 1, newNodes);
                    for (final Node child : newNodes) {
                        child.removeFromParent();
                        child.parentNode = this;
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
    // TODO this should be avoided except when constructing the node tree
    public void appendChild(Node node) {
        if (node != null) {
            getChildren(true).add(node);
            node.removeFromParent();
            node.parentNode = this;
        }
    }

    /**
     * Remove this node from its parent (if any).
     */
    private void removeFromParent() {
        if (getParentNode() != null) {
            getParentNode().getChildren(true).remove(this);
            parentNode = null;
        }
    }

    public List<Node> getChildren() {
        return Collections.unmodifiableList(getChildren(false));
    }

    // avoid calling this method whenever possible
    @Deprecated
    protected void setChildren(Collection<Node> newChildren) {
        children = new ArrayList<Node>(newChildren);
        // add new children
        for (Node child : newChildren) {
            child.parentNode = this;
        }
    }

    private List<Node> getChildren(boolean create) {
        if (children == null && create) {
            children = new ArrayList<Node>();
        }
        if (children != null) {
            return children;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Method for manipulating the data contained within the {@link Node}.
     * 
     * Traversing a node is allowed to modify the node, replace it with one or
     * more nodes at the same or later position in its parent and modify the
     * children of the node, but not modify or remove preceding nodes in its
     * parent. Traversing a node is also allowed to modify the definitions
     * currently in scope as its side-effect.
     * 
     * @param context
     *            current compilation context
     * @return nodes replacing the current node
     */
    public abstract Collection<Node> traverse(ScssContext context);

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

    public Node getNormalParentNode() {
        Node parent = getParentNode();
        while (parent instanceof TemporaryNode) {
            parent = parent.getParentNode();
        }
        return parent;
    }

    /**
     * Copy a node (deep copy including children).
     * 
     * The copy is detached from the original tree, with null as parent, and
     * data that is not relevant to handling of function or mixin expansion is
     * not copied.
     * 
     * @return copy of the node
     */
    public abstract Node copy();

    // to be used primarily from inside the class Node
    protected Collection<Node> copyChildren() {
        if (getChildren(false).isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node child : getChildren(false)) {
            result.add(child.copy());
        }
        return result;
    }

    public Collection<Node> traverseChildren(ScssContext context) {
        return traverseChildren(context, true);
    }

    protected Collection<Node> traverseChildren(ScssContext context,
            boolean newScope) {
        List<Node> children = getChildren();
        if (!children.isEmpty()) {
            if (newScope) {
                context.openVariableScope();
            }
            try {
                ArrayList<Node> result = new ArrayList<Node>();
                for (Node child : new ArrayList<Node>(children)) {
                    result.addAll(child.traverse(context));
                }
                // TODO this ugly but hard to eliminate as long as some classes
                // use traverseChildren() for its side-effects
                setChildren(result);
                return result;
            } finally {
                if (newScope) {
                    context.closeVariableScope();
                }
            }
        } else {
            return Collections.emptyList();
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
