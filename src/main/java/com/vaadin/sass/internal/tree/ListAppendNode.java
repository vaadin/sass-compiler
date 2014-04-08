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

import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

public class ListAppendNode extends ListModifyNode {

    public ListAppendNode(String variable, SassListItem list,
            SassListItem append, String separator) {
        this.variable = variable;
        setSeparator(separator);
        populateList(list, append);
    }

    protected void checkSeparator() {
        if (separator != null) {
            if (list.size() > 1) {
                separator = list.getSeparator();
            } else if (modify.size() > 1) {
                separator = modify.getSeparator();
            } else {
                separator = SassList.Separator.SPACE;
            }
        }
    }

    @Override
    protected SassListItem modifyList(SassListItem newList) {
        checkSeparator();
        SassList modifiedList = newList.addAllItems(modify);
        modifiedList.setSeparator(separator);
        return modifiedList;
    }

}
