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

import com.vaadin.sass.internal.util.DeepCopy;

public class MixinDefNode extends DefNode {
    private static final long serialVersionUID = 5469294053247343948L;

    public MixinDefNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        super(name, args, hasVariableArgs);
    }

    @Override
    public String toString() {
        return "Mixin Definition Node: {name: " + getName() + ", args: "
                + getArglist().size() + "}";
    }

    /**
     * This should only happen on a cloned MixinDefNode, since it changes the
     * Node itself.
     * 
     * @param mixinNode
     * @return
     */
    public MixinDefNode replaceContentDirective(MixinNode mixinNode) {
        return findAndReplaceContentNodeInChildren(this, mixinNode);
    }

    private MixinDefNode findAndReplaceContentNodeInChildren(Node node,
            MixinNode mixinNode) {
        ContentNode contentNode = null;
        for (Node child : new ArrayList<Node>(node.getChildren())) {
            if (child instanceof ContentNode) {
                contentNode = (ContentNode) child;
                replaceContentNode(contentNode, mixinNode);
            } else {
                findAndReplaceContentNodeInChildren(child, mixinNode);
            }
        }
        return this;
    }

    public MixinDefNode replaceContentNode(ContentNode contentNode,
            MixinNode mixinNode) {
        if (contentNode != null) {
            contentNode.getParentNode().appendChildrenAfter(
                    DeepCopy.copy(mixinNode.getChildren()), contentNode);
            contentNode.getParentNode().removeChild(contentNode);
        }
        return this;
    }

    @Override
    public MixinDefNode copy() {
        return (MixinDefNode) super.copy();
    }
}
