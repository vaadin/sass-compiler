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

import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

public class ListLengthFunctionGenerator extends AbstractFunctionGenerator {

    public ListLengthFunctionGenerator() {
        super("length");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        SassList params = function.getParameterList();
        if (params == null || params.size() != 1) {
            throw new ParseException(
                    "The function length() requires exactly one parameter. Actual parameters: "
                            + params);
        }
        int length;
        SassListItem list = params.get(0);
        if (list instanceof SassList) {
            length = ((SassList) list).size();
        } else {
            length = 1;
        }
        return LexicalUnitImpl.createInteger(function.getLineNumber(),
                function.getColumnNumber(), length);
    }

}
