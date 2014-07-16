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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.selector.Selector;
import com.vaadin.sass.internal.visitor.ExtendNodeHandler;

public class ExtendNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 3301805078983796878L;

    private List<Selector> list;
    private boolean optional;

    public ExtendNode(List<Selector> list, boolean optional) {
        super();
        this.list = list;
        this.optional = optional;
    }

    private ExtendNode(ExtendNode nodeToCopy) {
        super(nodeToCopy);
        list = new ArrayList<Selector>(nodeToCopy.list);
        optional = nodeToCopy.optional;
    }

    public List<Selector> getList() {
        return list;
    }

    @Override
    public void replaceVariables(ScssContext context) {

    }

    @Override
    public String toString() {
        return "Extend node [" + getListAsString() + "]";
    }

    public String getListAsString() {
        StringBuilder b = new StringBuilder();
        for (final Selector s : list) {
            b.append(s.toString());
        }

        return b.toString();
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        try {
            // TODO here or later?
            traverseChildren(context);
            return ExtendNodeHandler.traverse(context, this);
        } catch (Exception e) {
            Logger.getLogger(ExtendNode.class.getName()).log(Level.SEVERE,
                    null, e);
            // TODO is it correct to ignore the exception?
            return Collections.emptyList();
        }
    }

    @Override
    public ExtendNode copy() {
        return new ExtendNode(this);
    }
}
