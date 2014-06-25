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

import com.vaadin.sass.internal.Scope;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.tree.MixinNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.VariableNode;

public class MixinNodeHandler {

    public static void traverse(MixinNode node) throws ParseException {
        replaceMixins(node);
    }

    private static void replaceMixins(MixinNode node) throws ParseException {
        MixinDefNode mixinDef = ScssStylesheet.getMixinDefinition(node
                .getName());
        if (mixinDef == null) {
            throw new ParseException("Mixin Definition: " + node.getName()
                    + " not found");
        }
        replaceMixinNode(node, mixinDef);
    }

    private static void replaceMixinNode(MixinNode mixinNode,
            MixinDefNode mixinDef) {
        MixinDefNode defClone = mixinDef.copy();

        defClone.replaceContentDirective(mixinNode);

        if (!mixinDef.getArglist().isEmpty()) {
            defClone.replacePossibleArguments(mixinNode.getArglist());
            defClone.replaceVariables();
        }

        // parameters have been evaluated in parent scope, rest should be
        // in the scope where the mixin was defined
        Scope previousScope = ScssStylesheet.openVariableScope(defClone
                .getDefinitionScope());
        try {
            // add variables from argList
            for (VariableNode var : defClone.getArglist().getArguments()) {
                VariableNode evaluated = new VariableNode(var.getName(), var
                        .getExpr().evaluateFunctionsAndExpressions(true), false);
                ScssStylesheet.addVariable(evaluated);
            }
            // traverse child nodes in this scope
            ArrayList<Node> children = new ArrayList<Node>(
                    defClone.getChildren());
            mixinNode.getParentNode().replaceNode(mixinNode, children);
            for (Node child : children) {
                child.traverse();
            }
        } finally {
            ScssStylesheet.closeVariableScope(previousScope);
        }
    }
}
