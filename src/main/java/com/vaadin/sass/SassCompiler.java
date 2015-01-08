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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;

public class SassCompiler {

    private static final String[] supportedOptions = { "urlMode" };

    public static void main(String[] args) throws Exception {
        String input = null;
        String output = null;
        ScssContext.UrlMode urlMode = getUrlMode(args);
        args = removeOptions(args);
        if (args.length < 1 || args.length > 2) {
            System.out
                    .println("usage: SassCompiler -urlMode:mode <scss file to compile> <css file to write> or\n"
                            + "SassCompiler -urlMode:mode -- <scss file to compile> <css file to write>,\n"
                            + "where mode is one of absolute, mixed and relative. -- indicates end of\n"
                            + "options and should be used when the file names start with -.\n\n"
                            + "The urlMode parameter is optional.\n");
            return;
        }

        File in = new File(args[0]);
        if (!in.canRead()) {
            System.err.println(in.getCanonicalPath() + " could not be read!");
            return;
        }
        input = in.getCanonicalPath();

        if (args.length == 2) {
            output = args[1];
        }

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
            if (output == null) {
                System.out.println(scss.printState());
            } else {
                writeFile(output, scss.printState());
            }
        } catch (Exception e) {
            System.err.println("Compilation failed:");
            e.printStackTrace();
            throw e;
        }
    }

    private static ScssContext.UrlMode getUrlMode(String[] args) {
        // Extract the url mode from the options. The mode is given in the form
        // "-urlMode:<mode>" where <mode> is one of absolute, mixed and
        // relative. If no url mode is specified, the mode is taken to be
        // mixed for compatibility with previous versions of Vaadin Sass
        // compiler. This may be changed later to absolute urls for
        // compatibility with sass-lang.
        List<String> argList = Arrays.asList(args);
        List<String> options = getOptions(argList);
        if (options.size() == 0) {
            return ScssContext.UrlMode.MIXED;
        }
        if (options.size() > 1) {
            throw new RuntimeException("Unsupported options: " + options);
        }
        String arg = options.get(0);
        String[] parts = arg.split(":");
        if (!"-urlMode".equalsIgnoreCase(parts[0])) {
            throw new RuntimeException("Unsupported option: " + arg);
        }
        try {
            String urlMode = parts[1];
            if ("relative".equalsIgnoreCase(urlMode)) {
                return ScssContext.UrlMode.RELATIVE;
            } else if ("absolute".equalsIgnoreCase(urlMode)) {
                return ScssContext.UrlMode.ABSOLUTE;
            } else if ("mixed".equalsIgnoreCase(urlMode)) {
                return ScssContext.UrlMode.MIXED;
            } else {
                throw new RuntimeException("Unsupported url mode: " + urlMode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unsupported option: " + arg);
        }
    }

    private static String[] removeOptions(String[] args) {
        ArrayList<String> result = new ArrayList<String>(Arrays.asList(args));
        List<String> options = getOptions(result);
        result.removeAll(options);
        result.remove("--");
        return result.toArray(new String[result.size()]);
    }

    /*
     * Returns all options of the argument list. A string is an option if it
     * occurs before the first occurrence of the string '--' in argList and is
     * of the form '-supportedOption' where 'supportedOption' is one of the
     * strings in supportedOptions.
     */
    private static List<String> getOptions(List<String> argList) {
        int separatorIndex = argList.indexOf("--");
        if (separatorIndex < 0) {
            // There is no option separator '--': this is the same as if '--'
            // was inserted after the last element.
            separatorIndex = argList.size();
        }
        // include the options before '--'
        return getOptionsInSubList(argList.subList(0, separatorIndex));
    }

    /*
     * Returns all strings of argList that are of the form '-supportedOption'
     * where supportedOption is a String in supportedOptions.
     */
    private static List<String> getOptionsInSubList(List<String> argList) {
        List<String> result = new ArrayList<String>();
        for (String s : argList) {
            if (isSupportedOption(s)) {
                result.add(s);
            }
        }
        return result;
    }

    private static boolean isSupportedOption(String arg) {
        String argLowerCase = arg.toLowerCase();
        for (String supportedOption : supportedOptions) {
            if (argLowerCase.startsWith("-" + supportedOption.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void writeFile(String filename, String output)
            throws IOException {
        File file = new File(filename);
        FileWriter writer = new FileWriter(file);
        writer.write(output);
        writer.close();
    }
}