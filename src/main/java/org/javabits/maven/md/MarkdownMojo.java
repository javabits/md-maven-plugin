package org.javabits.maven.md;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * This plugin is responsible to generate Html files from Markdown input files.
 * The output file is the result of the merge of a template http file and the
 * output of the parsing of the input file. You may want to change the html template file.
 * Or you may simply change the css used in the original template.
 * TODO provide a way to change the template or the css.
 * @author Romain Gilles
 */
@Mojo(name = "generate")
public class MarkdownMojo extends AbstractMojo {

    private static final String DEFAULT_FILE_EXTENSION = "md";
    private static final String FILE_FILTER = "**/*." + DEFAULT_FILE_EXTENSION;
    private static final String[] DEFAULT_INCLUDES = new String[]{FILE_FILTER};
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String DEFAULT_OUTPUT_DIRECTORY = "${project.build.directory}/site";
    /**
     * The input directory from where the input files will be looked for generation.
     * By default it points to {@code ${basedir}/src/main/md}
     */
    @Parameter(property = "md.sources", defaultValue = "${basedir}/src/main/md")
    private File sources;
    /**
     * The default charset to use when read the source files.
     * By default it uses {@code UTF-8} charset.
     */
    @Parameter(property = "md.charset", defaultValue = DEFAULT_CHARSET)
    private String charset;
    /**
     * The output directory where the generated files will be written.
     * By default it's {@code ${project.build.directory}/site}
     */
    @Parameter(property = "md.output.dir", defaultValue = DEFAULT_OUTPUT_DIRECTORY)
    private File outputDir;

    /**
     * List of include patterns to use to discover
     * the files that need to be parsed.
     * By default it includes all the '.md' files.
     */
    @Parameter
    private String[] includes;

    /**
     * List of options name to give to the parser.
     * Values of the names can be found at <a href="http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html">Extensions javadoc</a>
     * Or {@link Extensions Extensions class}.
     */
    @Parameter
    private String[] options;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Markdown");
        PegDownProcessor pegDownProcessor = new PegDownProcessor(getOptions());
        DirectoryScanner markDownScanner = new DirectoryScanner();
        getLog().debug("Scan files " + Arrays.toString(getIncludes()) + ", from: " + sources);
        markDownScanner.setIncludes(getIncludes());
        markDownScanner.setBasedir(sources);
        markDownScanner.scan();
        String[] includedFiles = markDownScanner.getIncludedFiles();
        getLog().debug("Included files: " + Arrays.toString(includedFiles));
        String templateFile = null;
        try {
            templateFile = Resources.toString(getClass().getResource("/file-template.html"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read the template-file", e);
        }

        for (String includedFile : includedFiles) {
            try {
                Charset charset = Charset.forName(this.charset);
                String file = Files.toString(getInputFile(includedFile), charset);
                getLog().debug("Transform file: " + file);
                String html = pegDownProcessor.markdownToHtml(file);
                File destinationFile = getDestinationFile(includedFile);
                getLog().debug("To file: " + file);
                File parentFile = destinationFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                    Files.copy(new InputSupplier<InputStream>() {
                        @Override
                        public InputStream getInput() throws IOException {
                            return getClass().getResourceAsStream("/base.css");
                        }
                    }, new File(parentFile, "base.css"));
                }
                html = templateFile.replace("${content}", html);
                Files.write(html, destinationFile, charset);
            } catch (IOException e) {
                throw new MojoExecutionException("IO exception when generated from: " + includedFile, e);
            }
        }
    }

    private File getInputFile(String includedFile) {
        return new File(sources, includedFile);
    }

    private File getDestinationFile(String includedFile) {
        String nameWithoutExtension = Files.getNameWithoutExtension(includedFile) + ".html";
        return new File(outputDir, nameWithoutExtension);
    }

    private String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    public int getOptions() throws MojoExecutionException {
        int result = Extensions.NONE;
        Extensions extensions = new Extensions() {
        };
        if (options != null && options.length > 0) {
            for (String option : options) {
                getLog().debug("Lookup option value for: " + option);
                try {
                    Field field = Extensions.class.getField(option);
                    result |= (int)field.get(extensions);
                } catch (NoSuchFieldException e) {
                    throw new MojoExecutionException("Cannot find the corresponding extension: " + option, e);
                } catch (IllegalAccessException e) {
                    throw new MojoExecutionException("Cannot get the value for extension: " + option, e);
                }
            }
        }
        return result;
    }
}
