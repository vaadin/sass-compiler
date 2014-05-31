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

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.tree.MixinNode;
import com.vaadin.sass.internal.tree.Node;

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
        MixinDefNode defClone = mixinDef.copy();
        defClone.traverse();

        defClone.replaceContentDirective(mixinNode);

        ArrayList<Node> children = new ArrayList<Node>(defClone.getChildren());
        if (!mixinDef.getArglist().isEmpty()) {
            defClone.replacePossibleArguments(mixinNode.getArglist());
            for (final Node child : children) {
                replaceChildVariables(defClone, child);
            }
        }
        // might have changed
        children = new ArrayList<Node>(defClone.getChildren());
        mixinNode.getParentNode().replaceNode(mixinNode, children);
        for (Node child : children) {
            child.traverse();
        }
    }

    private static void replaceChildVariables(MixinDefNode mixinDef, Node node) {
        for (final Node child : node.getChildren()) {
            replaceChildVariables(mixinDef, child);
        }
        if (node instanceof IVariableNode) {
            ((IVariableNode) node).replaceVariables(mixinDef.getArglist()
                    .getArguments());
        }
    }
}
