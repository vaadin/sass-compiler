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

import java.util.ArrayList;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.selector.Selector;
import com.vaadin.sass.internal.tree.BlockNode;

/**
 * Handle nesting of blocks by moving child blocks to their parent, updating
 * their selector lists while doing so. Also parent selectors (&amp;) are
 * handled here.
 * 
 * Sample SASS code (from www.sass-lang.com):
 * 
 * <pre>
 * table.hl {
 *   margin: 2em 0;
 *   td.ln {
 *     text-align: right;
 *   }
 * }
 * </pre>
 * 
 * Note that nested properties are handled by {@link NestedNodeHandler}, not
 * here.
 */
public class BlockNodeHandler {

    public static void traverse(BlockNode node) {

        if (node.getChildren().size() == 0) {
            // empty blocks are also removed later
            node.removeFromParent();
            return;
        }

        if (node.getParentNode() instanceof BlockNode) {
            replaceParentSelectors(node);

        } else if (node.getSelectors().contains("&")) {
            ScssStylesheet.warning("Base-level rule contains"
                    + " the parent-selector-referencing character '&';"
                    + " the character will be removed:\n" + node);
            removeParentReference(node);
        }
    }

    /**
     * Goes through the selector list of the given BlockNode and removes the '&'
     * character from the selectors.
     * 
     * @param node
     */
    private static void removeParentReference(BlockNode node) {
        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for (Selector sel : node.getSelectorList()) {
            newSelectors.add(sel.replaceParentReference(null));
        }

        node.setSelectorList(newSelectors);
    }

    private static void replaceParentSelectors(BlockNode node) {
        BlockNode parentBlock = (BlockNode) node.getParentNode();

        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for (Selector parentSel : parentBlock.getSelectorList()) {
            for (Selector sel : node.getSelectorList()) {
                newSelectors.add(sel.replaceParentReference(parentSel));
            }
        }

        node.setSelectorList(newSelectors);

        parentBlock.getParentNode().adoptGrandChild(node);
    }
}
