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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Argument parsing facility, used internally by SassCompiler.
 */
public final class ArgumentParser {

    /**
     * Describes a command-line option.
     */
    public static class Option {
        private List<String> names;
        private List<String> validValues;
        private String defaultValue;
        private String value;
        private String help;
        private boolean set;

        protected Option(String... names) {
            assert names.length > 0 : "At least one option name must be specified";
            this.names = Arrays.asList(names);
            help = "";
            set = false;
        }

        /**
         * Specify the list of valid values for this Option.
         * 
         * @param values
         *            a list of values to accept for this option. Value input
         *            other than one of these values results in program
         *            termination.
         * @return a reference to self
         */
        public Option values(String... values) {
            validValues = Arrays.asList(values);
            return this;
        }

        /**
         * Specify a default value for this Option. If this option is not
         * specified on the command line,
         * {@link ArgumentParser#getOptionValue(String)} for this option will
         * return this default value. This method must only be called after a
         * list of valid input values has been set via a call to
         * {@link #values(String...)}.
         * 
         * @param value
         *            a previously defined valid value string
         * @return a reference to self
         */
        public Option defaultValue(String value) {

            assert validValues != null : "Valid values must be specified before specifying a default value";
            assert validValues.contains(value) : "Default value \'" + value
                    + "\' is not one of the valid values";

            this.value = value;
            defaultValue = value;

            return this;
        }

        /**
         * Specify the help text for this command-line option. This is optional,
         * but can (and should) offer the user additional information about what
         * this particular option does.
         * 
         * @param desc
         *            a descriptive help text
         * @return a reference to self
         */
        public Option help(String desc) {
            help = desc;
            return this;
        }

        protected boolean supportsValues() {
            return validValues != null;
        }

        protected void markSet() {
            set = true;
        }

        protected boolean setValue(String value) {

            assert validValues != null : "Valid values must be specified before setting a value";

            if (!validValues.contains(value)) {
                return false;
            }

            this.value = value;
            return true;
        }

        protected String getValue() {
            return value;
        }

        protected boolean isSet() {
            return set;
        }

        protected List<String> getNames() {
            return Collections.unmodifiableList(names);
        }

        protected String getHelp() {
            return help;
        }

        protected List<String> getValidValues() {
            if (validValues != null) {
                return Collections.unmodifiableList(validValues);
            }
            return null;
        }

        protected String getDefaultValue() {
            return defaultValue;
        }

    }

    // Indentation constants
    private static final String OPTION_INDENT = "   ";
    private static final String OPTION_HELP_INDENT = "      ";

    // Singleton instance
    private static final ArgumentParser instance = new ArgumentParser();

    /**
     * Get access to the global Argument Parser
     * 
     * @return an argument parser instance
     */
    public static final ArgumentParser get() {
        return instance;
    }

    private final List<Option> validOptions;
    private final Map<String, Option> options;
    private Option helpOption;

    private String progName;
    private String help;

    private String inFile;
    private String outFile;

    private ArgumentParser() {
        validOptions = new ArrayList<Option>();
        options = new HashMap<String, Option>();

        inFile = null;
        outFile = null;

        progName = "program";
        help = "";

        // Define end-of-options pseudo-option for help
        defineOption("-").help("Stop processing options");

        // Define default help option
        helpOption = defineOption("h", "help").help(
                "Print this help text and exit");
    }

    /**
     * Define a command-line option. Returns an {@link Option} object with fluid
     * API for defining its specifics (like help text, list of valid
     * values/parameters and default value).
     * 
     * @param names
     *            list of names to associate with this option. One must be
     *            defined, any more are optional. Names may NOT overlap;
     *            attempting to overwrite a previous name results in an
     *            assertion failure.
     * @return the newly created option object
     */
    public Option defineOption(String... names) {
        Option o = new Option(names);
        validOptions.add(o);

        for (String n : names) {
            assert !options.containsKey(n) : "Tried to re-define option " + n;
            options.put(n, o);
        }

        return o;
    }

    /**
     * Parse the arguments passed to the application
     * 
     * @param args
     *            argument array - usually the command line split into its
     *            constituent words as passed in to the Java main method.
     */
    public void parse(String[] args) {

        // Go through args in order
        boolean processOpts = true;
        int argc = 0;

        for (String arg : args) {

            // Process as option
            if (processOpts && arg.startsWith("-")) {

                // Stop accepting options after double minus
                if (arg.equals("--")) {
                    processOpts = false;
                    continue;
                }

                processOption(arg.substring(1));

            } else {
                // Process argument
                switch (argc) {
                case 0:
                    inFile = arg;
                    break;
                case 1:
                    outFile = arg;
                    break;
                default:
                    invalidArgument(arg);
                    break;
                }
                argc++;
            }
        }

        // Require input file
        if (inFile == null) {
            printHelp();
            System.exit(1);
        }
    }

