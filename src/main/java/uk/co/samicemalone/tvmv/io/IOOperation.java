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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author Sam Malone
 */
public abstract class IOOperation {
    
    public enum Type {
        COPY, MOVE
    }
    
    public static IOOperation fromType(Type type) {
        return type == Type.COPY ? new Copy() : new Move();
    }
    
    protected Path source;
    protected Path destination;
    
    public abstract IOOperation start() throws IOException;
    
    public abstract void rollbackOrThrow() throws IOException;
    
    public abstract Type getType();
    
    public abstract IOOperation newInstance();
    
    public IOOperation start(Path source, Path destination) throws IOException {
        setOperands(source, destination);
        return start();
    }
    
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
    
    public static class Move extends IOOperation {        
        @Override
        public IOOperation start() throws IOException {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return this;
        }

        @Override
        public void rollbackOrThrow() throws IOException {
            Files.move(destination, source, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public Type getType() {
            return Type.MOVE;
        }

        @Override
        public IOOperation newInstance() {
            return new Move();
        }
    }
    
    public static class Copy extends IOOperation {
        @Override
        public IOOperation start() throws IOException {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return this;
        }
        
        @Override
        public void rollbackOrThrow() throws IOException {
            Files.delete(destination);
        }

        @Override
        public Type getType() {
            return Type.COPY;
        }

        @Override
        public IOOperation newInstance() {
            return new Copy();
        }
    }
    
}