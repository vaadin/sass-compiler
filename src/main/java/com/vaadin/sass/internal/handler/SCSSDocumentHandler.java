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

package com.vaadin.sass.internal.handler;

import java.util.Collection;
import java.util.List;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.SACMediaList;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.StringInterpolationSequence;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.selector.Selector;

public interface SCSSDocumentHandler extends DocumentHandler {
    ScssStylesheet getStyleSheet();

    void variable(String name, SassListItem value, boolean guarded);

    void startMixinDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs);

    void endMixinDirective();

    void startFunctionDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs);

    void endFunctionDirective();

    void debugDirective(String message);

    void warnDirective(String message);

    void startForDirective(String var, SassListItem from, SassListItem to,
            boolean exclusive);

    void endForDirective();

    void startWhileDirective(SassListItem evaluator);

    void endWhileDirective();

    void startNestedProperties(StringInterpolationSequence name);

    void endNestedProperties(StringInterpolationSequence name);

    void importStyle(String uri, SACMediaList media, boolean isURL);

    void property(StringInterpolationSequence name, SassListItem value,
            boolean important, String comment);

    void startEachDirective(String variable, SassListItem list);

    void endEachDirective();

    void startIfElseDirective();

    void endIfElseDirective();

    void ifDirective(SassListItem evaluator);

    void elseDirective();

    void startSelector(List<Selector> selectors) throws CSSException;

    void endSelector() throws CSSException;

    void extendDirective(List<Selector> list, boolean optional);

    void microsoftDirective(String name, StringInterpolationSequence value);

    void startKeyFrames(String keyframeName,
            StringInterpolationSequence animationname);

    void endKeyFrames();

    void startKeyframeSelector(String selector);

    void endKeyframeSelector();

    void contentDirective();

    void returnDirective(SassListItem expr);

    void startInclude(String name, List<Variable> args, boolean hasVariableArgs);

    void endInclude();

}
