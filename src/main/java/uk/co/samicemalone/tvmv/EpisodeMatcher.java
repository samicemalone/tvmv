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

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import uk.co.samicemalone.libtv.VideoFilter;
import uk.co.samicemalone.libtv.exception.MatchException;
import uk.co.samicemalone.libtv.matcher.TVMatcher;
import uk.co.samicemalone.libtv.model.EpisodeMatch;
import uk.co.samicemalone.libtv.matcher.TVMatcher.MatchElement;

/**
 *
 * @author Sam Malone
 */
public class EpisodeMatcher {
    
    private final boolean isSkipNotMatched;
    private final String tvShow;
    
    /**
     * Create a new instance of episode matcher
     * @param tvShow TV show
     * @param isSkipNotMatched if true, files that cannot be matched will be
     * skipped. if false, an exception will be thrown when unable to match 
     */
    public EpisodeMatcher(String tvShow, boolean isSkipNotMatched) {
        this.tvShow = tvShow;
        this.isSkipNotMatched = isSkipNotMatched;
    }
    
    /**
     * Create a new instance of episode matcher
     * @param isSkipNotMatched if true, files that cannot be matched will be
     * skipped. if false, an exception will be thrown when unable to match 
     */
    public EpisodeMatcher(boolean isSkipNotMatched) {
        this.tvShow = null;
        this.isSkipNotMatched = isSkipNotMatched;
    }
    
    /**
     * Create a new instance of episode matcher for a given show
     * @param tvShow TV show
     */
    public EpisodeMatcher(String tvShow) {
        this.tvShow = tvShow;
        this.isSkipNotMatched = false;
    }
    
    /**
     * Matches the list of input episode paths or directory paths
     * @param inputFiles list of episode files or directories (can be mixed)
     * @return List of matched episodes
     * @throws uk.co.samicemalone.libtv.exception.MatchException if a match could not
     * be found for an input file and this episode matcher doesn't skip unmatched episodes
     */
    public List<EpisodeMatch> matchEpisodes(List<String> inputFiles) throws MatchException {
        TVMatcher tvMatcher = new TVMatcher();
        List<EpisodeMatch> episodeList = new ArrayList<>(inputFiles.size());
        for(String inputFile : inputFiles) {
            Path p = Paths.get(inputFile);
            if(Files.isDirectory(p)) {
                matchEpisodesInDir(tvMatcher, episodeList, p);
            } else {
                EpisodeMatch m = matchEpisode(tvMatcher, p);
                if(m != null) {
                    episodeList.add(m);
                }
            }
        }
        return episodeList;
    }
    
    private EpisodeMatch matchEpisode(TVMatcher tvMatcher, Path path) throws MatchException {
        MatchElement me = tvShow == null ? MatchElement.ALL : MatchElement.SEASON;
        if(isSkipNotMatched) {
            EpisodeMatch e = tvMatcher.matchElement(path, me);
            if(e == null) {
                Display.onSkipNotMatched(path);
            } else if(tvShow != null) {
                e.setShow(tvShow);
            }
            return e;
        } else {
            EpisodeMatch e = tvMatcher.matchOrThrow(path, me);
            if(tvShow != null) {
                e.setShow(tvShow);
            }
            return e;
        }
    }
    
    private void matchEpisodesInDir(TVMatcher tvMatcher, List<EpisodeMatch> matches, Path dirPath) throws MatchException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, new VideoFilter())) {
            for(Path path : stream) {
                EpisodeMatch e = matchEpisode(tvMatcher, path);
                if(e != null) {
                    matches.add(e);
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            
        }
    }
    
}
