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

import java.io.File;
import uk.co.samicemalone.tvmv.exception.OSNotSupportedException;

/**
 *
 * @author Sam Malone
 */
public class OS {

    public static final String OS = System.getProperty("os.name").toLowerCase();

    public static final boolean isWindows = OS.contains("win");
    public static final boolean isMac =     OS.contains("mac");
    public static final boolean isSolaris = OS.contains("sunos");
    public static final boolean isUnix =    OS.contains("nix") ||
                                            OS.contains("nux") ||
                                            OS.indexOf("aix") > 0;

    public static File getDefaultConfigDirectory() {
        if(isWindows) {
            return new File(System.getenv("USERPROFILE"), "tvmv/");
        } else if(isUnix || isMac) {
            return new File(System.getProperty("user.home") + "/.config/", "tvmv/");
        }
        throw new OSNotSupportedException("Your operating system is not currently supported");
    }

    public static boolean isSupported() {
        return isWindows || isUnix || isMac;
    }

    public static boolean isCygwinPath(String path) {
        return path.startsWith("/cygdrive/");
    }

    public static boolean isWSLPath(String path) {
        return path.startsWith("/mnt/");
    }

}
