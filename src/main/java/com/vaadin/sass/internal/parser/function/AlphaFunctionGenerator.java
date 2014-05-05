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
import com.vaadin.sass.internal.util.ColorUtil;

public class AlphaFunctionGenerator extends AbstractFunctionGenerator {

    public AlphaFunctionGenerator() {
        super("alpha", "opacity");
    }

    @Override
    public SassListItem compute(LexicalUnitImpl function) {
        checkParameters(function);
        LexicalUnitImpl color = (LexicalUnitImpl) function.getParameterList()
                .get(0);
        float opacity = 1.0f;
        if (ColorUtil.isRgba(color)) {
            SassList parameterList = color.getParameterList();
            SassListItem last = parameterList.get(parameterList.size() - 1);
            opacity = ((LexicalUnitImpl) last).getFloatValue();
        }
        return LexicalUnitImpl.createNumber(function.getLineNumber(),
                function.getColumnNumber(), null, opacity);
    }

    private void checkParameters(LexicalUnitImpl function) {
        SassList params = function.getParameterList();
        if (params.size() != 1) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires exactly one parameter", function);
        }
        LexicalUnitImpl color = (LexicalUnitImpl) params.get(0);
        if (!(color instanceof LexicalUnitImpl)
                || (!ColorUtil.isColor(color) && !ColorUtil.isRgba(color))) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires a color as its first parameter", function);
        }
    }
}
