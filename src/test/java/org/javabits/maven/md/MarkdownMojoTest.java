package org.javabits.maven.md;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.javabits.maven.md.MarkdownMojo.getDestinationFilePath;

/**
 * TODO comment
 * Date: 6/18/13
 * Time: 4:15 AM
 *
 * @author Romain Gilles
 */
public class MarkdownMojoTest {

    @Test
    public void testGetDestinationFilePathSimple() throws Exception {
        assertThat(getDestinationFilePath("user-guide.md", "html"), is("user-guide.html"));
    }

    @Test
    public void testGetDestinationFilePathChildPath() throws Exception {
        assertThat(getDestinationFilePath("user-guide/user-guide.md", "mkd"), is("user-guide" + File.separatorChar + "user-guide.mkd"));
    }
}
