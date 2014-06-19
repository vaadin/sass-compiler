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

import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.tree.controldirective.ForNode;

public class ForNodeHandler extends LoopNodeHandler {

    public static void traverse(ForNode node) {
        replaceForNode(node);
    }

    private static void replaceForNode(ForNode forNode) {
        int fromInt = getInt(forNode.getFrom());
        int toInt = getInt(forNode.getTo());
        if (forNode.isExclusive()) {
            toInt = toInt - 1;
        }
        Collection<VariableNode> indices = new ArrayList<VariableNode>();
        for (int idx = fromInt; idx <= toInt; ++idx) {
            LexicalUnitImpl idxUnit = LexicalUnitImpl.createInteger(forNode
                    .getFrom().getLineNumber(), forNode.getFrom()
                    .getColumnNumber(), idx);
            indices.add(new VariableNode(forNode.getVariableName(), idxUnit,
                    false));
        }
        replaceLoopNode(forNode, indices);
    }

    private static int getInt(SassListItem item) {
        item = item.replaceVariables();
        SassListItem value = item.evaluateFunctionsAndExpressions(true);
        if (value instanceof LexicalUnitImpl
                && ((LexicalUnitImpl) value).isNumber()) {
            return ((LexicalUnitImpl) value).getIntegerValue();
        }
        throw new ParseException(
                "The loop indices of @for must evaluate to integer values",
                item);
    }

}
