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
package tvmv.util;

import java.io.File;
import java.util.Random;

/**
 *
 * @author Sam Malone
 */
public class FileUtil {
    
    /**
     * Returns a temporary File with a random filename in the directory given
     * by tmpDir. The temporary file will not exist or be created.
     * @param tmpDir Directory to store temp file
     * @return Non existing temporary file
     */
    public static File getTempFile(File tmpDir) {
        final String alphabet = "0123456789abcdefghijklmnpqrstuvwxyz";
        final String extension = ".tmp";
        final int tmpFileNameLength = 20;
        Random r = new Random();
        StringBuilder sb;
        File tmpFile;
        do {
            sb = new StringBuilder(tmpFileNameLength + extension.length() + 1);
            for(int i = 0; i < tmpFileNameLength; i++) {
                sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
            }
            sb.append(extension);
        } while((tmpFile = new File(tmpDir, sb.toString())).exists());
        return tmpFile;
    }
    
}
