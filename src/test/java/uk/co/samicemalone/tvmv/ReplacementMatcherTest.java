/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.samicemalone.tvmv;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import uk.co.samicemalone.libtv.model.EpisodeMatch;
import uk.co.samicemalone.libtv.model.TVMap;
import static uk.co.samicemalone.tvmv.ReplacementMatcher.mapReplacements;
import uk.co.samicemalone.tvmv.model.ReplacementMapping;

/**
 *
 * @author Sam Malone
 */
public class ReplacementMatcherTest {
    
    private TVMap sourceMap;
    private TVMap replaceMap;
    
    @Before
    public void setUp() {
        sourceMap = new TVMap();
        sourceMap.addEpisode(new EpisodeMatch("24", 1, 10));
        sourceMap.addEpisode(new EpisodeMatch("24", 1, 11));
        sourceMap.addEpisode(new EpisodeMatch("24", 1, Arrays.asList(12, 13)));
        replaceMap = new TVMap();
        replaceMap.addEpisode(new EpisodeMatch("24", 1, 10));
        replaceMap.addEpisode(new EpisodeMatch("24", 1, 11));
        replaceMap.addEpisode(new EpisodeMatch("24", 1, Arrays.asList(12, 13)));
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test
    public void testMapReplacementsMultiMulti() {
        Set<EpisodeMatch> expResult = new HashSet<>();
        expResult.add(new EpisodeMatch("24", 1, 2));
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(3, 4)));
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(5, 6)));
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(7, 8)));
        sourceMap.addEpisodes(expResult);
        Set<EpisodeMatch> expResultReplace = new HashSet<>();
        expResultReplace.add(new EpisodeMatch("24", 1, Arrays.asList(2, 3, 4, 5)));
        expResultReplace.add(new EpisodeMatch("24", 1, Arrays.asList(6, 7)));
        replaceMap.addEpisodes(expResultReplace);
        EpisodeMatch source = new EpisodeMatch("24", 1, 2);
        EpisodeMatch replace = new EpisodeMatch("24", 1, Arrays.asList(2, 3, 4, 5));
        ReplacementMapping<Set<EpisodeMatch>> result = mapReplacements(sourceMap, replaceMap, source, replace);
        assertEquals(expResult, result.getSource());
        assertEquals(expResultReplace, result.getDestination());
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test
    public void testMapReplacementsSingleMulti() {
        Set<EpisodeMatch> expResult = new HashSet<>();
        expResult.add(new EpisodeMatch("24", 1, 1));
        expResult.add(new EpisodeMatch("24", 1, 2));
        sourceMap.addEpisodes(expResult);
        Set<EpisodeMatch> expResultReplace = new HashSet<>();
        expResultReplace.add(new EpisodeMatch("24", 1, Arrays.asList(1, 2)));
        replaceMap.addEpisodes(expResultReplace);
        ReplacementMapping<Set<EpisodeMatch>> result = mapReplacements(sourceMap, replaceMap, new EpisodeMatch("24", 1, 1), new EpisodeMatch("24", 1, Arrays.asList(1, 2)));
        assertEquals(expResult, result.getSource());
        assertEquals(expResultReplace, result.getDestination());
        // test reverse : multi - single
        result = mapReplacements(replaceMap, sourceMap, new EpisodeMatch("24", 1, Arrays.asList(1, 2)), new EpisodeMatch("24", 1, 1));
        assertEquals(expResultReplace, result.getSource());
        assertEquals(expResult, result.getDestination());
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test(expected = IllegalStateException.class)
    public void testMapReplacementsMultiSingle() {
        Set<EpisodeMatch> expResult = new HashSet<>();
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(2, 3, 4)));
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(5, 6)));
        sourceMap.addEpisodes(expResult);
        Set<EpisodeMatch> expResultReplace = new HashSet<>();
        expResultReplace.add(new EpisodeMatch("24", 1, 2));
        expResultReplace.add(new EpisodeMatch("24", 1, 3));
        expResultReplace.add(new EpisodeMatch("24", 1, Arrays.asList(4, 5)));
        replaceMap.addEpisodes(expResultReplace);
        EpisodeMatch source = new EpisodeMatch("24", 1, Arrays.asList(2, 3, 4));
        EpisodeMatch replace = new EpisodeMatch("24", 1, 2);
        ReplacementMapping<Set<EpisodeMatch>> result = mapReplacements(sourceMap, replaceMap, source, replace);
        assertEquals(expResult, result.getSource());
        assertEquals(expResultReplace, result.getDestination());
        // test reverse throws : single - multi
        mapReplacements(replaceMap, sourceMap, replace, source);
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test(expected = IllegalStateException.class)
    public void testMapReplacementsThrow() {
        EpisodeMatch source = new EpisodeMatch("24", 1, Arrays.asList(1, 2));
        EpisodeMatch replace = new EpisodeMatch("24", 1, Arrays.asList(2, 3));
        sourceMap.addEpisode(source);
        replaceMap.addEpisode(replace);
        mapReplacements(sourceMap, replaceMap, source, replace);
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test
    public void testMapReplacementsSingleSingle() {
        EpisodeMatch e = new EpisodeMatch("24", 1, 2);
        sourceMap.addEpisode(e);
        replaceMap.addEpisode(e);
        ReplacementMapping<Set<EpisodeMatch>> result = mapReplacements(sourceMap, replaceMap, e, e);
        assertEquals(1, result.getSource().size());
        assertEquals(1, result.getDestination().size());
        assertTrue(result.getSource().contains(e));
        assertTrue(result.getDestination().contains(e));
    }

    /**
     * Test of main method, of class ReplacementMatcher.
     */
    @Test
    public void testMapReplacementsOrder() {
        Set<EpisodeMatch> expResult = new HashSet<>();
        expResult.add(new EpisodeMatch("24", 1, Arrays.asList(20, 21)));
        expResult.add(new EpisodeMatch("24", 1, 22));
        sourceMap.addEpisodes(expResult);
        Set<EpisodeMatch> expResultReplace = new HashSet<>();
        expResultReplace.add(new EpisodeMatch("24", 1, 20));
        expResultReplace.add(new EpisodeMatch("24", 1, Arrays.asList(21, 22)));
        replaceMap.addEpisodes(expResultReplace);
        EpisodeMatch source = new EpisodeMatch("24", 1, 22);
        EpisodeMatch replace = new EpisodeMatch("24", 1, Arrays.asList(21, 22));
        ReplacementMapping<Set<EpisodeMatch>> result = mapReplacements(sourceMap, replaceMap, source, replace);
        assertEquals(expResult, result.getSource());
        assertEquals(expResultReplace, result.getDestination());
    }
    
}
