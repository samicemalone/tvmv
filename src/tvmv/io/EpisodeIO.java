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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import tvmv.EpisodeMatcher;
import tvmv.model.Episode;
import tvmv.util.FileUtil;

/**
 *
 * @author Sam Malone
 */
public class EpisodeIO {
    
    /**
     * The input episode files will be moved to their season directory with
     * the same file name.
     * @param inputFiles List of input episode file paths
     * @param episodeList List of episodes representing the input files
     * @throws IOException if an IO error occurs during moving
     */
    public static void moveEpisodes(List<String> inputFiles, List<Episode> episodeList) throws IOException {
        for(int i = 0; i < inputFiles.size(); i++) {
            moveEpisode(inputFiles.get(i), episodeList.get(i));
        }
    }
 
    /**
     * The input episode file will be moved to its season directory with
     * the same file name.
     * @param inputFile Path of the input episode file
     * @param ep Episode representing the input file
     * @throws IOException if the destination file already exists
     * @throws IOException if an IO error occurs during moving
     */
    public static void moveEpisode(String inputFile, Episode ep) throws IOException {
        File toMove = new File(inputFile);
        File destFile = new File(ep.getSeasonDirectory(), toMove.getName());
        System.out.print("Moving " + toMove.getName() + "...");
        FileUtils.moveFile(toMove, destFile);
        System.out.println("done");
    }
    
    /**
     * 
     * @param matcher Episode Matcher for matching the existing episode
     * @param inputFiles List of paths of the input episode files
     * @param episodeList List of episodes representing the input files
     * @throws IOException if an IO error occurs during replacing an episode
     */
    public static void replaceEpisodes(EpisodeMatcher matcher, List<String> inputFiles, List<Episode> episodeList) throws IOException {
        for(int i = 0; i < inputFiles.size(); i++) {
            replaceEpisode(matcher, inputFiles.get(i), episodeList.get(i));
        }
    }
    
    /**
     * Replaces an existing episode with the input episode file.
     * The input episode file will be moved to its season directory with
     * the same file name. The existing episode will only be removed if
     * moving the input file succeeds.
     * @param matcher Episode Matcher for matching the existing episode
     * @param inputFile Path of the input episode file
     * @param ep Episode representing the input file
     * @throws IOException if an IO error occurs during replacing
     */
    public static void replaceEpisode(EpisodeMatcher matcher, String inputFile, Episode ep) throws IOException {
        File toDelete = matcher.findExistingEpisodeFile(ep);
        if(toDelete == null) {
            throw new FileNotFoundException("Unable to find existing file for " + ep);
        }
        File toMove = new File(inputFile);
        File destFile = new File(ep.getSeasonDirectory(), toMove.getName());
        File tmpFile = FileUtil.getTempFile(ep.getSeasonDirectory());
        System.out.println("Replacing " + toDelete.getName());
        System.out.print(" with " + toMove.getName() + "...");
        
        FileUtils.moveFile(toMove, tmpFile);
        if(!toDelete.delete()) {
            FileUtils.moveFile(tmpFile, toMove); // roll back
            throw new IOException("Could not delete the existing episode at " + toDelete.getAbsolutePath());
        }
        FileUtils.moveFile(tmpFile, destFile);
        System.out.println("done");
    }
    
}
