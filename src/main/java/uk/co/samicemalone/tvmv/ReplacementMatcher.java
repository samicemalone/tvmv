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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import uk.co.samicemalone.libtv.VideoFilter;
import uk.co.samicemalone.libtv.matcher.EpisodeMatcher;
import uk.co.samicemalone.libtv.model.EpisodeMatch;
import uk.co.samicemalone.libtv.model.TVMap;
import uk.co.samicemalone.libtv.util.PathUtil;
import uk.co.samicemalone.tvmv.model.ReplacementMapping;

/**
 *
 * @author Sam Malone
 */
public class ReplacementMatcher {

    /**
     * Create a ReplacementMapping of a set of source episodes that are required
     * in order to replace a set of episodes that already exist in the destination.
     * For example (Let [x...] be an EpisodeMatch of show "24", season 1, episodes x...):
     * <p>Let {@code sourceMap = [1], [2, 3], [4]...} and {@code destinationMap = [1, 2], [3], [4]...}
     * <br>Let {@code source = [1]} and {@code dest = [1, 2]}
     * <br>source cannot replace dest because episode 2 would lost, so each episode
     * number must also be checked for existence in sourceMap/destinationMap
     * <br>This example would return a replacement mapping of
     * Source: [1], [2, 3] and Destination: [1, 2], [3]
     * <p>If a source episode contains multiple episode numbers, and not all the 
     * episode numbers exist in the destinationMap, the replacement is still valid.
     * For example - Source: [1, 2] can replace Destination: [1]
     * @param sourceMap source map containing all the source episodes available
     * @param destinationMap destination map containing all the destination episodes available
     * @param source EpisodeMatch that will replace {@code dest}
     * @param dest destination EpisodeMatch containing any episode number that 
     * {@code source} contains.
     * @throws IllegalStateException if an episode number exists in the destinationMap but
     * the episode number doesn't exist in sourceMap. E.g. if trying to replace episodes
     * [2, 3] with [1, 2] (episode 3 would be lost in the replacement)
     * @return ReplacementMapping of a set of source episodes that are required
     * in order to replace a set of episodes that already exist in the destination.
     */
    public static ReplacementMapping<Set<EpisodeMatch>> mapReplacements(TVMap sourceMap, TVMap destinationMap, EpisodeMatch source, EpisodeMatch dest) {
        Set<EpisodeMatch> sourceSet = new HashSet<>(Collections.singleton(source));
        Set<EpisodeMatch> destSet = new HashSet<>(Collections.singleton(dest));
        Set<Integer> foundSourceEps = new HashSet<>();
        List<Integer> tmpEpSource = source.getEpisodes();
        List<Integer> tmpEpDestination = dest.getEpisodes();
        while(!tmpEpSource.isEmpty() || !tmpEpDestination.isEmpty()) {
            Set<Integer> curSource = complement(complement(tmpEpSource, foundSourceEps), tmpEpDestination);
            Set<Integer> curDest = complement(complement(tmpEpDestination, foundSourceEps), tmpEpSource);
            Set<Integer> curIntersection = intersection(tmpEpSource, tmpEpDestination);
            foundSourceEps.addAll(curIntersection);
            if(!curDest.isEmpty()) {
                int episode = curDest.iterator().next();
                EpisodeMatch e = sourceMap.getEpisode(source.getShow(), source.getSeason(), episode);
                if(e == null) {
                    throw new IllegalStateException("Unable to map valid replacements for " + source + " because source episode " + episode + " cannot be found");
                }
                sourceSet.add(e);
                EpisodeMatch m = destinationMap.getEpisode(source.getShow(), source.getSeason(), episode);
                tmpEpSource = e.getEpisodes();
                tmpEpDestination = m == null ? new LinkedList<Integer>() : m.getEpisodes();
            } else if(!curSource.isEmpty()) {
                int episode = curSource.iterator().next();
                EpisodeMatch e = sourceMap.getEpisode(source.getShow(), source.getSeason(), episode);
                EpisodeMatch m = destinationMap.getEpisode(source.getShow(), source.getSeason(), episode);
                if(m == null) {
                    break;
                }
                destSet.add(m);
                tmpEpSource = e == null ? new LinkedList<Integer>() : e.getEpisodes();
                tmpEpDestination = m.getEpisodes();
            } else if(curSource.isEmpty() && curDest.isEmpty()) {
                break;
            }
        }
        return new ReplacementMapping<>(sourceSet, destSet);
    }
    
