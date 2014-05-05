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
package com.vaadin.sass.internal.selector;

import java.util.Collection;
import java.util.LinkedHashSet;

public class SelectorSet extends LinkedHashSet<Selector> {

    public SelectorSet() {
        super();
    }

    public SelectorSet(Selector selector) {
        add(selector);
    }

    public SelectorSet(Collection<Selector> selectorCollection) {
        addAll(selectorCollection);
    }

    /**
     * Removes each selector which is subsumed by another selector.
     */
    public SelectorSet eliminateRedundantSelectors() {
        SelectorSet filtered = new SelectorSet();
        for (Selector s1 : this) {
            // Find most general selector matching s1
            Selector s = s1;
            for (Selector s2 : this) {
                if (s2.subsumes(s)) {
                    s = s2;
                }
            }
            filtered.add(s);
        }
        return filtered;
    }
}
