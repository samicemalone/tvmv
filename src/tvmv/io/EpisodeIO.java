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
     * The input episode file will be copied to its season directory with
     * the same file name.
     * @param inputFile Path of the input episode file
     * @param ep Episode representing the input file
     * @throws IOException if the destination file already exists
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyEpisode(String inputFile, Episode ep) throws IOException {
        File toCopy = new File(inputFile);
        File destFile = new File(ep.getSeasonDirectory(), toCopy.getName());
        System.out.print("Copying " + toCopy.getName() + "...");
        FileUtils.copyFile(toCopy, destFile);
        System.out.println("done");
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
     * Replaces an existing episode with the input episode file.
     * The input episode file will be moved to its season directory with
     * the same file name if moveOriginal is set. If moveOriginal is false,
     * the input episode file will be copied instead of moved.
     * The existing episode will only be removed if moving/copying the input
     * file succeeds.
     * @param matcher Episode Matcher for matching the existing episode
     * @param inputFile Path of the input episode file
     * @param ep Episode representing the input file
     * @param moveOriginal if true, the input file will be moved. if false,
     * the input file will be copied.
     * @throws IOException if an IO error occurs during replacing
     */
    public static void replaceEpisode(EpisodeMatcher matcher, String inputFile, Episode ep, boolean moveOriginal) throws IOException {
        File toDelete = matcher.findExistingEpisodeFile(ep);
        if(toDelete == null) {
            throw new FileNotFoundException("Unable to find existing file for " + ep);
        }
        File toMove = new File(inputFile);
        File destFile = new File(ep.getSeasonDirectory(), toMove.getName());
        File tmpFile = FileUtil.getTempFile(ep.getSeasonDirectory(), toMove.getName());
        System.out.println("Replacing " + toDelete.getName());
        System.out.print(" with " + toMove.getName() + "...");
        if(moveOriginal) {
            FileUtils.moveFile(toMove, tmpFile);
        } else {
            FileUtils.copyFile(toMove, tmpFile);
        }
        if(!toDelete.delete()) {
            if(moveOriginal) {
                FileUtils.moveFile(tmpFile, toMove); // roll back
            } else {
                tmpFile.delete(); // delete tmp copy
            }
            throw new IOException("Could not delete the existing episode at " + toDelete.getAbsolutePath());
        }
        FileUtils.moveFile(tmpFile, destFile);
        System.out.println("done");
    }
    
}
