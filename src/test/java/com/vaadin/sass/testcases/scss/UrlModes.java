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
package com.vaadin.sass.testcases.scss;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.sass.AbstractTestBase;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;

public class UrlModes extends AbstractTestBase {
    private String scssFileName = "/urlmode/url-in-imported-stylesheet.scss";

    @Test
    public void testAbsoluteUrlMode() throws Exception {
        String cssFileName = "/urlmode/css/absolute.css";
        testCompiler(scssFileName, cssFileName, ScssContext.UrlMode.ABSOLUTE);
    }

    @Test
    public void testMixedUrlMode() throws Exception {
        String cssFileName = "/urlmode/css/mixed.css";
        testCompiler(scssFileName, cssFileName, ScssContext.UrlMode.MIXED);
    }

    @Test
    public void testRelativeUrlMode() throws Exception {
        String cssFileName = "/urlmode/css/relative.css";
        testCompiler(scssFileName, cssFileName, ScssContext.UrlMode.RELATIVE);
    }

    public ScssStylesheet testCompiler(String scss, String css,
            ScssContext.UrlMode urlMode) throws Exception {
        comparisonCss = getFileContent(css);
        comparisonCss = comparisonCss.replaceAll(CR, "");
        ScssStylesheet sheet = getStyleSheet(scss);
        sheet.compile(urlMode);
        parsedScss = sheet.printState();
        parsedScss = parsedScss.replaceAll(CR, "");
        Assert.assertEquals("Original CSS and parsed CSS do not match",
                comparisonCss, parsedScss);
        return sheet;
    }
}
