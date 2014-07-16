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

import java.util.Collection;
import java.util.Collections;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.StringInterpolationSequence;

public class KeyframesNode extends Node implements IVariableNode {
    private String keyframeName;
    private StringInterpolationSequence animationName;

    public KeyframesNode(String keyframeName,
            StringInterpolationSequence animationName) {
        this.keyframeName = keyframeName;
        this.animationName = animationName;
    }

    private KeyframesNode(KeyframesNode nodeToCopy) {
        super(nodeToCopy);
        keyframeName = nodeToCopy.keyframeName;
        animationName = nodeToCopy.animationName;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Key frames node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        replaceVariables(context);
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    @Override
    public void replaceVariables(ScssContext context) {
        animationName = animationName.replaceVariables(context);
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder();
        string.append(keyframeName).append(" ").append(animationName)
                .append(" {\n");
        for (Node child : getChildren()) {
            string.append("\t\t").append(strategy.build(child)).append("\n");
        }
        string.append("\t}");
        return string.toString();
    }

    @Override
    public KeyframesNode copy() {
        return new KeyframesNode(this);
    }
}