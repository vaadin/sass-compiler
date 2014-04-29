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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.VariableArgumentList;
import com.vaadin.sass.internal.util.DeepCopy;

/**
 * NodeWithVariableArguments is used as a superclass for nodes that handle
 * argument lists with support for variable arguments. When variable arguments
 * are used, a NodeWithVariableArguments expands a list into separate arguments,
 * whereas a DefNode packs several arguments into a list.
 * NodeWithVariableArguments is currently used as a superclass for MixinNode and
 * FunctionNode. The corresponding definition nodes are subclasses of DefNode.
 * 
 * @author Vaadin
 * 
 */
public abstract class NodeWithVariableArguments extends Node implements
        IVariableNode {

    // these are the actual parameter values, not whether the definition node
    // uses varargs
    private List<VariableNode> arglist = new ArrayList<VariableNode>();
    private SassList.Separator sep = SassList.Separator.COMMA;
    private boolean hasVariableArguments;
    private String name;

    public NodeWithVariableArguments(String name,
            Collection<VariableNode> args, boolean hasVariableArgs) {
        super();
        if (args != null && !args.isEmpty()) {
            args = DeepCopy.copy(args);
            arglist.addAll(args);
        }
        hasVariableArguments = hasVariableArgs;
        this.name = name;
    }

    public NodeWithVariableArguments(String name, SassList args) {
        super();
        hasVariableArguments = args instanceof VariableArgumentList;
        this.name = name;

        List<VariableNode> actualParams = new ArrayList<VariableNode>();
        for (int i = 0; i < args.size(); ++i) {
            actualParams.add(new VariableNode(null, args.get(i), false));
        }
        // when using this constructor, args contents already evaluated so can
        // perform expandVariableArguments immediately
        arglist = expandVariableArguments(actualParams, hasVariableArguments);
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }

    public List<VariableNode> getArglist() {
        return arglist;
    }

    protected void setArglist(List<VariableNode> arglist) {
        this.arglist = arglist;
    }

    public SassList.Separator getSeparator() {
        return sep;
    }

    protected void updateSeparator(SassListItem expr,
            boolean hasVariableArguments) {
        if (hasVariableArguments) {
            if (expr instanceof SassList) {
                SassList lastList = (SassList) expr;
                if (lastList.size() > 1) {
                    sep = lastList.getSeparator();
                    return;
                }
            }
        }
        sep = SassList.Separator.COMMA;
    }

    protected static List<VariableNode> expandVariableArguments(
            List<VariableNode> arglist, boolean hasVariableArguments) {
        /*
         * If there are variable arguments, the last argument is expanded into
         * separate arguments.
         * 
         * Note that the separator character should be preserved when an
         * 
         * @include expands variables into separate arguments and the
         * corresponding @mixin packs them again into a list. Some cases have
         * not yet been verified to work as they should.
         * 
         * To illustrate the cases, suppose that there is a mixin with variable
         * arguments @mixin foo($a1, $a2, ..., $ak...). That is used by an
         * include with variable arguments: @include foo($b1, $b2, ..., $bl...).
         * Then the include will expand the argument bl into separate arguments,
         * if bl is a list. The mixin will pack possibly several arguments into
         * a list ak. The cases are then
         * 
         * 1) k = l. Then ak will be a list equal to bl. To retain the
         * separator, it needs to be taken from the list bl.
         * 
         * 2) l < k. Now ak will be a sublist of bl, the first elements of bl
         * will be used for the parameters a(l+1), ..., a(k-1). If a list should
         * retain the separator, its sublist should also have the same
         * separator.
         * 
         * 3) l > k, the uncertain and only partially verified case. Now, ak
         * will be a list that contains the parameters b(k+1), ..., b(l-1) and
         * the contents of the list bl. Using the separator of the list bl means
         * that the same separator will also separate the parameters b(k+1)...
         * from each other in the list ak. That is the approach adopted here,
         * but it is only based on a limited amount of testing.
         * 
         * The separator of a one-element list is considered to be a comma here.
         * 
         * 
         * Also note that the named and unnamed parameters are stored in two
         * separate lists. The named parameters packed into a variable argument
         * list cannot be accessed inside the mixin. While this is unexpected,
         * it seems to be the desired behavior, although only a limited amount
         * of testing has been done to verify this.
         */
        if (hasVariableArguments) {
            List<VariableNode> newArglist = new ArrayList<VariableNode>(arglist);
            VariableNode last = newArglist.get(newArglist.size() - 1);
            newArglist.remove(newArglist.size() - 1);
            SassListItem expr = last.getExpr();
            if (expr instanceof SassList) {
                SassList lastList = (SassList) expr;
                for (SassListItem item : lastList) {
                    newArglist.add(new VariableNode(null, item, false));
                }
            } else {
                newArglist.add(new VariableNode(null, expr, false));
            }
            // Append any remaining variable name-value pairs to the argument
            // list
            if (expr instanceof VariableArgumentList) {
                for (VariableNode namedNode : ((VariableArgumentList) expr)
                        .getNamedVariables()) {
                    newArglist.add(namedNode.copy());
                }
            }
            return newArglist;
        }
        return arglist;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /**
     * Replace variable references with their values in the argument list and
     * name.
     */
    @Override
    public void replaceVariables(Collection<VariableNode> variables) {
        for (final VariableNode arg : getArglist()) {
            SassListItem expr = arg.getExpr().replaceVariables(variables);
            expr = expr.replaceFunctions();
            arg.setExpr(expr);
        }
    }

    @Override
    public void traverse() {
        // limit variable scope
        Map<String, VariableNode> variableScope = ScssStylesheet
                .openVariableScope();
        try {
            doTraverse();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        } finally {
            ScssStylesheet.closeVariableScope(variableScope);
        }
    }

    protected abstract void doTraverse() throws Exception;

}