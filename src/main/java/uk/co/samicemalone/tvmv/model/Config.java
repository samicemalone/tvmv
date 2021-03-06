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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam Malone
 */
public class Config {
    
    private final List<String> destinations;
    private String source;
    private String windowsLibrary;
    private String createShowsFile;
    private String createDestShowDir;

    public Config() {
        destinations = new ArrayList<>();
    }

    public void addDestinationPath(String destination) {
        destinations.add(destination);
    }

    public String getSource() {
        return source;
    }

    public List<String> getDestinationPaths() {
        return destinations;
    }

    public String getWindowsLibrary() {
        return windowsLibrary;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setWindowsLibrary(String windowsLibrary) {
        this.windowsLibrary = windowsLibrary;
    }

    public String getCreateShowsFile() {
        return createShowsFile;
    }

    public void setCreateShowsFile(String createShowsFile) {
        this.createShowsFile = createShowsFile;
    }

    public String getCreateDestShowDir() {
        return createDestShowDir;
    }

    public void setCreateDestShowDir(String createDestShowDir) {
        this.createDestShowDir = createDestShowDir;
    }
    
}
