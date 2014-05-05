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
package com.vaadin.sass.internal.selector;

import com.vaadin.sass.internal.tree.VariableNode;

/**
 * Single CSS3 attribute selector such as "[attribute]" or "[attribute~=value]".
 * 
 * See also {@link SimpleSelectorSequence} and {@link Selector}.
 */
public class AttributeSelector extends SimpleSelector {

    public enum MatchRelation {
        EQUALS("="), INCLUDES("~="), DASHMATCH("|="), PREFIXMATCH("^="), SUFFIXMATCH(
                "$="), SUBSTRINGMATCH("*=");

        private String value;

        private MatchRelation(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private String attribute;
    private MatchRelation matchRelation;
    private String value;

    /**
     * Constructs an attribute selector with an attribute name and optional
     * match relation and value. Match relation and value should both be set or
     * both be null.
     * 
     * @param attribute
     *            attribute name
     * @param matchRelation
     *            CSS3 match relation enum value
     * @param value
     *            string value to compare against
     */
    public AttributeSelector(String attribute, MatchRelation matchRelation,
            String value) {
        this.attribute = attribute;
        this.matchRelation = matchRelation;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public MatchRelation getMatchRelation() {
        return matchRelation;
    }

    private String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (getValue() == null) {
            return "[" + getAttribute() + "]";
        } else {
            return "[" + getAttribute() + getMatchRelation() + getValue() + "]";
        }
    }

    @Override
    public AttributeSelector replaceVariable(VariableNode var) {
        String newAttribute = var.replaceInterpolation(attribute);
        String newValue = (value == null) ? null : var
                .replaceInterpolation(value);
        return new AttributeSelector(newAttribute, matchRelation, newValue);
    }

}
