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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.selector.Selector;
import com.vaadin.sass.internal.selector.SelectorSet;
import com.vaadin.sass.internal.selector.SimpleSelectorSequence;
import com.vaadin.sass.internal.tree.BlockNode;
import com.vaadin.sass.internal.tree.ExtendNode;
import com.vaadin.sass.internal.tree.Node;

public class ExtendNodeHandler {
    /*
     * TODOs:
     * 
     * - Print a warning when unification of an @extend-clause fails, unless
     * !optional specified. This may require some rework of the way the extends
     * map is built, as currently the connection to the @extend declaration is
     * lost.
     */

    /**
     * Maps each @extend-selector (its simple selector sequence) to a set of of
     * its containing block's selectors. E.g. the following extensions:
     * 
     * a { @extend b; ... }; b { @extend b,c; ... }
     * 
     * corresponds to the following map:
     * 
     * { b={a,b}, c={b} }
     * 
     */
    private static Map<SimpleSelectorSequence, SelectorSet> extendsMap = new HashMap<SimpleSelectorSequence, SelectorSet>();

    public static void traverse(ExtendNode node) throws Exception {
        for (Selector s : node.getList()) {
            if (!s.isSimple()) {
                // @extend-selectors must not be nested
                throw new ParseException(
                        "Nested selector not allowed in @extend-clause");
            }
            if (node.getParentNode() instanceof BlockNode) {
                SelectorSet containedBlockSelectors = new SelectorSet(
                        ((BlockNode) node.getParentNode()).getSelectorList());
                SimpleSelectorSequence extendSelector = s.firstSimple();
                if (extendsMap.get(extendSelector) == null) {
                    extendsMap.put(extendSelector, containedBlockSelectors);
                } else {
                    extendsMap.get(extendSelector).addAll(
                            containedBlockSelectors);
                }
            }
        }
    }

    public static void clear() {
        if (extendsMap != null) {
            extendsMap.clear();
        }
    }

    public static void modifyTree(Node node) throws Exception {
        Iterator<Node> nodeIt = node.getChildren().iterator();

        while (nodeIt.hasNext()) {
            final Node child = nodeIt.next();

            if (child instanceof BlockNode) {
                BlockNode blockNode = (BlockNode) child;
                List<Selector> selectorList = blockNode.getSelectorList();
                SelectorSet newSelectors = new SelectorSet();
                for (Selector selector : selectorList) {
                    newSelectors.addAll(createSelectorsForExtensions(selector,
                            extendsMap));
                }

                // remove any selector duplicated in the initial list of
                // selectors
                newSelectors.removeAll(selectorList);

                selectorList.addAll(newSelectors);

                // remove all placeholder selectors
                Iterator<Selector> it = selectorList.iterator();
                while (it.hasNext()) {
                    Selector s = it.next();
                    if (s.isPlaceholder()) {
                        it.remove();
                    }
                }

                // remove block if selector list is empty
                if (selectorList.isEmpty()) {
                    nodeIt.remove();
                } else {
                    blockNode.setSelectorList(selectorList);
                }
            }
        }

    }

    /**
     * Try to unify argument selector with each selector specified in an
     * extend-clause. For each match found, add the enclosing block's selectors
     * with substitutions (see examples below). Finally eliminates redundant
     * selectors (a selector is redundant in a set if subsumed by another
     * selector in the set).
     * 
     * .a {...}; .b { @extend .a } ---> .b
     * 
     * .a.b {...}; .c { @extend .a } ---> .b.c
     * 
     * .a.b {...}; .c .c { @extend .a } ---> .c .b.c
     * 
     * @param target
     *            the selector to match
     * @param extendsMap
     *            maps from the simple selector sequence of the extend-selector
     *            to a set of extending selectors
     * 
     * @return the generated selectors (may contain duplicates)
     */
    public static SelectorSet createSelectorsForExtensions(Selector target,
            Map<SimpleSelectorSequence, SelectorSet> extendsMap) {
        SelectorSet newSelectors = new SelectorSet();
        createSelectorsForExtensionsRecursively(target, newSelectors,
                extendsMap);
        return newSelectors.eliminateRedundantSelectors();
    }

    /**
     * Create all selector extensions matching target. Mutable collection for
     * efficiency. Recursively applied to generated selectors.
     * 
     * Optimization: May be inefficient since we may unify the same selector
     * several times. It would be a good idea to cache unifications in a map as
     * we go along.
     */
    private static void createSelectorsForExtensionsRecursively(
            Selector target, SelectorSet current,
            Map<SimpleSelectorSequence, SelectorSet> extendsMap) {

        SelectorSet newSelectors = new SelectorSet();
        for (SimpleSelectorSequence extendSelector : extendsMap.keySet()) {
            for (Selector extendingBlockSelector : extendsMap
                    .get(extendSelector)) {
                newSelectors.add(target.replace(extendSelector,
                        extendingBlockSelector));
            }
            current.addAll(newSelectors);
        }

        for (SimpleSelectorSequence extendSelector : extendsMap.keySet()) {
            // invoke recursively for transitivity, removing the processed
            // extends mapping to avoid looping (there may be be more efficient
            // ways of achieving this...)
            Map<SimpleSelectorSequence, SelectorSet> lesserExtendsMap = new HashMap<SimpleSelectorSequence, SelectorSet>(
                    extendsMap);
            lesserExtendsMap.remove(extendSelector);
            for (Selector newSel : newSelectors) {
                createSelectorsForExtensionsRecursively(newSel, current,
                        lesserExtendsMap);
            }
        }
    }
}
