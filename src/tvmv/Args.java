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
package tvmv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import tvmv.exception.InputFileException;
import tvmv.exception.UnsupportedFileException;

/**
 *
 * @author Sam Malone
 */
public class Args {
    
    private boolean isReplace = false;
    private List<String> inputFiles;

    private Args() {
        inputFiles = new ArrayList<String>();
    }

    private static Args newInstance() {
        return new Args();
    }

    /**
     * Checks if the Replace flag is set
     * @return true if set, false otherwise
     */
    public boolean isReplaceSet() {
        return isReplace;
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

    /**
     * Parses the argument string array into an Args object
     * @param args Command Line Arguments
     * @return Arguments instance or null if help flag set in arguments
     */
    public static Args parse(String[] args) {
        Args returnArgs = newInstance();
        for(String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                return null;
            }
            if (arg.equals("-r") || arg.equals("--replace")) {
                returnArgs.isReplace = true;
                continue;
            }
            returnArgs.addInputFile(arg);
        }
        return returnArgs;
    }
    
    /**
     * Attempts to validate the given arguments. If any of the input files
     * are in Cygwin format (/cygdrive/) then the Args object will be modified
     * to store the windows readable path.
     * An exception will be thrown if any arguments fail validation.
     * @param args Args
     * @return Same object reference to args parameter. The object will be
     * unmodified unless Cygwin path formats are found.
     * @throws InputFileException if no input files were given
     * @throws FileNotFoundException if any of the input files cannot be found
     */
    public static Args validate(Args args) throws Exception {
        if(args.getInputFiles().isEmpty()) {
            throw new InputFileException("There were no input files given. Use the --help flag for usage.");
        }
        for(int i = 0; i < args.inputFiles.size(); i++) {
            String file = args.inputFiles.get(i);
            if(file.startsWith("/cygdrive/")) {
                String winPath = new StringBuilder().append(file.charAt(10)).append(":\\").append(file.substring(12)).toString();
                args.inputFiles.set(i, winPath);
            }
            File f = new File(file);
            if(!f.exists()) {
                throw new FileNotFoundException(String.format("The input file %s could not be found", file));
            }
            if(!new VideoFilter().accept(f.getParentFile(), f.getName())) {
                throw new UnsupportedFileException("The input file " + f.getAbsolutePath() + " is not a valid format");
            }
        }
        return args;
    }
    
}