    /**
     * Handle verification and value-setting of an option.
     * 
     * @param opt
     *            option name
     */
    private void processOption(String opt) {

        String optName = null;
        String optValue = null;
        Option o;

        // Extract option name and value (if applicable)
        if (opt.indexOf(':') != -1) {
            String[] parts = opt.split(":");
            optName = parts[0];
            optValue = parts[1];
        } else {
            optName = opt;
        }

        o = options.get(optName);
        if (o == null) {
            // Fail on invalid option. This halts the program.
            invalidOption(optName);
        }

        if (o == helpOption) {
            // Print help and exit, fulfilling the promise of the standard help
            // option
            printHelp();
            System.exit(1);
        }

        if (optValue != null) {
            // If setting a value on the option fails, halt the program.
            boolean ok = o.setValue(optValue);
            if (!ok) {
                invalidOptionValue(optName, optValue);
            }
        }

        // We're going to want to mark the option as 'set' since we're
        // reading it from the console...
        o.markSet();

    }

    /**
     * Show error message about invalid option input, print help and exit with
     * error
     * 
     * @param opt
     *            option string to fail on
     */
    private void invalidOption(String opt) {
        System.err.println("Invalid option \'-" + opt + "\'");
        printHelp();
        System.exit(1);
    }

    /**
     * Show error about invalid option value input, print help and exit with
     * error
     * 
     * @param opt
     *            option name
     * @param value
     *            value that is invalid for this option
     */
    private void invalidOptionValue(String opt, String value) {
        System.err.println("Option -" + opt + " does not support the value \'"
                + value + "\'");
        printHelp();
        System.exit(1);
    }

    /**
     * Show error message about invalid argument value, print help and exit with
     * error
     * 
     * @param arg
     *            argument string to fail on
     */
    private void invalidArgument(String arg) {
        System.err.println("Invalid argument \'" + arg + "\'");
        printHelp();
        System.exit(1);
    }

    /**
     * Set name of the program (defaults to 'program')
     * 
     * @param name
     *            program name string
     */
    public void setProgramName(String name) {
        progName = name;
    }

    /**
     * Set help text body. Defaults to empty.
     * 
     * @param help
     *            help text
     */
    public void setHelpText(String help) {
        this.help = help;
    }

    /**
     * Print help text
     */
    public void printHelp() {
        System.out.println("Usage: " + progName
                + " [options] <input file> [output file]"
                + (!help.isEmpty() ? "\n\n" + help : "") + "\n" + "Options:\n");

        // Sort valid options in alphabetical order according to primary
        // preference
        Collections.sort(validOptions, new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.names.get(0).compareTo(o2.names.get(0));
            }
        });

        // Print option-specific help
        for (Option o : validOptions) {

            // Print option name
            List<String> names = o.getNames();
            String nameString = "" + OPTION_INDENT;
            for (int i = 0; i < names.size(); ++i) {
                nameString += "-" + names.get(i);
                if (i < names.size() - 1) {
                    nameString += ", ";
                }
            }
            System.out.println(nameString);

            // Print generic help for option
            if (!o.getHelp().isEmpty()) {
                String[] helpLines = o.getHelp().split("\n");
                for (String h : helpLines) {
                    System.out.println(OPTION_HELP_INDENT + h);
                }
            }

            // Print a list of all valid values if applicable
            if (o.getValidValues() != null) {
                List<String> values = o.getValidValues();
                String valuesString = "Valid values: ";
                for (int i = 0; i < values.size(); ++i) {
                    valuesString += values.get(i);
                    if (i < values.size() - 1) {
                        valuesString += ", ";
                    }
                }
                System.out.println(OPTION_HELP_INDENT + valuesString);
                if (o.getDefaultValue() != null) {
                    System.out.println(OPTION_HELP_INDENT + "Default: "
                            + o.getDefaultValue());
                }
                System.out.println(OPTION_HELP_INDENT + "Example: -"
                        + names.get(0) + ":" + values.get(0));
            }
            System.out.println();
        }
    }

    /**
     * Get the input file argument
     * 
     * @return a string value
     */
    public String getInputFile() {
        return inFile;
    }

    /**
     * Get the output file argument
     * 
     * @return a string value or null if no output file has been specified
     */
    public String getOutputFile() {
        return outFile;
    }

    /**
     * Check if the user has specified an option on the command line. Can be
     * used for simple boolean checks. This method asserts that an option with
     * the specified name has been added to the argument parser.
     * 
     * @param optName
     *            name of the option to check
     * @return true, if the specified option is part of the command line
     */
    public boolean isOptionSet(String optName) {
        Option o = options.get(optName);
        assert o != null : "No option by name " + optName
                + " registered in ArgumentParser";
        return o.isSet();
    }

    /**
     * Get an option's value. If an option has a default value set and the user
     * hasn't explicitly specified a value for this option on the command line,
     * the default value will be returned. Otherwise, the user-set value will be
     * returned. This method asserts that an option with the specified name
     * exists. Note, that not all options support values.
     * 
     * @param optName
     *            name of the option to check
     * @return the user-specified value, if the option was set from the command
     *         line, or default value (if it has one).
     */
    public String getOptionValue(String optName) {
        Option o = options.get(optName);
        assert o != null : "No option by name " + optName
                + " registered in ArgumentParser";
        String value = o.getValue();
        assert value != null : "Option "
                + optName
                + " does not have a value set (did you forget to specify a default value?)";
        return value;
    }

}
