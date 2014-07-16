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

import java.util.Collection;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.Node;

public class IfNode extends Node implements IfElseNode, IVariableNode {
    private SassListItem expression;

    public IfNode(SassListItem expression) {
        if (expression == null) {
            expression = LexicalUnitImpl.createIdent("false");
        }
        this.expression = expression;
    }

    private IfNode(IfNode nodeToCopy) {
        super(nodeToCopy);
        expression = nodeToCopy.expression;
    }

    @Override
    public SassListItem getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "@if " + expression.toString();
    }

    @Override
    public void replaceVariables(ScssContext context) {
        expression = expression.replaceVariables(context);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return traverseChildren(context);
    }

    @Override
    public IfNode copy() {
        return new IfNode(this);
    }
}