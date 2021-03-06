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
package uk.co.samicemalone.tvmv.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import uk.co.samicemalone.tvmv.Display;
import uk.co.samicemalone.tvmv.exception.FileStillExistsException;
import uk.co.samicemalone.tvmv.model.IOProgress;

/**
 *
 * @author Sam Malone
 */
public abstract class IOOperation {
    
    public enum Type {
        COPY, MOVE
    }
    
    public static IOOperation fromType(Type type) {
        return type == Type.COPY ? new CopyOperation() : new MoveOperation();
    }
    
    protected Path source;
    protected Path destination;
    
    public abstract IOOperation startNative() throws IOException;
    
    public abstract IOOperation startProgress() throws IOException;
    
    public abstract void rollbackOrThrow() throws IOException;
    
    public abstract Type getType();
    
    public abstract IOOperation newInstance();
    
    public void rollback() {
        try {
            rollbackOrThrow();
        } catch (IOException e) {

        }
    }
    
    public IOOperation setOperands(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
        return this;
    }

    public Path getDestination() {
        return destination;
    }

    public Path getSource() {
        return source;
    }
    
    protected void doIO(Path source, Path destination) throws IOException {
        ExecutorService es = Executors.newFixedThreadPool(1);
        BlockingQueue<IOProgress> bq = new LinkedBlockingQueue<>(100);
        es.execute(new ThreadIOProgress(source, destination, bq));
        try {
            IOProgress p;
            while(!(p = bq.take()).hasCompleted()) {
                displayIOProgress(p);
            }
            displayIOProgress(p);
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        es.shutdown();
    }
    
    private void displayIOProgress(IOProgress p) throws IOException {
        if(p.wasError()) {
            Display.onIOProgress(p);
            Display.onPostIO(false);
            throw p.getError();
        }
        Display.onIOProgress(p);
    }
    
    private class ThreadIOProgress implements Runnable {
        
        private final Path source;
        private final Path destination;
        private final BlockingQueue<IOProgress> progress;

        public ThreadIOProgress(Path source, Path destination, BlockingQueue<IOProgress> progress) {
            this.source = source;
            this.destination = destination;
            this.progress = progress;
        }

        @Override
        public void run() {
            long totalBytesWritten = 0;
            long size = 0;
            try (InputStream bis = new BufferedInputStream(Files.newInputStream(source));
                OutputStream bos = new BufferedOutputStream(Files.newOutputStream(destination))) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                size = Files.size(source);
                long bytesPerChar = IOProgress.getBytesPerChar(size);
                long nextCharBytes = bytesPerChar;
                progress.offer(new IOProgress(0, size));
                while((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytesWritten += bytesRead;
                    if(totalBytesWritten >= nextCharBytes && totalBytesWritten != size) {
                        progress.offer(new IOProgress(totalBytesWritten, size));
                        nextCharBytes += bytesPerChar;
                    }
                }
                if(getType() == Type.MOVE) {
                    try {
                        Files.delete(source);
                    } catch (IOException ex) {
                        throw new FileStillExistsException("@|yellow Notice|@: The source file could not be deleted so the move has been rolled back", ex);
                    }
                }
                progress.offer(new IOProgress(size, size));
            } catch (IOException ex) {
                progress.offer(new IOProgress(totalBytesWritten, size, ex));
            }
        }
        
    }
    
}