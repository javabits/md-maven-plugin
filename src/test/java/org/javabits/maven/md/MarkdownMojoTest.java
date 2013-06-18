package org.javabits.maven.md;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.nullValue;
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

    @Test
    public void relativePathFromRelativePath() throws Exception {
        Path cssPath = Paths.get("base.css");
        Path userGuidePath = Paths.get("user-guide/user-guide.html").getParent();
        Path indexPath = Paths.get("index.html").getParent();
        assertThat(userGuidePath.relativize(cssPath).toString(), is(".." + File.separatorChar + "base.css"));
        assertThat(indexPath, is(nullValue()));
    }

    @Test
    public void relativePathFromAbsolutePath() throws Exception {
        Path cssPath = Paths.get("base.css").toAbsolutePath();
        Path userGuidePath = Paths.get("user-guide/user-guide.html").toAbsolutePath().getParent();
        Path indexPath = Paths.get("index.html").toAbsolutePath().getParent();
        assertThat(userGuidePath.relativize(cssPath).toString(), is(".." + File.separatorChar + "base.css"));
        assertThat(indexPath.relativize(cssPath).toString(), is("base.css"));
    }

}
