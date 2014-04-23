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
import java.util.List;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.VariableArgumentList;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.tree.MixinNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.util.DeepCopy;

public class MixinNodeHandler {

    public static void traverse(MixinNode node) throws Exception {
        replaceMixins(node);
    }

    private static void replaceMixins(MixinNode node) throws Exception {
        MixinDefNode mixinDef = ScssStylesheet.getMixinDefinition(node
                .getName());
        if (mixinDef == null) {
            throw new Exception("Mixin Definition: " + node.getName()
                    + " not found");
        }
        replaceMixinNode(node, mixinDef);
    }

    private static void replaceMixinNode(MixinNode mixinNode,
            MixinDefNode mixinDef) {
        MixinDefNode defClone = (MixinDefNode) DeepCopy.copy(mixinDef);
        defClone.traverse();

        defClone.replaceContentDirective(mixinNode);

        if (mixinDef.getArglist().isEmpty()) {
            mixinNode.getParentNode().appendChildrenAfter(
                    new ArrayList<Node>(defClone.getChildren()), mixinNode);
        } else {
            if (mixinNode.getArglist() != null
                    && !mixinNode.getArglist().isEmpty()) {
                replacePossibleArguments(mixinNode, defClone);
            }

            Node previous = mixinNode;
            for (final Node child : new ArrayList<Node>(defClone.getChildren())) {
                replaceChildVariables(defClone, child);
                mixinNode.getParentNode().appendChild(child, previous);
                previous = child;
            }

        }

        mixinNode.getParentNode().removeChild(mixinNode);
    }

    /**
     * We have to replace all the mixin parameters. This is done in two phases.
     * First phase replaces all the named parameters while the second replaces
     * in order of remaining unmodified parameters.
     * 
     * If there are variable arguments (the last argument is of the form $x...),
     * any remaining arguments are packaged into a list.
     */
    private static void replacePossibleArguments(MixinNode mixinNode,
            MixinDefNode def) {
        if (mixinNode.getArglist().size() > 0) {
            ArrayList<VariableNode> remainingDefArguments = new ArrayList<VariableNode>(
                    def.getArglist());
            ArrayList<VariableNode> remainingActualArguments = new ArrayList<VariableNode>(
                    mixinNode.getArglist());
            String varArgName = null;
            if (def.hasVariableArguments()) {
                varArgName = remainingDefArguments.get(
                        remainingDefArguments.size() - 1).getName();
            }
            for (final VariableNode unit : mixinNode.getArglist()) {
                if (unit.getName() != null) {
                    for (final VariableNode node : def.getArglist()) {
                        if (!node.getName().equals(varArgName)
                                && node.getName().equals(unit.getName())) {
                            node.setExpr(unit.getExpr());
                            remainingDefArguments.remove(node);
                            remainingActualArguments.remove(unit);
                            break;
                        }
                    }
                }
            }
            if (!def.hasVariableArguments()) {
                checkExtraParameters(mixinNode, remainingDefArguments.size(),
                        remainingActualArguments.size());
            }
            for (int i = 0; i < remainingDefArguments.size()
                    && i < remainingActualArguments.size(); i++) {
                VariableNode unit = remainingActualArguments.get(i);
                remainingDefArguments.get(i).setExpr(unit.getExpr());
            }
            checkForUnsetParameters(mixinNode, def);

            // If the mixin takes a variable number of arguments, the last
            // argument and any remaining arguments are packaged into (one or
            // two) lists. The unnamed and named arguments form separate lists.
            if (def.hasVariableArguments()) {
                List<SassListItem> unnamed = new ArrayList<SassListItem>();
                List<VariableNode> named = new ArrayList<VariableNode>();
                int lastIndex = def.getArglist().size() - 1;
                SassListItem last = def.getArglist().get(lastIndex).getExpr();
                if (last != null) {
                    unnamed.add(last);
                }

                for (int i = remainingDefArguments.size(); i < remainingActualArguments
                        .size(); i++) {
                    VariableNode unit = (VariableNode) DeepCopy
                            .copy(remainingActualArguments.get(i));
                    if (unit.getName() == null) {
                        unnamed.add(unit.getExpr());
                    } else {
                        named.add(new VariableNode(unit.getName(), unit
                                .getExpr(), false));
                        // The named arguments cannot be used inside the mixin
                        // but they can be passed to another mixin using
                        // variable arguments in an @include.
                    }
                }
                VariableArgumentList remaining = new VariableArgumentList(
                        mixinNode.getSeparator(), unnamed, named);
                def.getArglist().get(lastIndex).setExpr(remaining);
            }
        }
    }

    protected static void checkExtraParameters(MixinNode mixinNode,
            int remainingDefArguments, int remainingActualArguments) {
        if (remainingActualArguments > remainingDefArguments) {
            String errorMessage = "More parameters than expected, in Mixin "
                    + mixinNode.getName();
            throw new ParseException(errorMessage, mixinNode);
        }
    }

    /**
     * Checks whether all parameters of the mixin definition node defNode have
     * been set. Raises an exception if there are unset parameters.
     */
    protected static void checkForUnsetParameters(MixinNode node,
            MixinDefNode defNode) {
        ArrayList<VariableNode> arglist = defNode.getArglist();
        for (int i = 0; i < arglist.size() - 1; i++) {
            if (arglist.get(i) == null) {
                throw new ParseException(
                        "Less parameters than expected for mixin "
                                + defNode.getName(), node);
            }
        }
        if (!defNode.hasVariableArguments()
                && arglist.get(arglist.size() - 1) == null) {
            throw new ParseException("Less parameters than expected for mixin "
                    + defNode.getName(), node);
        }
    }

    private static void replaceChildVariables(MixinDefNode mixinDef, Node node) {
        for (final Node child : node.getChildren()) {
            replaceChildVariables(mixinDef, child);
        }
        if (node instanceof IVariableNode) {
            ((IVariableNode) node).replaceVariables(mixinDef.getArglist());
        }
    }
}
