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
package tvmv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import tvmv.OS;
import tvmv.exception.OSNotSupportedException;
import tvmv.exception.ParseException;
import tvmv.model.AliasMap;

/**
 *
 * @author Sam Malone
 */
public class AliasReader {
    
    /**
     * Reads the aliases.txt file from the default location. If the file does
     * not exist, an empty AliasMap will be returned.
     * @return AliasMap containing the shows and their aliases
     * @throws OSNotSupportedException if os not supported
     * @throws ParseException if the was an error parsing aliases.txt
     */
    public static AliasMap read() throws OSNotSupportedException, ParseException {
        File f = new File(OS.getDefaultConfigDirectory(), "aliases.txt");
        AliasMap aliases = new AliasMap();
        if(!f.exists()) {
            return aliases;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
            String line;
            while((line = br.readLine()) != null) {
                parseLine(aliases, line);
            }
            br.close();
        } catch(IOException e) {
            
        }
        return aliases;
    }
    
    private static void parseLine(AliasMap aliases, String line) throws ParseException {
        if(line.isEmpty() || line.charAt(0) == '#') {
            return;
        }
        String key, value;
        try {
            int equalsIndex = line.indexOf('=');
            key = line.substring(0, equalsIndex).trim();
            value = line.substring(equalsIndex + 1).trim();
        } catch(IndexOutOfBoundsException ex) {
            throw new ParseException("Unable to parse the line " + line);
        }
        if(!value.isEmpty()) {
            aliases.addAlias(key, value);
        }
    }
    
}
