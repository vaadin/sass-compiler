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

import java.util.ArrayList;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.util.DeepCopy;

public abstract class ListModifyNode extends Node implements IVariableNode {

    protected SassListItem list;
    protected SassListItem modify;
    protected SassList.Separator separator = null;
    protected String variable;

    public String getNewVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return "List append node [var = " + variable + " , list =" + list
                + ", separator =" + separator + ", modify =" + modify + "]";
    }

    public VariableNode getModifiedList() {
        SassListItem newList = (SassListItem) DeepCopy.copy(list);
        newList = modifyList(newList);

        VariableNode node = new VariableNode(variable.substring(1), newList,
                false);
        return node;
    }

    protected abstract SassListItem modifyList(SassListItem newList);

    protected void setSeparator(String separator) {
        String lowerCase = "";
        if (separator == null
                || (lowerCase = separator.toLowerCase()).equals("auto")) {
            // The separator will be chosen when modifying the list
            this.separator = null;
        } else if (lowerCase.equals("comma")) {
            this.separator = SassList.Separator.COMMA;
        } else if (lowerCase.equals("space")) {
            this.separator = SassList.Separator.SPACE;
        }
    }

    protected void populateList(SassListItem list, SassListItem modify) {
        this.list = (SassListItem) DeepCopy.copy(list);
        this.modify = (SassListItem) DeepCopy.copy(modify);
    }

    @Override
    public void replaceVariables(ArrayList<VariableNode> variables) {
        list = list.replaceVariables(variables);
        modify = modify.replaceVariables(variables);
    }

    @Override
    public void traverse() {
        replaceVariables(ScssStylesheet.getVariables());
        ScssStylesheet.addVariable(getModifiedList());
        getParentNode().removeChild(this);
    }

}
