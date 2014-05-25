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
package uk.co.samicemalone.tvmv;

import java.math.BigDecimal;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import uk.co.samicemalone.tvmv.io.IOOperation;
import uk.co.samicemalone.tvmv.model.IOProgress;

/**
 *
 * @author Sam Malone
 */
public class Display {
    
    public static final int WIDTH = 80;
    public static final int PROGRESS_WIDTH = 73; // WIDTH - "[] ###%" length
    
    private static String getIODescription(IOOperation.Type type) {
        return type == IOOperation.Type.COPY ? "Copying" : "Moving";
    }
    
    public static void onPreRemoveOld(int replacementCount) {
        if(replacementCount > 0) {
            System.out.println("Replacing");
        }
    }
    
    public static void onPreIORemoveOld(String fileName) {
        System.out.format("  %s\n", fileName);
    }
    
    public static void onPreReplace(IOOperation.Type type, int replacementCount) {
        if(replacementCount > 0) {
            System.out.format("with\n");
        } else {
            System.out.println(getIODescription(type));
        }
    }
    
    public static void onPreIOReplace(IOOperation iop, boolean isNativeIO) {
        System.out.format("  %s...", iop.getSource().getFileName());
        if(!isNativeIO) {
            System.out.println();
        }
    }
    
    public static void onPostIOReplace(boolean isNativeIO) {
        onPostIO(isNativeIO);
    }
    
    public static void onPreIO(IOOperation iop, boolean isNativeIO) {
        System.out.format("%s %s...", getIODescription(iop.getType()), iop.getSource().getFileName());
        if(!isNativeIO) {
            System.out.println();
        }
    }
    
    public static void onPostIO(boolean isNativeIO) {
        System.out.println(isNativeIO ? "done" : "");
    }
    
    public static void onSkipNotMatched(Path skippedPath) {
        System.out.println("Skipping: " + skippedPath);
    }
    
    public static void onIOProgress(IOProgress p) {
        String percent = p.hasCompleted() ? "100" : String.valueOf(p.getPercent());
        int chars = p.hasCompleted() ? PROGRESS_WIDTH : p.getRatio().multiply(new BigDecimal(PROGRESS_WIDTH)).intValue();
        int spaces = PROGRESS_WIDTH - chars;
        StringBuilder format = new StringBuilder(WIDTH);
        format.append("[@|green ").append(StringUtils.repeat('=', chars)).append("|@");
        format.append(StringUtils.repeat(' ', spaces)).append("] ");
        format.append(StringUtils.leftPad(percent, 3)).append('%');
        Ansi ansi = Ansi.ansi();
        if(OS.isWindows && !System.getProperty("java.io.tmpdir").toLowerCase().contains("cygwin")) {
            ansi.saveCursorPosition().eraseLine().render(format.toString()).restorCursorPosition();
        } else {
            System.out.print('\r');
            ansi.render(format.toString());
        }
        AnsiConsole.out.print(ansi);
    }

    public static void onIORollback(IOOperation io) {
        System.out.println("Rolling Back: " + io.getDestination().getFileName());
    }
    
}
