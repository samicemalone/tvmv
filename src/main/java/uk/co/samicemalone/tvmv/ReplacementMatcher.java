/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
     * 
     * @param sourceMap
     * @param destinationMap
     * @param source
     * @param replace
     * @throws IllegalStateException
     * @return 
     */
    public static ReplacementMapping<Set<EpisodeMatch>> mapReplacements(TVMap sourceMap, TVMap destinationMap, EpisodeMatch source, EpisodeMatch replace) {
        Set<EpisodeMatch> sourceSet = new HashSet<>(Collections.singleton(source));
        Set<EpisodeMatch> replaceSet = new HashSet<>(Collections.singleton(replace));
        Set<Integer> foundSourceEps = new HashSet<>();
        List<Integer> tmpEpSource = source.getEpisodes();
        List<Integer> tmpEpReplace = replace.getEpisodes();
        while(!tmpEpSource.isEmpty() || !tmpEpReplace.isEmpty()) {
            Set<Integer> curSource = complement(complement(tmpEpSource, foundSourceEps), tmpEpReplace);
            Set<Integer> curReplace = complement(complement(tmpEpReplace, foundSourceEps), tmpEpSource);
            Set<Integer> curIntersection = intersection(tmpEpSource, tmpEpReplace);
            foundSourceEps.addAll(curIntersection);
            if(!curReplace.isEmpty()) {
                int episode = curReplace.iterator().next();
                EpisodeMatch e = sourceMap.getEpisode(source.getShow(), source.getSeason(), episode);
                if(e == null) {
                    throw new IllegalStateException("Unable to map valid replacements for " + source + " because source episode " + episode + " cannot be found");
                }
                sourceSet.add(e);
                EpisodeMatch m = destinationMap.getEpisode(source.getShow(), source.getSeason(), episode);
                tmpEpSource = e.getEpisodes();
                tmpEpReplace = m == null ? new LinkedList<Integer>() : m.getEpisodes();
            } else if(!curSource.isEmpty()) {
                int episode = curSource.iterator().next();
                EpisodeMatch e = sourceMap.getEpisode(source.getShow(), source.getSeason(), episode);
                EpisodeMatch m = destinationMap.getEpisode(source.getShow(), source.getSeason(), episode);
                if(m == null) {
                    break;
                }
                replaceSet.add(m);
                tmpEpSource = e == null ? new LinkedList<Integer>() : e.getEpisodes();
                tmpEpReplace = m.getEpisodes();
            } else if(curSource.isEmpty() && curReplace.isEmpty()) {
                break;
            }
        }
        return new ReplacementMapping<>(sourceSet, replaceSet);
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
    
    public Set<ReplacementMapping<Set<EpisodeMatch>>> matchReplacements(Collection<EpisodeMatch> matchesToReplace, Collection<Path> destPaths) throws IOException {
        buildDestinationMap(destPaths);
        TVMap sourceMap = new TVMap(matchesToReplace);
        Set<ReplacementMapping<Set<EpisodeMatch>>> replacements = new HashSet<>();
        matchesToReplace = new HashSet<>(matchesToReplace);
        while(!sourceMap.isEmpty()) {
            EpisodeMatch e = matchesToReplace.iterator().next();
            EpisodeMatch destEp = getFirstDestinationEpisode(e);
            ReplacementMapping<Set<EpisodeMatch>> rm;
            if(destEp == null || (!e.isMultiEpisode() && !destEp.isMultiEpisode()) || e.equals(destEp)) {
                Set<EpisodeMatch> destSet = destEp == null ? new HashSet<EpisodeMatch>() : Collections.singleton(destEp);
                rm = new ReplacementMapping<>(Collections.singleton(e), destSet);
            } else {
                rm = mapReplacements(sourceMap, tvMap, e, destEp);
            }
            replacements.add(rm);
            matchesToReplace.removeAll(rm.getSource());
            sourceMap.removeAll(rm.getSource());
            tvMap.removeAll(rm.getDestination());
        }
        return replacements;
    }
    
    
}