    private static <T> Set<T> complement(Collection<T> set, Collection<T> remove) {
        HashSet<T> s = new HashSet<>(set);
        s.removeAll(remove);
        return s;
    }
    
    private static <T> Set<T> intersection(Collection<T> a, Collection<T> b) {
        HashSet<T> s = new HashSet<>(a);
        s.retainAll(b);
        return s;
    }
    
    private final TVMap tvMap;
    private final EpisodeMatcher episodeMatcher;

    public ReplacementMatcher() {
        tvMap = new TVMap();
        episodeMatcher = new EpisodeMatcher();
    }
    
    private void buildDestinationMap(Collection<Path> destinationDirPaths) throws IOException {
        for(Path destDir : destinationDirPaths) {
            tvMap.addEpisodes(episodeMatcher.match(PathUtil.listPaths(destDir, new VideoFilter())));
        }
    }
    
    private EpisodeMatch getFirstDestinationEpisode(EpisodeMatch source) {
        for(int n : source.getEpisodes()) {
            EpisodeMatch destEp = tvMap.getEpisode(source.getShow(), source.getSeason(), n);
            if(destEp != null) {
                return destEp;
            }
        }
        return null;
    }
    
    /**
     * Match the sets of episodes from the collection given, that are to replace
     * the matching episodes that exist in the destination paths given.
     * <p>The mapping destination may be empty if no matching episodes could be
     * found in the destination. 
     * @see #mapReplacements(uk.co.samicemalone.libtv.model.TVMap, uk.co.samicemalone.libtv.model.TVMap, uk.co.samicemalone.libtv.model.EpisodeMatch, uk.co.samicemalone.libtv.model.EpisodeMatch) 
     * @param sourceEpisodes collection of episode matches to find replacements for
     * @param destPaths collection of episode directory paths used to match
     * the episodes, in order to determine the episodes to be replaced
     * @return ReplacementMapping Set containing a set of source episodes that are required
     * in order to replace a set of episodes that already exist in the destination.
     * @throws IllegalStateException
     * @throws IOException if unable to list files in the destination paths
     */
    public Set<ReplacementMapping<Set<EpisodeMatch>>> matchReplacements(Collection<EpisodeMatch> sourceEpisodes, Collection<Path> destPaths) throws IOException {
        buildDestinationMap(destPaths);
        TVMap sourceMap = new TVMap(sourceEpisodes);
        Set<ReplacementMapping<Set<EpisodeMatch>>> replacements = new HashSet<>();
        sourceEpisodes = new HashSet<>(sourceEpisodes);
        while(!sourceMap.isEmpty()) {
            EpisodeMatch e = sourceEpisodes.iterator().next();
            EpisodeMatch destEp = getFirstDestinationEpisode(e);
            ReplacementMapping<Set<EpisodeMatch>> rm;
            if(destEp == null || (!e.isMultiEpisode() && !destEp.isMultiEpisode()) || e.equals(destEp)) {
                Set<EpisodeMatch> destSet = destEp == null ? new HashSet<EpisodeMatch>() : Collections.singleton(destEp);
                rm = new ReplacementMapping<>(Collections.singleton(e), destSet);
            } else {
                rm = mapReplacements(sourceMap, tvMap, e, destEp);
            }
            replacements.add(rm);
            sourceEpisodes.removeAll(rm.getSource());
            sourceMap.removeAll(rm.getSource());
            tvMap.removeAll(rm.getDestination());
        }
        return replacements;
    }
    
    
}
