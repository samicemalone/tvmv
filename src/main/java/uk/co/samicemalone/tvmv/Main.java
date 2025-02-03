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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import uk.co.samicemalone.libtv.exception.MatchException;
import uk.co.samicemalone.libtv.matcher.path.AliasedTVLibrary;
import uk.co.samicemalone.libtv.model.AliasMap;
import uk.co.samicemalone.libtv.model.EpisodeMatch;
import uk.co.samicemalone.tvmv.exception.OSNotSupportedException;
import uk.co.samicemalone.tvmv.io.EpisodeIO;
import uk.co.samicemalone.tvmv.io.reader.AliasReader;
import uk.co.samicemalone.tvmv.io.reader.ConfigReader;
import uk.co.samicemalone.tvmv.io.reader.StringListReader;
import uk.co.samicemalone.tvmv.model.Config;
import uk.co.samicemalone.tvmv.model.Environment;
import uk.co.samicemalone.tvmv.model.ReplacementMapping;

/**
 *
 * @author Sam Malone
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Args arguments = Args.parse(args);
            if(arguments.isHelpSet()) {
                printHelp();
                System.exit(0);
            }
            Config config = ConfigReader.read(arguments.getConfigFile());
            Environment env = new Environment(arguments, config).initialise();
            run(env);
        } catch(Exception e) {
            AnsiConsole.err().println(Ansi.ansi().render(e.getMessage()));
            Throwable t = e.getCause();
            if(t != null) {
                AnsiConsole.err().print(Ansi.ansi().render(" @|yellow Cause|@: "));
                System.err.println(t.getMessage());
            }
            System.exit(1);
        }
    }
    
    private static void run(Environment env) throws IOException, IllegalStateException, OSNotSupportedException, MatchException{
        AliasMap aliasMap = AliasReader.read(new AliasMap());
        AliasedTVLibrary library = new AliasedTVLibrary(env.getTvDestinationPaths(), aliasMap);
        EpisodeMatcher matcher = new EpisodeMatcher(env.getArgs().getShowOverride(), env.getArgs().isSkipNotMatchedSet());
        EpisodeIO episodeIO = new EpisodeIO(library, env.getArgs().isNativeIOSet());
        if(env.getCreateShowsFile() != null && env.getCreateDestShowsDir() != null) {
            for(String showName : StringListReader.read(Paths.get(env.getCreateShowsFile()))) {
                Path toCreate = Paths.get(env.getCreateDestShowsDir(), showName);
                if(!Files.exists(toCreate)) {
                    Files.createDirectory(toCreate);
                }
            }
        }
        List<EpisodeMatch> episodeList = matcher.matchEpisodes(env.getSourcePaths());
        Set<Path> destPaths = episodeIO.createDestinationDirectories(episodeList);
        if(env.getArgs().isReplaceSet()) {
            ReplacementMatcher rMatcher = new ReplacementMatcher();
            Set<ReplacementMapping<Set<EpisodeMatch>>> rm = rMatcher.matchReplacements(episodeList, destPaths);
            for(ReplacementMapping<Set<EpisodeMatch>> replacementMapping : rm) {
                episodeIO.replaceEpisode(env.getArgs().getIOOperation(), replacementMapping);
            }
        } else {
            for(EpisodeMatch e : episodeList) {
                Path destDir = library.newEpisodesPath(e.getShow(), e.getSeason());
                episodeIO.start(env.getArgs().getIOOperation(), e, destDir);
            }
        }
    }
    
    public static void printHelp() {
        System.out.println("Usage:   tvmv FILE|DIR... [-chnrs]");
        System.out.println();
        System.out.println("Matches each episode FILE or each file in DIR to determine the TV show name,");
        System.out.println("season number and episode number. Each episode file will then be moved the");
        System.out.println("matching TV source folder as defined in sources.conf");
        System.out.println();
        System.out.println("The replacements are multi-episode aware, so only complete episode replacements");
        System.out.println("are accepted. For example \"24 - s01e01\" could not replace \"24 - s01e01e02\"");
        System.out.println("because episode 2 would be removed. For this replacement to succeed,");
        System.out.println("\"24 - s01e02\" must also exist as an input episode file.");
        System.out.println();
        System.out.println("   --config FILE             Use this specific tvmv.conf file");
        System.out.println("   -c, --copy                Copy the input FILEs instead of moving them");
        System.out.println("   -h, --help                Prints this message");
        System.out.println("   -n, --native              Use Java NIO API's for IO operations instead of");
        System.out.println("                             Java IO Streams. Native IO will not display a ");
        System.out.println("                             progress bar, but will avoid a copy and delete");
        System.out.println("                             from the same filesystem if moving");
        System.out.println("   -o, --override-show SHOW  Dont detect show, use the value given");
        System.out.println("   -r, --replace             Replaces existing episodes");
        System.out.println("   -s, --skip-not-matched    Skip input files that cannot be matched instead");
        System.out.println("                             of exiting");
        System.out.println();
        System.out.println("TV show aliases can be defined in aliases.txt");
    }
}
