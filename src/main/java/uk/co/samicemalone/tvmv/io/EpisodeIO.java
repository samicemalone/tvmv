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
package uk.co.samicemalone.tvmv.io;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.samicemalone.libtv.exception.SeasonsPathNotFoundException;
import uk.co.samicemalone.libtv.matcher.path.AliasedTVLibrary;
import uk.co.samicemalone.libtv.model.EpisodeMatch;
import uk.co.samicemalone.tvmv.Display;
import uk.co.samicemalone.tvmv.exception.FileStillExistsException;
import uk.co.samicemalone.tvmv.model.DequeStack;
import uk.co.samicemalone.tvmv.model.ReplacementMapping;
import uk.co.samicemalone.tvmv.model.Stack;

/**
 *
 * @author Sam Malone
 */
public class EpisodeIO {

    private final AliasedTVLibrary tvLibrary;
    private final boolean useNativeIO;

    public EpisodeIO(AliasedTVLibrary tvLibrary, boolean useNativeIO) {
        this.tvLibrary = tvLibrary;
        this.useNativeIO = useNativeIO;
    }
    
    /**
     * Start the IO operation using the EpisodeMatch as the source path. The
     * IO operation destination path uses the same file name as the source path
     * but the directory is specified by destinationDir
     * @param iop IOOperation used to determine operation type e.g. copy or move
     * @param sourceEpisode EpisodeMatch to startNative IO
     * @param destinationDir destination path to store the source episode
     * @throws IOException if the destination file already exists or
     * if an IO error occurs
     */
    public void start(IOOperation iop, EpisodeMatch sourceEpisode, Path destinationDir) throws IOException {
        Path destPath = destinationDir.resolve(sourceEpisode.getEpisodeFile().getName());
        if(Files.exists(destPath)) {
            throw new FileAlreadyExistsException(sourceEpisode.getEpisodeFile().getAbsolutePath(), destPath.toString(), "Destination file already exists.");
        }
        iop.setOperands(sourceEpisode.getEpisodeFile().toPath(), destPath);
        Display.onPreIO(iop, useNativeIO);
        if(useNativeIO) {
            iop.startNative();
        } else {
            iop.startProgress();
        }
        Display.onPostIO(useNativeIO);
    }
    
    /**
     * Replaces existing episodes according the IO operation and mapping specified.
     * The replacement is implemented by moving the destination file(s) to a 
     * temporary file in the same directory. The source(s) are then moved/copied
     * to the destination. If the IO operation was successful, the old temporary
     * destination file will be deleted. Otherwise an attempt will be made to
     * roll back the IO operation.
     * @param iop IOOperation used to determine operation type e.g. copy or move
     * @param mapping a mapping of source episodes required to replace the 
     * destination episodes
     * @throws IOException if an IO error occurs during replacing
     */
    public void replaceEpisode(IOOperation iop, ReplacementMapping<Set<EpisodeMatch>> mapping) throws IOException {
        Path destDir = getEpisodesPath(mapping.getSource().iterator().next());
        List<Path> tmpDestinations = new ArrayList<>(mapping.getDestination().size());
        Stack<IOOperation> tmpTransactions = new DequeStack<>(mapping.getDestination().size());
        IOOperation io = null;
        try {
            Display.onPreRemoveOld(mapping.getDestination().size());
            for(EpisodeMatch destMatch : mapping.getDestination()) {
                String destFileName = destMatch.getEpisodeFile().getName();
                Path tmpPath = Files.createTempFile(destDir, destFileName, ".old.tmp");
                tmpDestinations.add(tmpPath);
                io = new MoveOperation().setOperands(destMatch.getEpisodeFile().toPath(), tmpPath);
                Display.onPreIORemoveOld(destFileName);
                tmpTransactions.push(io.startNative());
            }
            Display.onPreReplace(iop.getType(), mapping.getDestination().size());
            for(EpisodeMatch sourceMatch : mapping.getSource()) {
                Path destPath = destDir.resolve(sourceMatch.getEpisodeFile().getName());
                io = iop.newInstance().setOperands(sourceMatch.getEpisodeFile().toPath(), destPath);
                Display.onPreIOReplace(io, useNativeIO);
                tmpTransactions.push(useNativeIO ? io.startNative() : io.startProgress());
                Display.onPostIOReplace(useNativeIO);
            }
        } catch (FileStillExistsException e) {
            if(io != null) {
                deleteQuietly(io.getDestination());
            }
            rollback(tmpTransactions);
            throw e;
        } catch (IOException e) {
            rollback(tmpTransactions);
            throw e;
        }
        deleteAllPaths(destDir, tmpDestinations);
    }
    
    private void deleteQuietly(Path p) {
        try {
            Files.delete(p);
        } catch(IOException e) {
            
        }
    }
    
    private void deleteAllPaths(Path destDir, Collection<Path> paths) throws IOException {
        boolean deletedAll = true;
        for(Path tmpDestPath : paths) {
            deletedAll &= tmpDestPath.toFile().delete();
        }
        if(!deletedAll) {
            throw new IOException("Unable to delete an existing episode that is to be replaced. Check " + destDir + " for .tmp files.");
        }
    }
    
    private void rollback(Stack<IOOperation> transactions) {
        IOOperation io;
        while((io = transactions.pop()) != null) {
            //Display.onIORollback(io);
            io.rollback();
        }
    }
    
    /**
     * Create the destination directories for the source episodes given.
     * @param sourceEpisodes
     * @return set of the destination directories for the episodes in sourceEpisodes
     * @throws IOException if unable to create the new episodes directory path
     * @throws SeasonsPathNotFoundException if unable to find the seasons path
     */
    public Set<Path> createDestinationDirectories(Collection<EpisodeMatch> sourceEpisodes) throws IOException {
        Set<Path> paths = new HashSet<>(sourceEpisodes.size());
        for(EpisodeMatch e : sourceEpisodes) {
            paths.add(tvLibrary.newEpisodesPath(e.getShow(), e.getSeason()));
        }
        return paths;
    }
    
    private Path getEpisodesPath(EpisodeMatch m) {
        return tvLibrary.getEpisodesPath(m.getShow(), m.getSeason());
    }
    
}
