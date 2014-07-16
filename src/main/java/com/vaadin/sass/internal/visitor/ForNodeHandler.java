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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.ForNode;

public class ForNodeHandler extends LoopNodeHandler {

    public static Collection<Node> traverse(ScssContext context, ForNode forNode) {
        int fromInt = getInt(context, forNode.getFrom());
        int toInt = getInt(context, forNode.getTo());
        if (forNode.isExclusive()) {
            toInt = toInt - 1;
        }
        Collection<Variable> indices = new ArrayList<Variable>();
        for (int idx = fromInt; idx <= toInt; ++idx) {
            LexicalUnitImpl idxUnit = LexicalUnitImpl.createInteger(forNode
                    .getFrom().getLineNumber(), forNode.getFrom()
                    .getColumnNumber(), idx);
            indices.add(new Variable(forNode.getVariableName(), idxUnit));
        }
        return replaceLoopNode(context, forNode, indices);
    }

    private static int getInt(ScssContext context, SassListItem item) {
        item = item.replaceVariables(context);
        SassListItem value = item
                .evaluateFunctionsAndExpressions(context, true);
        if (value instanceof LexicalUnitImpl
                && ((LexicalUnitImpl) value).isNumber()) {
            return ((LexicalUnitImpl) value).getIntegerValue();
        }
        throw new ParseException(
                "The loop indices of @for must evaluate to integer values",
                item);
    }

}
