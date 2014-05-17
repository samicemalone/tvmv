/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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