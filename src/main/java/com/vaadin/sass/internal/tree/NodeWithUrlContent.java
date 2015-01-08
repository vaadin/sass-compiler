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

import com.vaadin.sass.internal.ScssContext;

/**
 * NodeWithUrlContent is implemented by Nodes that may contain updatable urls.
 * The updating of urls is a mechanism that makes it possible to specify the
 * locations of resources with respect to the folder of an scss file: for
 * instance, if an imported style sheet bar.scss is located in the folder foo,
 * url(baz.png) appearing in bar.scss is transformed into url(foo/baz.png) when
 * using relative urls. See {@link ScssContext} for an explanation of the
 * supported url modes. The implementation of handling the url modes is in
 * ImportNodeHandler.updateUrlInImportedSheet.
 */
public interface NodeWithUrlContent {
    /**
     * Returns a new Node that is otherwise identical to this but has all urls
     * updated by adding the specified prefix and cleaning the resulting path
     * to, e.g., eliminate redundant parent folder references. Does not modify
     * this node.
     * 
     * @param prefix
     *            the prefix to be added to all urls contained in this Node.
     */
    public NodeWithUrlContent updateUrl(String prefix);
}