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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.StringInterpolationSequence;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.selector.Selector;
import com.vaadin.sass.internal.tree.BlockNode;
import com.vaadin.sass.internal.tree.CommentNode;
import com.vaadin.sass.internal.tree.ConsoleMessageNode;
import com.vaadin.sass.internal.tree.ContentNode;
import com.vaadin.sass.internal.tree.ExtendNode;
import com.vaadin.sass.internal.tree.FontFaceNode;
import com.vaadin.sass.internal.tree.FunctionDefNode;
import com.vaadin.sass.internal.tree.ImportNode;
import com.vaadin.sass.internal.tree.KeyframeSelectorNode;
import com.vaadin.sass.internal.tree.KeyframesNode;
import com.vaadin.sass.internal.tree.MediaNode;
import com.vaadin.sass.internal.tree.MicrosoftRuleNode;
import com.vaadin.sass.internal.tree.MixinDefNode;
import com.vaadin.sass.internal.tree.MixinNode;
import com.vaadin.sass.internal.tree.NestPropertiesNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.ReturnNode;
import com.vaadin.sass.internal.tree.RuleNode;
import com.vaadin.sass.internal.tree.SimpleNode;
import com.vaadin.sass.internal.tree.VariableNode;
import com.vaadin.sass.internal.tree.controldirective.EachDefNode;
import com.vaadin.sass.internal.tree.controldirective.ElseNode;
import com.vaadin.sass.internal.tree.controldirective.ForNode;
import com.vaadin.sass.internal.tree.controldirective.IfElseDefNode;
import com.vaadin.sass.internal.tree.controldirective.IfNode;
import com.vaadin.sass.internal.tree.controldirective.WhileNode;

public class SCSSDocumentHandlerImpl implements SCSSDocumentHandler {

    private final ScssStylesheet styleSheet;
    Stack<Node> nodeStack = new Stack<Node>();

    public SCSSDocumentHandlerImpl() {
        this(new ScssStylesheet());
    }

    public SCSSDocumentHandlerImpl(ScssStylesheet styleSheet) {
        this.styleSheet = styleSheet;
        nodeStack.push(styleSheet);
    }

    @Override
    public ScssStylesheet getStyleSheet() {
        return styleSheet;
    }

    @Override
    public void startDocument(InputSource source) throws CSSException {
        nodeStack.push(styleSheet);
    }

    @Override
    public void endDocument(InputSource source) throws CSSException {
    }

