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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.samicemalone.tvmv.OS;

/**
 *
 * @author Sam Malone
 */
public class WindowsLibraryReader {

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
     * Gets a list of absolute paths to the directories that make up the library
     * @param libraryName Windows Library Name or "?" to detect TV library (see {@link #findTVLibraryPath()})
     * @return List of paths of the directories in the given library, or an empty
     * list if the library is empty or does not exist
     */
    public static List<String> listLibraryDirectories(String libraryName) {
        List<String> dirs = new ArrayList<>();
        Path libraryPath = "?".equals(libraryName) ? findTVLibraryPath() : getLibraryPath(libraryName);
        if(libraryPath == null) {
            return dirs;
        }
        return listLibraryDirectorires(libraryPath);
    }
    
    /**
     * Gets a list of absolute paths to the directories that make up the library
     * @param libraryPath Path to the .library-ms file (must exist)
     * @return List of paths of the directories in the given library, or an empty
     * list if the library is empty or does not exist
     */
    public static List<String> listLibraryDirectorires(Path libraryPath) {
        List<String> dirs = new ArrayList<>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            Document doc = domFactory.newDocumentBuilder().parse(libraryPath.toFile());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/libraryDescription/searchConnectorDescriptionList/searchConnectorDescription/simpleLocation/url/text()");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                dirs.add(nodes.item(i).getNodeValue()); 
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            
        }
        return dirs;
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
    
}
