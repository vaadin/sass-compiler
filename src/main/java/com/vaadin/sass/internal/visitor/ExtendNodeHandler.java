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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
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

    public static Collection<Node> traverse(ScssContext context, ExtendNode node)
            throws Exception {
        for (Selector s : node.getList()) {
            if (!s.isSimple()) {
                // @extend-selectors must not be nested
                throw new ParseException(
                        "Nested selector not allowed in @extend-clause");
            }
            if (node.getNormalParentNode() instanceof BlockNode) {
                BlockNode parentBlock = (BlockNode) node.getNormalParentNode();
                SimpleSelectorSequence extendSelector = s.firstSimple();
                for (Selector sel : parentBlock.getSelectorList()) {
                    Collection<Selector> ctx = parentBlock
                            .getParentSelectors();
                    context.addExtension(
                            new Extension(extendSelector, sel, ctx));
                }
            }
        }
        return Collections.emptyList();
    }

    public static void modifyTree(ScssContext context, Node node)
            throws Exception {
        Iterator<Node> nodeIt = new ArrayList<Node>(node.getChildren())
                .iterator();

        while (nodeIt.hasNext()) {
            final Node child = nodeIt.next();

            if (child instanceof BlockNode) {
                BlockNode blockNode = (BlockNode) child;
                // need a copy as the selector list is modified below
                List<Selector> selectorList = new ArrayList<Selector>(
                        blockNode.getSelectorList());
                SelectorSet newSelectors = new SelectorSet();
                for (Selector selector : selectorList) {
                    // keep order while avoiding duplicates
                    newSelectors.add(selector);
                    newSelectors.addAll(createSelectorsForExtensions(selector,
                            context.getExtensions()));
                }

                // remove all placeholder selectors
                Iterator<Selector> it = newSelectors.iterator();
                while (it.hasNext()) {
                    Selector s = it.next();
                    if (s.isPlaceholder()) {
                        it.remove();
                    }
                }

                // remove block if selector list is empty
                if (newSelectors.isEmpty()) {
                    blockNode.getParentNode().replaceNode(blockNode,
                            Collections.<Node> emptyList());
                } else {
                    blockNode.setSelectorList(new ArrayList<Selector>(
                            newSelectors));
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
     *            mapping from the simple selector sequence of the
     *            extend-selector to an extending selector
     * 
     * @return the generated selectors (may contain duplicates)
     */
    public static SelectorSet createSelectorsForExtensions(Selector target,
            Iterable<Extension> extendsMap) {
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
            Selector target, SelectorSet current, Iterable<Extension> extendsMap) {

        List<Selector> newSelectors = new ArrayList<Selector>();
        List<Extension> extensionsForNewSelectors = new ArrayList<Extension>();
        for (Extension extension : extendsMap) {
            newSelectors.add(target.replace(extension));
            extensionsForNewSelectors.add(extension);
        }
        current.addAll(newSelectors);

        for (Extension extension : extendsMap) {
            Collection<Extension> singleExt = Collections.singleton(extension);
            for (int i = 0; i < newSelectors.size(); ++i) {
                // avoid infinite loops
                if (extensionsForNewSelectors.get(i) != extension) {
                    createSelectorsForExtensionsRecursively(
                            newSelectors.get(i), current, singleExt);
                }
            }
        }
    }
}
