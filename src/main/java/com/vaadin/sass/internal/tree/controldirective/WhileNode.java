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
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.visitor.WhileNodeHandler;

public class WhileNode extends Node {

    private SassListItem condition;

    public WhileNode(SassListItem condition) {
        this.condition = condition;
    }

    private WhileNode(WhileNode nodeToCopy) {
        super(nodeToCopy);
        condition = nodeToCopy.condition;
    }

    @Override
    public String toString() {
        return "While Node: { condition: " + condition + "}";
    }

    public SassListItem getCondition() {
        return condition;
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return WhileNodeHandler.traverse(context, this);
    }

    @Override
    public WhileNode copy() {
        return new WhileNode(this);
    }

}
