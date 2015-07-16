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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.css.sac.CSSException;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.tree.ImportNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.NodeWithUrlContent;
import com.vaadin.sass.internal.tree.RuleNode;
import com.vaadin.sass.internal.tree.controldirective.TemporaryNode;

public class ImportNodeHandler {

    public static Collection<Node> traverse(ScssContext context,
            ImportNode importNode) {
        ScssStylesheet styleSheet = importNode.getStylesheet();
        // top-level case
        if (styleSheet == null) {
            // iterate to parents of node, find ScssStylesheet
            Node parent = importNode.getParentNode();
            while (parent != null && !(parent instanceof ScssStylesheet)) {
                parent = parent.getParentNode();
            }
            if (parent instanceof ScssStylesheet) {
                styleSheet = (ScssStylesheet) parent;
            }
        }
        if (styleSheet == null) {
            SCSSErrorHandler.get().traverseError(
                    "Nested import in an invalid context");
            return Collections.emptyList();
        }
        if (!importNode.isPureCssImport()) {
            List<Node> importedChildren = Collections.emptyList();
            ScssStylesheet imported = null;
            try {
                // set parent's charset to imported node.

                imported = ScssStylesheet.get(importNode.getUri(), styleSheet,
                        new SCSSDocumentHandlerImpl(), SCSSErrorHandler.get());
                if (imported == null) {
                    SCSSErrorHandler.get().traverseError(
                            "Import '" + importNode.getUri() + "' in '"
                                    + styleSheet.getFileName()
                                    + "' could not be found");
                    return Collections.emptyList();
                }

                String prefix = styleSheet.getPrefix()
                        + getUrlPrefix(importNode.getUri());
                if (!"".equals(prefix)) {
                    // support resolving nested imports relative to prefix
                    imported.setPrefix(prefix);
                    updateUrlInImportedSheet(imported, prefix, imported,
                            context);
                }

                importedChildren = new ArrayList<Node>(imported.getChildren());
            } catch (Exception e) {
                SCSSErrorHandler.get().traverseError(e);
                return Collections.emptyList();
            }

            if (imported != null) {
                // traverse the imported nodes normally in the correct context
                Node tempParent = new TemporaryNode(importNode.getParentNode(),
                        importedChildren);
                Collection<Node> result = tempParent.traverseChildren(context);

                styleSheet.addSourceUris(imported.getSourceUris());
                return result;
            }
        } else {
            if (styleSheet != importNode.getParentNode()) {
                SCSSErrorHandler
                        .get()
                        .traverseError(
                                "CSS imports can only be used at the top level, not as nested imports. Within style rules, use SCSS imports.");
                return Collections.emptyList();

            }
        }
        return Collections.singleton((Node) importNode);
    }

    private static String getUrlPrefix(String url) {
        if (url == null) {
            return "";
        }
        int pos = url.lastIndexOf('/');
        if (pos == -1) {
            return "";
        }
        return url.substring(0, pos + 1);
    }

    private static void updateUrlInImportedSheet(Node node, String prefix,
            ScssStylesheet styleSheet, ScssContext context) {
        ScssContext.UrlMode urlMode = context.getUrlMode();
        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            Node newChild = child;
            if (child instanceof NodeWithUrlContent
                    && (urlMode.equals(ScssContext.UrlMode.RELATIVE) || (urlMode
                            .equals(ScssContext.UrlMode.MIXED) && child instanceof RuleNode))) {
                newChild = (Node) ((NodeWithUrlContent) child)
                        .updateUrl(prefix);
                node.replaceNodeAt(i, newChild);
            } else if (child instanceof ImportNode) {
                ((ImportNode) child).setStylesheet(styleSheet);
            }
            updateUrlInImportedSheet(newChild, prefix, styleSheet, context);
        }
    }
}
