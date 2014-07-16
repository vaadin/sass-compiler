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

public class KeyframeSelectorNode extends Node {
    private String selector;

    public KeyframeSelectorNode(String selector) {
        this.selector = selector;
    }

    private KeyframeSelectorNode(KeyframeSelectorNode nodeToCopy) {
        super(nodeToCopy);
        selector = nodeToCopy.selector;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Key frame selector node [" + buildString(TO_STRING_STRATEGY)
                + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    public String buildString(BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder();
        string.append(selector).append(" {\n");
        for (Node child : getChildren()) {
            string.append("\t\t").append(strategy.build(child)).append("\n");
        }
        string.append("\t}");
        return string.toString();
    }

    @Override
    public KeyframeSelectorNode copy() {
        return new KeyframeSelectorNode(this);
    }
}
