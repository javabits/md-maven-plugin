package org.javabits.maven.md;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 * TODO comment
 * Date: 6/17/13
 * Time: 10:49 PM
 *
 * @author Romain Gilles
 */
public final class Resources {
    private Resources() {
        throw new AssertionError("not for you!");
    }


    public static String toString(String path) throws IOException {
        return toString(path, "UTF-8");
    }

    public static String toString(String path, String charset) throws IOException {
        return toString(path, Charset.forName(charset));
    }

    public static String toString(String path, Charset charset) throws IOException {
        return com.google.common.io.Resources.toString(Resources.class.getResource(path), charset);
    }

    public static File copyToDir(String path, File destinationDirectory) throws IOException {
        String name = Paths.get(path).getFileName().toString();
        File toFile = new File(destinationDirectory, name);
        copy(path, toFile);
        return toFile;
    }

    public static void copy(final String path, File toFile) throws IOException {
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return getClass().getResourceAsStream(path);
            }
        }, toFile);
    }
}
