/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.samicemalone.tvmv;

import java.nio.file.Path;
import uk.co.samicemalone.tvmv.io.IOOperation;

/**
 *
 * @author Sam Malone
 */
public class Display {
    
    private static String getIODescription(IOOperation.Type type) {
        return type == IOOperation.Type.COPY ? "Copying" : "Moving";
    }
    
    public static void onPreRemoveOld(int replacementCount) {
        if(replacementCount > 0) {
            System.out.println("Replacing");
        }
    }
    
    public static void onPreIORemoveOld(String fileName) {
        System.out.format("  %s\n", fileName);
    }
    
    public static void onPreReplace(IOOperation.Type type, int replacementCount) {
        if(replacementCount > 0) {
            System.out.format("with\n");
        } else {
            System.out.println(getIODescription(type));
        }
    }
    
    public static void onPreIOReplace(IOOperation iop) {
        System.out.format("  %s...", iop.getSource().getFileName());
    }
    
    public static void onPostIOReplace() {
        onPostIO();
    }
    
    public static void onPreIO(IOOperation iop) {
        System.out.format("%s %s...", getIODescription(iop.getType()), iop.getSource().getFileName());
    }
    
    public static void onPostIO() {
        System.out.println("done");
    }
    
    public static void onSkipNotMatched(Path skippedPath) {
        System.out.println("Skipping: " + skippedPath);
    }
    
}
