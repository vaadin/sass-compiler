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

public class FontFaceNode extends Node {

    public FontFaceNode() {
    }

    private FontFaceNode(FontFaceNode nodeToCopy) {
        super(nodeToCopy);
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "FontFace node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder builder = new StringBuilder();
        builder.append("@font-face {\n");

        for (final Node child : getChildren()) {
            builder.append("\t" + strategy.build(child) + "\n");
        }

        builder.append("}");
        return builder.toString();
    }

    @Override
    public FontFaceNode copy() {
        return new FontFaceNode(this);
    }

}