    @Override
    public void variable(String name, SassListItem value, boolean guarded) {
        VariableNode node = new VariableNode(name, value, guarded);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void debugDirective(String message) {
        ConsoleMessageNode node = new ConsoleMessageNode(message, false);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void warnDirective(String message) {
        ConsoleMessageNode node = new ConsoleMessageNode(message, true);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void startForDirective(String var, SassListItem from,
            SassListItem to, boolean exclusive) {
        ForNode node = new ForNode(var, from, to, exclusive);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endForDirective() {
        nodeStack.pop();
    }

    @Override
    public void startEachDirective(String var, SassListItem list) {
        EachDefNode node = new EachDefNode(var, list);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endEachDirective() {
        nodeStack.pop();
    }

    @Override
    public void startWhileDirective(SassListItem condition) {
        WhileNode node = new WhileNode(condition);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endWhileDirective() {
        nodeStack.pop();
    }

    @Override
    public void comment(String text) throws CSSException {
        CommentNode node = new CommentNode(text);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void ignorableAtRule(String atRule) throws CSSException {
        log("ignorableAtRule(String atRule): " + atRule);
    }

    @Override
    public void namespaceDeclaration(String prefix, String uri)
            throws CSSException {
        log("namespaceDeclaration(String prefix, String uri): " + prefix + ", "
                + uri);
    }

    @Override
    public void importStyle(String uri, SACMediaList media,
            String defaultNamespaceURI) throws CSSException {
    }

    @Override
    public void startMedia(SACMediaList media) throws CSSException {
        MediaNode node = new MediaNode(media);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endMedia(SACMediaList media) throws CSSException {
        nodeStack.pop();
    }

    @Override
    public void startPage(String name, String pseudo_page) throws CSSException {
        log("startPage(String name, String pseudo_page): " + name + ", "
                + pseudo_page);
    }

    @Override
    public void endPage(String name, String pseudo_page) throws CSSException {
        log("endPage(String name, String pseudo_page): " + name + ", "
                + pseudo_page);
    }

    @Override
    public void startFontFace() throws CSSException {
        FontFaceNode node = new FontFaceNode();
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endFontFace() throws CSSException {
        nodeStack.pop();
    }

    @Override
    public void startSelector(List<Selector> selectors) throws CSSException {
        BlockNode node = new BlockNode(selectors);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endSelector() throws CSSException {
        nodeStack.pop();
    }

    public void property(StringInterpolationSequence name, SassListItem value,
            boolean important) throws CSSException {
        property(name, value, important, null);
    }

    @Override
    public void property(StringInterpolationSequence name, SassListItem value,
            boolean important, String comment) {
        RuleNode node = new RuleNode(name, value, important, comment);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void extendDirective(List<Selector> list, boolean optional) {
        ExtendNode node = new ExtendNode(list, optional);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void startNestedProperties(StringInterpolationSequence name) {
        NestPropertiesNode node = new NestPropertiesNode(name);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endNestedProperties(StringInterpolationSequence name) {
        nodeStack.pop();
    }

    @Override
    public void startMixinDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs) {
        MixinDefNode node = new MixinDefNode(name.trim(), args, hasVariableArgs);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endMixinDirective() {
        nodeStack.pop();
    }

    @Override
    public void startFunctionDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs) {
        FunctionDefNode node = new FunctionDefNode(name.trim(), args,
                hasVariableArgs);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endFunctionDirective() {
        nodeStack.pop();
    }

    @Override
    public void importStyle(String uri, SACMediaList media, boolean isURL) {
        ImportNode node = new ImportNode(uri, media, isURL);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void startIfElseDirective() {
        final IfElseDefNode node = new IfElseDefNode();
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void ifDirective(SassListItem evaluator) {
        if (nodeStack.peek() instanceof IfNode) {
            nodeStack.pop();
        }
        IfNode node = new IfNode(evaluator);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void elseDirective() {
        if (nodeStack.peek() instanceof IfNode) {
            nodeStack.pop();
        }
        ElseNode node = new ElseNode();
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    @Override
    public void endIfElseDirective() {
        if ((nodeStack.peek() instanceof ElseNode)
                || (nodeStack.peek() instanceof IfNode)) {
            nodeStack.pop();
        }
        nodeStack.pop();
    }

    @Override
    public void microsoftDirective(String name,
            StringInterpolationSequence value) {
        MicrosoftRuleNode node = new MicrosoftRuleNode(name, value);
        nodeStack.peek().appendChild(node);
    }

    // rule that is passed to the output as-is (except variable value
    // substitution) - no children
    public void unrecognizedRule(String text) {
        SimpleNode node = new SimpleNode(text);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void endSelector(SelectorList arg0) throws CSSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startSelector(SelectorList arg0) throws CSSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startKeyFrames(String keyframeName,
            StringInterpolationSequence animationName) {
        KeyframesNode node = new KeyframesNode(keyframeName, animationName);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    @Override
    public void endKeyFrames() {
        nodeStack.pop();

    }

    @Override
    public void startKeyframeSelector(String selector) {
        KeyframeSelectorNode node = new KeyframeSelectorNode(selector);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    @Override
    public void endKeyframeSelector() {
        nodeStack.pop();
    }

    @Override
    public void contentDirective() {
        ContentNode node = new ContentNode();
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void returnDirective(SassListItem expr) {
        ReturnNode node = new ReturnNode(expr);
        nodeStack.peek().appendChild(node);
    }

    @Override
    public void startInclude(String name, List<Variable> args,
            boolean hasVariableArgs) {
        MixinNode node = new MixinNode(name, args, hasVariableArgs);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    @Override
    public void endInclude() {
        nodeStack.pop();
    }

    private void log(Object object) {
        if (object != null) {
            log(object.toString());
        } else {
            log(null);
        }
    }

    private void log(String msg) {
        Logger.getLogger(SCSSDocumentHandlerImpl.class.getName()).log(
                Level.INFO, msg);
    }

    @Override
    public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
        // This method needs to be here due to an implemented interface.
        throw new CSSException("Unsupported call: property(" + name + ", "
                + value + ", important: " + important + ")");
    }

}
