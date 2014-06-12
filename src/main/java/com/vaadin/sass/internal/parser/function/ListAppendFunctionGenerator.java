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

import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

public class ListAppendFunctionGenerator extends ListFunctionGenerator {

    private static String[] argumentNames = { "list", "val", "separator" };
    private static SassListItem[] defaultValues = { null, null,
            LexicalUnitImpl.createIdent("auto") };

    public ListAppendFunctionGenerator() {
        super(createArgumentList(argumentNames, defaultValues, false), "append");
    }

    @Override
    protected SassListItem computeForArgumentList(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem listAsItem = getParam(actualArguments, "list");
        SassListItem appendItem = getParam(actualArguments, "val");

        SassList list = asList(listAsItem);
        ArrayList<SassListItem> newList = new ArrayList<SassListItem>();
        for (SassListItem item : list) {
            newList.add(item);
        }
        newList.add(appendItem);

        SassList.Separator sep = getSeparator(getParam(actualArguments,
                "separator"));
        if (sep == null) { // determine the separator in "auto" mode
            sep = getAutoSeparator(list);
        }
        return new SassList(sep, newList);
    }
}