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

package com.vaadin.sass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;

public class SassCompiler {

    public static void main(String[] args) throws Exception {

        ArgumentParser argp = ArgumentParser.get();

        argp.setProgramName(SassCompiler.class.getSimpleName());

        argp.setHelpText("Input file is the .scss file to compile.\n"
                + "Output file is an optional argument, indicating the file\n"
                + "in which to store the generated CSS. If it is not defined,\n"
                + "the compiled CSS will be written to standard output.\n");

        argp.defineOption("urlMode").values("mixed", "absolute", "relative")
                .defaultValue("mixed").help("Set URL handling mode");

        argp.defineOption("minify").values("true", "false")
                .defaultValue("true")
                .help("Minify the compiled CSS with YUI Compressor");

        argp.parse(args);

        String input = argp.getInputFile();
        String output = argp.getOutputFile();

        ScssContext.UrlMode urlMode = getUrlMode(argp.getOptionValue("urlMode"));

        boolean minify = Boolean.parseBoolean(argp.getOptionValue("minify"));

        File in = new File(input);
        if (!in.canRead()) {
            System.err.println(in.getCanonicalPath() + " could not be read!");
            return;
        }
        input = in.getCanonicalPath();

        // You can set the resolver; if none is set, VaadinResolver will be used
        // ScssStylesheet.setStylesheetResolvers(new VaadinResolver());

        ScssStylesheet scss = ScssStylesheet.get(input);
        if (scss == null) {
            System.err.println("The scss file " + input
                    + " could not be found.");
            return;
        }

        try {
            scss.compile(urlMode);

            Writer writer = createOutputWriter(output);
            scss.write(writer, minify);
            writer.close();
        } catch (Exception e) {
            System.err.println("Compilation failed:");
            e.printStackTrace();
            throw e;
        }
    }

    private static ScssContext.UrlMode getUrlMode(String urlMode) {
        if ("relative".equalsIgnoreCase(urlMode)) {
            return ScssContext.UrlMode.RELATIVE;
        } else if ("absolute".equalsIgnoreCase(urlMode)) {
            return ScssContext.UrlMode.ABSOLUTE;
        }
        return ScssContext.UrlMode.MIXED;
    }

    private static Writer createOutputWriter(String filename)
            throws IOException {
        if (filename == null) {
            return new OutputStreamWriter(System.out, "UTF-8");
        } else {
            File file = new File(filename);
            return new FileWriter(file);
        }
    }
}
