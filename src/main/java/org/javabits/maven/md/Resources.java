package org.javabits.maven.md;

import java.io.IOException;
import java.nio.charset.Charset;

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

}
