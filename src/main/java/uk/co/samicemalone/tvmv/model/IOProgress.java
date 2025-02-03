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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.co.samicemalone.tvmv.Display;

/**
 *
 * @author Sam Malone
 */
public class IOProgress {
    
    public static long getBytesPerChar(long size) {
        return new BigDecimal(size).divide(new BigDecimal(Display.PROGRESS_WIDTH), 3, RoundingMode.FLOOR).longValue();
    }

    private final long bytesWritten;
    private final long size;
    
    private final IOException exception;

    public IOProgress(long bytesWritten, long size, IOException ioe) {
        this.bytesWritten = bytesWritten;
        this.size = size;
        this.exception = ioe;
    }

    public IOProgress(long bytesWritten, long size) {
        this(bytesWritten, size, null);
    }

    public IOProgress(IOException ioe) {
        this(0, 0, ioe);
    }

    public long getSize() {
        return size;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }
    
    public BigDecimal getRatio() {
        if(size == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(bytesWritten).divide(new BigDecimal(size), 3, RoundingMode.FLOOR);
    }

    public int getPercent() {
        return getRatio().multiply(new BigDecimal(100)).intValue();
    }

    public boolean hasCompleted() {
        return bytesWritten == size;
    }
    
    public boolean wasError() {
        return exception != null;
    }
    
    public IOException getError() {
        return exception;
    }
    
}
