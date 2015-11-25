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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;

public class SassCompiler {

    private static final int ERROR_COMPILE_FAILED = 1;
    private static final int ERROR_FILE_NOT_FOUND = 2;

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
                .defaultValue("false")
                .help("Minify the compiled CSS with YUI Compressor");

        argp.defineOption("compress").values("true", "false")
                .defaultValue("false")
                .help("Create also a compressed version of the compiled CSS (only when output file is given)");

        argp.defineOption("ignore-warnings").values("true", "false")
                .defaultValue("false")
                .help("Let compilation succeed even though there are warnings");

        argp.parse(args);

        String input = argp.getInputFile();
        String output = argp.getOutputFile();

        ScssContext.UrlMode urlMode = getUrlMode(argp.getOptionValue("urlMode"));

        boolean minify = Boolean.parseBoolean(argp.getOptionValue("minify"));
        boolean compress = Boolean
                .parseBoolean(argp.getOptionValue("compress"));
        boolean ignoreWarnings = Boolean.parseBoolean(argp
                .getOptionValue("ignore-warnings"));

        File in = new File(input);
        if (!in.canRead()) {
            System.err.println(in.getCanonicalPath() + " could not be read!");
            System.exit(ERROR_FILE_NOT_FOUND);
        }
        input = in.getCanonicalPath();

        // You can set the resolver; if none is set, VaadinResolver will be used
        // ScssStylesheet.setStylesheetResolvers(new VaadinResolver());

        SCSSErrorHandler errorHandler = new SCSSErrorHandler();
        errorHandler.setWarningsAreErrors(!ignoreWarnings);
        try {
            // Parse stylesheet
            ScssStylesheet scss = ScssStylesheet.get(input, null,
                    new SCSSDocumentHandlerImpl(), errorHandler);
            if (scss == null) {
                System.err.println("The scss file " + input
                        + " could not be found.");
                System.exit(ERROR_FILE_NOT_FOUND);
            }

            // Compile scss -> css
            scss.compile(urlMode);

            // Write result
            Writer writer = createOutputWriter(output);
            scss.write(writer, minify);
            writer.close();

            if (output != null && compress) {
                String outputCompressed = output + ".gz";
                compressFile(output, outputCompressed);
            }
        } catch (Exception e) {
            throw e;
        }

        if (errorHandler.isErrorsDetected()) {
            // Exit with error code so Maven and others can detect compilation
            // was not successful
            System.exit(ERROR_COMPILE_FAILED);
        }
    }

    private static void compressFile(String uncompressedFileName,
            String compressedFileName) throws FileNotFoundException,
            IOException {
        FileInputStream uncompressedStream = new FileInputStream(
                uncompressedFileName);
        GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(
                compressedFileName));

        byte[] buffer = new byte[1024];
        int len;
        while ((len = uncompressedStream.read(buffer)) > 0) {
            gzos.write(buffer, 0, len);
        }

        uncompressedStream.close();

        gzos.finish();
        gzos.close();
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
