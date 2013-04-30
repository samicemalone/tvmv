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
package tvmv.model;

import java.io.File;

/**
 *
 * @author Sam Malone
 */
public class Episode {
    
    private String show;
    private int seasonNo;
    private int episodeNo;
    private File showDirectoryFile;
    private String seasonPrefix = "Season";

    /**
     * Creates an instance of Episode
     * @param show TV Show
     * @param seasonNo Season number
     * @param episodeNo Episode number
     */
    public Episode(String show, int seasonNo, int episodeNo) {
        this.show = show;
        this.seasonNo = seasonNo;
        this.episodeNo = episodeNo;
    }
    
    /**
     * Creates an empty instance of Episode
     */
    public Episode() {
        
    }
    
    /**
     * Gets the episode number
     * @return Episode number
     */
    public int getEpisodeNo() {
        return episodeNo;
    }

    /**
     * Sets the episode number
     * @param episodeNo Episode number
     */
    public void setEpisodeNo(int episodeNo) {
        this.episodeNo = episodeNo;
    }

    /**
     * Gets the season number
     * @return Season number
     */
    public int getSeasonNo() {
        return seasonNo;
    }

    /**
     * Sets the season number
     * @param seasonNo Season number
     */
    public void setSeasonNo(int seasonNo) {
        this.seasonNo = seasonNo;
    }

    /**
     * Sets the TV Show
     * @return 
     */
    public String getShow() {
        return show;
    }

    /**
     * Gets the TV Show
     * @param show 
     */
    public void setShow(String show) {
        this.show = show;
    }

    /**
     * Gets the File representing the TV Show directory
     * @return Show Directory File or null if not set
     */
    public File getShowDirectory() {
        return showDirectoryFile;
    }

    /**
     * Sets the TV Show Directory File
     * @param showDirectory Show Directory File
     */
    public void setShowDirectory(File showDirectory) {
        this.showDirectoryFile = showDirectory;
    }

    /**
     * Get the prefix for the season folder
     * @return season folder prefix
     */
    public String getSeasonPrefix() {
        return seasonPrefix;
    }

    /**
     * Set the prefix for the season folder
     * @param seasonPrefix season folder prefix e.g. Season, Series
     */
    public void setSeasonPrefix(String seasonPrefix) {
        this.seasonPrefix = seasonPrefix;
    }
    
    /**
     * Get the Season Directory File for this episodes\' season.
     * If the Show Directory File has not been set, this method will return
     * null
     * @return Season Directory File or null if the Show Directory is not set
     */
    public File getSeasonDirectory() {
        if(showDirectoryFile != null) {
            return new File(showDirectoryFile, seasonPrefix + " " + String.valueOf(seasonNo));
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s S%02dE%02d", show, seasonNo, episodeNo);
    }
    
}
