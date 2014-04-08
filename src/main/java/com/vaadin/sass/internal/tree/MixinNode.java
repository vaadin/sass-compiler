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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.VariableArgumentList;
import com.vaadin.sass.internal.util.DeepCopy;
import com.vaadin.sass.internal.visitor.MixinNodeHandler;

public class MixinNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 4725008226813110658L;

    private String name;
    private ArrayList<VariableNode> arglist;
    private SassList.Separator sep = SassList.Separator.COMMA;
    private boolean hasVariableArguments;

    public MixinNode(String name) {
        this.name = name;
        arglist = new ArrayList<VariableNode>();
    }

    public MixinNode(String name, Collection<VariableNode> args,
            boolean hasVariableArgs) {
        this(name);
        if (args != null && !args.isEmpty()) {
            args = DeepCopy.copy(args);
            arglist.addAll(args);
        }
        hasVariableArguments = hasVariableArgs;
    }

    @Override
    public String printState() {
        return "name: " + name + " args: " + arglist;
    }

    @Override
    public String toString() {
        return "Mixin node [" + printState() + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }

    public ArrayList<VariableNode> getArglist() {
        return arglist;
    }

    public void setArglist(ArrayList<VariableNode> arglist) {
        this.arglist = arglist;
    }

    public SassList.Separator getSeparator() {
        return sep;
    }

    /**
     * Replace variable references with their values in the mixin argument list.
     */
    @Override
    public void replaceVariables(ArrayList<VariableNode> variables) {
        for (final VariableNode arg : arglist) {
            arg.setExpr(arg.getExpr().replaceVariables(variables));
        }
        for (final VariableNode var : variables) {
            if (name.startsWith("$")) {
                if (name.equals("$" + var.getName())) {
                    name = var.getExpr().printState();
                }
            } else if (name.startsWith("#{") && name.endsWith("}")) {
                if (name.equals("#{$" + var.getName() + "}")) {
                    name = var.getExpr().printState();
                }
            }
        }
    }

    private void expandVariableArguments() {
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
            VariableNode last = arglist.get(arglist.size() - 1);
            SassListItem expr = last.getExpr();
            if (expr.size() > 1) {
                sep = expr.getSeparator();
            }
            arglist.remove(arglist.size() - 1);

            for (SassListItem item : expr) {
                SassListItem newArg = (SassListItem) DeepCopy.copy(item);
                VariableNode newArgNode = new VariableNode(null, newArg, false);
                arglist.add(newArgNode);
            }
            // Append any remaining variable name-value pairs to the argument
            // list
            if (expr instanceof VariableArgumentList) {
                for (VariableNode namedNode : ((VariableArgumentList) expr)
                        .getNamedVariables()) {
                    VariableNode newArgNode = (VariableNode) DeepCopy
                            .copy(namedNode);
                    arglist.add(newArgNode);
                }
            }
        }
    }

    protected void replaceVariablesForChildren() {
        for (Node child : getChildren()) {
            if (child instanceof IVariableNode) {
                ((IVariableNode) child).replaceVariables(ScssStylesheet
                        .getVariables());
            }
        }
    }

    @Override
    public void traverse() {
        try {
            // limit variable scope to the mixin
            Map<String, VariableNode> variableScope = ScssStylesheet
                    .openVariableScope();

            replaceVariables(ScssStylesheet.getVariables());
            expandVariableArguments();
            replaceVariablesForChildren();
            MixinNodeHandler.traverse(this);

            ScssStylesheet.closeVariableScope(variableScope);

        } catch (Exception e) {
            Logger.getLogger(MixinNode.class.getName()).log(Level.SEVERE, null,
                    e);
        }
    }

}
