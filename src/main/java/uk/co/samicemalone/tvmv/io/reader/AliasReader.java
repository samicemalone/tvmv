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
import uk.co.samicemalone.libtv.model.AliasMap;
import uk.co.samicemalone.tvmv.OS;
import uk.co.samicemalone.tvmv.exception.OSNotSupportedException;

/**
 *
 * @author Sam Malone
 */
public class AliasReader extends KeyValueReader {
    
    private final AliasMap aliasMap;

    /**
     * Reads the aliases.txt file from the current directory. If this is not found,
     * The default location ({@link OS#getDefaultConfigDirectory()}) will be used
     * instead. If this file does not exist, an empty AliasMap will be returned.
     * @param aliasMap AliasMap to read aliases into
     * @return AliasMap containing the shows and their aliases
     * @throws IOException if unable to read the file
     * @throws OSNotSupportedException if os not supported
     */
    public static AliasMap read(AliasMap aliasMap) throws IOException {
        AliasReader r = new AliasReader(aliasMap);
        try {
            r.readFile(getAliasFilePath());
        } catch (FileNotFoundException ex) {
            
        }
        return r.getAliasMap();
    }
    
    private static Path getAliasFilePath() throws FileNotFoundException {
        String aliasFileName = "aliases.txt";
        Path p = Paths.get(".", aliasFileName);
        if(Files.exists(p)) {
            return p;
        }
        p = OS.getDefaultConfigDirectory().toPath().resolve(aliasFileName);
        if(Files.exists(p)) {
            return p;
        }
        throw new FileNotFoundException();
    }

    public AliasReader(AliasMap aliasMap) {
        this.aliasMap = aliasMap;
    }

    public AliasMap getAliasMap() {
        return aliasMap;
    }
    
    @Override
    public boolean onReadKeyValue(String key, String value) {
        aliasMap.addAlias(key, value);
        return true;
    }
    
}
