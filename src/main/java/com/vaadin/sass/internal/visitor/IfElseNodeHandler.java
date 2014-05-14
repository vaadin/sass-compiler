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

import org.w3c.flute.parser.ParseException;

import com.vaadin.sass.internal.parser.BinaryBooleanExpression;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.ElseNode;
import com.vaadin.sass.internal.tree.controldirective.IfElseDefNode;
import com.vaadin.sass.internal.tree.controldirective.IfNode;

public class IfElseNodeHandler {

    public static void traverse(IfElseDefNode node) throws Exception {

        for (final Node child : node.getChildren()) {
            if (child instanceof IfNode) {
                if (BinaryBooleanExpression.evaluate(((IfNode) child)
                        .getExpression())) {
                    replaceDefNodeWithCorrectChild(node, node.getParentNode(),
                            child);
                    break;
                }
            } else {
                if (!(child instanceof ElseNode)
                        && node.getChildren().indexOf(child) == node
                                .getChildren().size() - 1) {
                    throw new ParseException(
                            "Invalid @if/@else in scss file for " + node);
                } else {
                    replaceDefNodeWithCorrectChild(node, node.getParentNode(),
                            child);
                    break;
                }
            }
        }

        node.getParentNode().removeChild(node);
    }

    private static void replaceDefNodeWithCorrectChild(IfElseDefNode defNode,
            Node parent, final Node child) {
        Node previous = defNode;
        for (final Node n : new ArrayList<Node>(child.getChildren())) {
            parent.appendChild(n, previous);
            previous = n;
        }
    }
}
