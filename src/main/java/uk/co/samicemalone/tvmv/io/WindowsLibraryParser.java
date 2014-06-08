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

package uk.co.samicemalone.tvmv.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.samicemalone.tvmv.OS;
import uk.co.samicemalone.tvmv.model.WindowsLibrary;

/**
 *
 * @author Sam Malone
 */
public class WindowsLibraryParser extends DefaultHandler {
    
    /**
     * Parse a Windows Library to get a list of absolute paths to the directories that make up the library
     * @param libraryName Windows Library Name or "?" to detect TV library (see {@link #findTVLibraryPath()})
     * @return WindowsLibrary containing list of paths of the directories in the given library, or an empty
     * list if the library is empty or does not exist
     */
    public static WindowsLibrary parse(String libraryName) {
        Path libraryPath = "?".equals(libraryName) ? findTVLibraryPath() : getLibraryPath(libraryName);
        if(libraryPath == null) {
            return new WindowsLibrary();
        }
        return parse(libraryPath.toFile());
    }
    
    /**
     * Parse a Windows Library to get a list of absolute paths to the directories that make up the library
     * @param msLibraryPath Path to the .library-ms file (must exist)
     * @return WindowsLibrary containing list of paths of the directories in the given library, or an empty
     * list if the library is empty or does not exist
     */
    public static WindowsLibrary parse(File msLibraryPath) {
        WindowsLibraryParser lp = new WindowsLibraryParser();
        try {
            SAXParser sax = SAXParserFactory.newInstance().newSAXParser();
            sax.parse(msLibraryPath, lp);
        } catch(SAXException | ParserConfigurationException | IOException ex) {
            
        }
        return lp.getWindowsLibrary();
    }
    
    /**
     * Check if the operating system is Windows and supports Libraries.
     * Currently only Windows 7 and 8 are supported.
     * @return true if os supported, false otherwise
     */
    public static boolean isOSSupported() {
        return OS.OS.matches("windows [78]");
    }
    
    /**
     * Check if the given Windows library name is valid and exists
     * @param libraryName Windows Library Name
     * @return true if libraryName is valid and exists, false otherwise
     */
    public static boolean isValidLibraryName(String libraryName) {
        return getLibraryPath(libraryName) != null;
    }
    
    /**
     * Gets the absolute path to the the Windows library-ms file with the given name
     * @param libraryName Windows Library Name
     * @return Full path to the given library name or null if invalid/doesn't exist
     */
    private static Path getLibraryPath(String libraryName) {
        Path p = Paths.get(System.getenv("APPDATA"), "Microsoft\\Windows\\Libraries", libraryName + ".library-ms");
        return Files.exists(p) ? p : null;
    }
    
        
    /**
     * Find the TV .library-ms file path. The "TV" library will be checked first
     * and if not found then "Television" will be checked.
     * @return TV .library-ms file path or null if not found
     */
    public static Path findTVLibraryPath() {
        Path libraryPath = getLibraryPath("TV");
        return libraryPath == null ? getLibraryPath("Television") : libraryPath;
    }
    
    private final WindowsLibrary l;
    
    private StringBuilder chars;
    
    private String curLocation;
    private boolean isDefaultSaveLocation;
    
    public WindowsLibraryParser() {
        chars = new StringBuilder();
        l = new WindowsLibrary();
    }

    private WindowsLibrary getWindowsLibrary() {
        return l;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch(qName) {
            case "isDefaultSaveLocation":
            case "url":
                chars = new StringBuilder();
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(qName) {
            case "searchConnectorDescription":
                l.addDirectoryLocation(curLocation, isDefaultSaveLocation);
                isDefaultSaveLocation = false;
            case "isDefaultSaveLocation":
                if("true".equals(chars.toString())) {
                    isDefaultSaveLocation = true;
                }
                break;
            case "url":
                curLocation = chars.toString();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars.append(ch, start, length);
    }
    
}
