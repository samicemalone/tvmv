/*
 * Copyright (c) 2014, Sam Malone. All rights reserved.
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

package uk.co.samicemalone.tvmv.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import uk.co.samicemalone.libtv.VideoFilter;
import uk.co.samicemalone.libtv.util.PathUtil;
import uk.co.samicemalone.tvmv.Args;
import uk.co.samicemalone.tvmv.OS;
import uk.co.samicemalone.tvmv.exception.OSNotSupportedException;
import uk.co.samicemalone.tvmv.io.WindowsLibraryReader;

/**
 *
 * @author Sam Malone
 */
public class Environment {
    
    private final Args args;
    private final Config config;
    private final List<String> sourcePaths;
    private final List<String> tvDestinationPaths;
    
    /**
     * Create a new Environment instance.
     * The Environment is used to store the program input data parsed from
     * the arguments and config file.
     * @param args program arguments
     * @param config program config 
     */
    public Environment(Args args, Config config) {
        this.args = args;
        this.config = config;
        sourcePaths = new ArrayList<>();
        tvDestinationPaths = new ArrayList<>();
    }
    
    /**
     * Initialise the environment. The arguments and config file will be validated
     * @return same instance
     * @throws FileNotFoundException if no input files or SOURCE config variable given.
     * Or if no TV destination found via DESTINATION or DESTINATION_LIBRARY variables.
     * @throws IOException if invalid input file formats are given as arguments
     */
    public Environment initialise() throws IOException {
        if(!OS.isSupported()) {
            throw new OSNotSupportedException("Your operating system is not currently supported");
        }
        if(args.getInputFiles().isEmpty() && config.getSource() == null) {
            throw new FileNotFoundException("There were no input files given or the SOURCE was not set in the config file.\nUse the --help flag for usage.");
        } else if(!args.getInputFiles().isEmpty()) {
            VideoFilter filter = new VideoFilter(true);
            for(String source : args.getInputFiles()) {
                sourcePaths.add(validatePath(filter, source).toString());
            }
        } else if(config.getSource() != null) {
            VideoFilter filter = new VideoFilter();
            for(Path path : PathUtil.listPaths(Paths.get(toFormattedPath(config.getSource())), filter)) {
                sourcePaths.add(path.toString());
            }
        }
        if(WindowsLibraryReader.isOSSupported() && config.getWindowsLibrary() != null) {
            List<String> libraryDirs = WindowsLibraryReader.listLibraryDirectories(config.getWindowsLibrary());
            for(String path : libraryDirs) {
                addDestPathIfExists(Paths.get(path));
            }
        }
        for(String path : config.getDestinationPaths()) {
            addDestPathIfExists(Paths.get(path));
        }
        if(tvDestinationPaths.isEmpty()) {
            throw new FileNotFoundException("No TV destination paths found.\nEnsure the DESTINATION (or DESTINATION_LIBRARY in Windows 7/8) is set in tvmv.conf");
        }
        return this;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public List<String> getTvDestinationPaths() {
        return tvDestinationPaths;
    }
    
    private void addDestPathIfExists(Path p) {
        if(Files.exists(p)) {
            tvDestinationPaths.add(p.toString());
        }
    }
    
    private Path validatePath(VideoFilter filter, String path) throws IOException {
        Path p = Paths.get(toFormattedPath(path));
        if(!Files.exists(p)) {
            throw new FileNotFoundException(String.format("The input file %s could not be found", path));
        }
        if(!filter.accept(p)) {
            throw new IOException("The input file " + p.toAbsolutePath().toString() + " is not a valid format");
        }
        return p;
    }
    
    private String toFormattedPath(String path) {
        return OS.isCygwinPath(path) ? OS.toWindowsPath(path) : path;
    }
    
}
