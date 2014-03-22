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
package tvmv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tvmv.exception.ParseException;
import tvmv.model.AliasMap;
import tvmv.model.Episode;
import tvmv.model.TVSourceList;
import tvmv.util.StringUtil;

/**
 *
 * @author Sam Malone
 */
public class EpisodeMatcher {
    
    private final AliasMap aliasMap;
    private final TVSourceList sourceList;
    
    private static final String[] regexList = new String[] {
        "(.*?)[ -._]+s([0-9]+)[ -._]*e([0-9]+).*?",
        "(.*?)[ -._]+([0-9]+)x([0-9]+).*?",
        "(.*?)[ -._]+([0-9]+)([0-9][0-9]).*?"
    };
    
    public EpisodeMatcher(TVSourceList sourceList, AliasMap aliasMap) {
        this.sourceList = sourceList;
        this.aliasMap = aliasMap;
    }
    
    /**
     * Attempt to parse the given episode path into an Episode object
     * @param ep Episode Path
     * @return Parsed Episode
     * @throws ParseException if the episode cannot be parsed
     * @throws FileNotFoundException if the TV Show directory cannot be found
     */
    public Episode parse(String ep) throws ParseException, FileNotFoundException {
        Episode e;
        for(String r : regexList) {
            Matcher m = Pattern.compile(r, Pattern.CASE_INSENSITIVE).matcher(new File(ep).getName());
            if (m.find()) {
                e = new Episode();
                e.setShow(StringUtil.toTitleCase(m.group(1).replaceAll("\\.|_", " ").trim()));
                e.setSeasonNo(Integer.valueOf(m.group(2)));
                e.setEpisodeNo(Integer.valueOf(m.group(3)));
                e.setShowDirectory(findShowDirectory(e.getShow()));
                if(e.getShowDirectory() == null) {
                    throw new FileNotFoundException("Could not find the TV Show folder for " + e.getShow());
                }
                return e;
            }
        }
        throw new ParseException("Could not parse the input episode " + ep + " for its show name, season or episode nubmber");
    }
    
    /**
     * Find an existing episode that matches the Episode given. The season
     * directory will be searched for an episode file that matches the episode
     * number
     * @param e Episode
     * @return Exiting episode File or null if no existing episode found
     */
    public File findExistingEpisodeFile(Episode e) {
        for(File file : e.getSeasonDirectory().listFiles(new VideoFilter())) {
            for(String regex : regexList) {
                Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(file.getName());
                if(m.find()) {
                    int episodeNo = Integer.valueOf(m.group(3));
                    if(episodeNo == e.getEpisodeNo()) {
                        return file;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Matches the list of input episode paths with their Episode object
     * representation.
     * @param inputFiles
     * @return List of matched episodes
     * @throws ParseException if an episode cannot be parsed
     * @throws FileNotFoundException if a TV Show directory cannot be found
     */
    public List<Episode> matchEpisodes(List<String> inputFiles) throws ParseException, FileNotFoundException {
        List<Episode> episodeList = new ArrayList<Episode>(inputFiles.size());
        for(String inputFile : inputFiles) {
            episodeList.add(parse(inputFile));
        }
        return episodeList;
    }
    
    /**
     * Find the TV Show Directory for the given show name that exists in one
     * of the TV Sources. This method will also search for the TV Show
     * Directory using an alias if one exists.
     * This method is a wrapper for findShowDirectory(showName, true);
     * @param showName TV Show Name
     * @return 
     */
    private File findShowDirectory(String showName) {
        return findShowDirectory(showName, true);
    }
    
    /**
     * Find the TV Show Directory for the given show name that exists in one
     * of the TV Sources. If checkAlias is set, the TV Show Alias will also
     * be used to search for the TV Show Directory
     * @param showName TV Show Name
     * @param checkAlias If true, the alias will also be used to search for
     * a TV Show Directory. If false only the given showName will be searched
     * @return TV Show Directory File or null if not found
     */
    private File findShowDirectory(String showName, boolean checkAlias) {
        File tmpFile;
        for(String source : sourceList.getSourceFolders()) {
            if((tmpFile = new File(source, showName)).exists()) {
                return tmpFile;
            }
        }
        if(checkAlias && aliasMap.hasAlias(showName)) {
            return findShowDirectory(aliasMap.getShowName(showName), false);
        }
        return null;
    }
    
}
