/*
 * Copyright (c) 2013, Sam Malone. All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of Sam Malone nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.co.samicemalone.tvmv;

import java.util.ArrayList;
import java.util.List;
import uk.co.samicemalone.tvmv.io.CopyOperation;
import uk.co.samicemalone.tvmv.io.IOOperation;
import uk.co.samicemalone.tvmv.io.MoveOperation;

/**
 *
 * @author Sam Malone
 */
public class Args {
    
    private boolean isNativeIO = false;
    private boolean isHelp = false;
    private boolean isReplace = false;
    private boolean isSkipNotMatched = false;
    private String showOverride;
    private String configFile;
    private IOOperation ioOperation = new MoveOperation();
    private final List<String> inputFiles;

    private Args() {
        inputFiles = new ArrayList<>();
    }

    private static Args newInstance() {
        return new Args();
    }

    /**
     * Checks if the Native IO flag is set
     * @return true if set, false otherwise
     */
    public boolean isNativeIOSet() {
        return isNativeIO;
    }

    /**
     * Checks if the Replace flag is set
     * @return true if set, false otherwise
     */
    public boolean isReplaceSet() {
        return isReplace;
    }

    /**
     * Checks if the Skip not matched flag is set
     * @return true if set, false otherwise
     */
    public boolean isSkipNotMatchedSet() {
        return isSkipNotMatched;
    }

    /**
     * Get the path to configuration file
     * @return path to configuration file
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * Get the IO Operation specified
     * The default value is {@link IOOperation.Move}
     * @return IO Operation
     */
    public IOOperation getIOOperation() {
        return ioOperation;
    }

    /**
     * Get the list of input files
     * @return List of input files
     */
    public List<String> getInputFiles() {
        return inputFiles;
    }

    /**
     * Adds an input file
     * @param inputFile input file
     */
    public void addInputFile(String inputFile) {
        inputFiles.add(inputFile);
    }
    
    public boolean isHelpSet() {
        return isHelp;
    }

    public String getShowOverride() {
        return showOverride;
    }

    /**
     * Parses the argument string array into an Args object
     * @param args Command Line Arguments
     * @return Arguments instance or null if help flag set in arguments
     */
    public static Args parse(String[] args) {
        Args returnArgs = newInstance();
        boolean isArg = false;
        for(int i = 0; i < args.length; i++) {
            if(isArg) {
                continue;
            }
            isArg = parseArgument(returnArgs, args, i);
        }
        return returnArgs;
    }
    
    /**
     * 
     * @param returnArgs
     * @param args
     * @param index
     * @return true if next argument is the argument value, false otherwise
     */
    private static boolean parseArgument(Args returnArgs, String[] args, int index) {
        switch(args[index]) {
            case "-c":
            case "--copy":
                returnArgs.ioOperation = new CopyOperation();
                return false;
            case "--config":
                returnArgs.configFile = getArgument(args, index+1);
                return true;
            case "-h":
            case "--help":
                returnArgs.isHelp = true;
                return false;
            case "-o":
            case "--override-show":
                returnArgs.showOverride = getArgument(args, index+1);
                return true;
            case "-n":
            case "--native":
                returnArgs.isNativeIO = true;
                return false;
            case "-r":
            case "--replace":
                returnArgs.isReplace = true;
                return false;
            case "-s":
            case "--skip-not-matched":
                returnArgs.isSkipNotMatched = true;
                return false;
            default:
                returnArgs.addInputFile(args[index]);
                return false;
        }
    }
    
    private static String getArgument(String[] args, int index) {
        if(index > 0 && index < args.length) {
            return args[index];
        }
        throw new IndexOutOfBoundsException("Missing argument for option " + args[index]);
    }
    
}
