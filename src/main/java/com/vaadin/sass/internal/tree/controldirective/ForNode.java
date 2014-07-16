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
import com.vaadin.sass.internal.visitor.ForNodeHandler;

public class ForNode extends Node {

    private final String variableName;
    private final SassListItem from;
    private final SassListItem to;
    private final boolean exclusive;

    public ForNode(String variableName, SassListItem from, SassListItem to,
            boolean exclusive) {
        super();
        this.variableName = variableName;
        this.from = from;
        this.to = to;
        this.exclusive = exclusive;
    }

    private ForNode(ForNode nodeToCopy) {
        super(nodeToCopy);
        variableName = nodeToCopy.variableName;
        from = nodeToCopy.from;
        to = nodeToCopy.to;
        exclusive = nodeToCopy.exclusive;
    }

    public String getVariableName() {
        return variableName;
    }

    public SassListItem getFrom() {
        return from;
    }

    public SassListItem getTo() {
        return to;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public String toString() {
        return "For Node: " + "{variable: " + variableName + ", from:" + from
                + ", to: "
                + to + ", exclusive: " + exclusive;
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return ForNodeHandler.traverse(context, this);
    }

    @Override
    public ForNode copy() {
        return new ForNode(this);
    }

}
