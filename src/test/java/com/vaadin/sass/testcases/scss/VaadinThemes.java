package com.vaadin.sass.testcases.scss;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.sass.AbstractTestBase;

public class VaadinThemes extends AbstractTestBase {
    String scssFolder = "/vaadin-themes/scss";
    String cssFolder = "/vaadin-themes/css";

    @Test
    public void compileValo() throws Exception {
        String scss = scssFolder + "/valo/styles.scss";
        String css = cssFolder + "/valo/styles.css";
        testCompiler(scss, css);
        Assert.assertEquals("Original CSS and parsed CSS doesn't match",
                comparisonCss, parsedScss);
    }

    @Test
    public void compileReindeer() throws Exception {
        String scss = scssFolder + "/reindeer/styles.scss";
        String css = cssFolder + "/reindeer/styles.css";
        testCompiler(scss, css);
        Assert.assertEquals("Original CSS and parsed CSS doesn't match",
                comparisonCss, parsedScss);
    }
}
