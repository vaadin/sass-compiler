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
import java.util.Collections;

import org.w3c.flute.parser.ParseException;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.expression.BinaryOperator;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.ElseNode;
import com.vaadin.sass.internal.tree.controldirective.IfElseDefNode;
import com.vaadin.sass.internal.tree.controldirective.IfNode;
import com.vaadin.sass.internal.tree.controldirective.TemporaryNode;

public class IfElseNodeHandler {

    public static Collection<Node> traverse(ScssContext context,
            IfElseDefNode node)
            throws Exception {
        for (final Node child : node.getChildren()) {
            if (child instanceof IfNode) {
                SassListItem expression = ((IfNode) child).getExpression();
                expression = expression.replaceVariables(context);
                expression = expression.evaluateFunctionsAndExpressions(
                        context, true);

                if (BinaryOperator.isTrue(expression)) {
                    return traverseChild(context, node.getParentNode(), child);
                }
            } else {
                if (!(child instanceof ElseNode)
                        && node.getChildren().indexOf(child) == node
                                .getChildren().size() - 1) {
                    throw new ParseException(
                            "Invalid @if/@else in scss file for " + node);
                } else {
                    return traverseChild(context, node.getParentNode(), child);
                }
            }
        }
        // no matching branch
        return Collections.emptyList();
    }

    private static Collection<Node> traverseChild(ScssContext context,
            Node parent, Node child) {
        TemporaryNode tempParent = new TemporaryNode(parent,
                child.getChildren());
        return tempParent.traverse(context);
    }

}
