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
package com.vaadin.sass.internal.parser.function;

import java.util.ArrayList;

import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

public class ListAppendFunctionGenerator extends ListFunctionGenerator {

    public ListAppendFunctionGenerator() {
        super("append");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        ActualArgumentList params = function.getParameterList();
        if (params == null || params.size() < 2 || params.size() > 3) {
            throw new ParseException(
                    "The function append() must have two or three parameters. Actual parameters: "
                            + params);
        }
        SassListItem listAsItem = params.get(0);
        SassListItem appendItem = params.get(1);

        SassList list = asList(listAsItem);
        ArrayList<SassListItem> newList = new ArrayList<SassListItem>();
        for (SassListItem item : list) {
            newList.add(item);
        }
        newList.add(appendItem);

        SassList.Separator sep = null; // this corresponds to "auto"
        if (params.size() == 3) { // get the specified list separator
            sep = getSeparator(params.get(2));
        }
        if (sep == null) { // determine the separator in "auto" mode
            sep = getAutoSeparator(list);
        }
        return new SassList(sep, newList);
    }
}