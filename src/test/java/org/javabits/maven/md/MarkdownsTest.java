package org.javabits.maven.md;

import org.junit.Assert;
import org.junit.Test;

import static org.javabits.maven.md.Markdowns.NO_TITLE;

/**
 * TODO comment
 * Date: 6/17/13
 * Time: 10:38 PM
 *
 * @author Romain Gilles
 */
public class MarkdownsTest {

    private static final String EXPECTED_TITLE = "The Title";
    private static final String ATX_STYLE_TITLE = "# " + EXPECTED_TITLE;

    @Test
    public void testGetTitle() throws Exception {
        String test = Resources.toString("/test.md");
        Assert.assertEquals(EXPECTED_TITLE, Markdowns.getTitle(test));
    }

    @Test
    public void testGetTitle_forEmptyFirstLine() throws Exception {
        String test = Resources.toString("/test-empty-first-lines.md");
        Assert.assertEquals(EXPECTED_TITLE, Markdowns.getTitle(test));
    }

    @Test
    public void testGetTitle_emptyFile() throws Exception {
        Assert.assertEquals(NO_TITLE, Markdowns.getTitle(""));
    }

    @Test
    public void testGetTitle_Atx() throws Exception {
        Assert.assertEquals(EXPECTED_TITLE, Markdowns.getTitle(ATX_STYLE_TITLE));
    }

    @Test
    public void testGetTitle_AtxEmptyFile() throws Exception {
        Assert.assertEquals(NO_TITLE, Markdowns.getTitle("# "));
    }
}
