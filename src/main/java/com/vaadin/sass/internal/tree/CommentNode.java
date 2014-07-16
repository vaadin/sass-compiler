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

public class CommentNode extends Node {
    private final String comment;

    public CommentNode(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String printState() {
        return comment;
    }

    @Override
    public String toString() {
        return "Comment node [" + comment + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return Collections.singleton((Node) this);
    }

    @Override
    public CommentNode copy() {
        return new CommentNode(comment);
    }
}
