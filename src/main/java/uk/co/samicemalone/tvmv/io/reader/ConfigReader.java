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

package uk.co.samicemalone.tvmv.io.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.co.samicemalone.tvmv.OS;
import uk.co.samicemalone.tvmv.exception.OSNotSupportedException;
import uk.co.samicemalone.tvmv.model.Config;

/**
 * 
 * @author Sam Malone
 */
public class ConfigReader extends KeyValueReader {
    
    /**
     * Read the config file from the specified file path. If this is set to
     * null, the config file will be read from the current directory.
     * If this file doesn't exist, the default location will be used ({@link OS#getDefaultConfigDirectory()}).
     * If this file doesn't exist, a FileNotFoundException is thrown
     * @param filePath Config file path or null
     * @return Config
     * @throws FileNotFoundException if the config cannot be found
     * @throws OSNotSupportedException if the OS is not supported
     * @throws IOException if unable to read the config file
     */
    public static Config read(String filePath) throws IOException {
        ConfigReader r = new ConfigReader();
        Path p = filePath == null ? getConfigFilePath() : Paths.get(filePath);
        if(filePath != null && !Files.exists(p)) {
            throw new FileNotFoundException("Config file not found: " + filePath);
        }
        r.readFile(p);
        return r.getConfig();
    }
    
    private static Path getConfigFilePath() throws FileNotFoundException {
        String configFileName = "tvmv.conf";
        Path p = Paths.get(".", configFileName);
        if(Files.exists(p)) {
            return p;
        }
        p = OS.getDefaultConfigDirectory().toPath().resolve(configFileName);
        if(Files.exists(p)) {
            return p;
        }
        throw new FileNotFoundException("Config file not found in the current working directory or " + p.getParent());
    }
    
    private final Config config;

    public ConfigReader() {
        config = new Config();
    }

    @Override
    protected boolean onReadKeyValue(String key, String value) {
        switch(key) {
            case "SOURCE":
                config.setSource(value);
                break;
            case "DESTINATION":
                config.addDestinationPath(value);
                break;
            case "DESTINATION_LIBRARY":
                config.setWindowsLibrary(value);
                break;
            case "CREATE_SHOWS_FILE":
                config.setCreateShowsFile(value);
                break;
            case "CREATE_SHOWS_DEST":
                config.setCreateDestShowDir(value);
                break;
        }
        return true;
    }

    public Config getConfig() {
        return config;
    }
    
}
