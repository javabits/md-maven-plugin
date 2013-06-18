package org.javabits.maven.md;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.google.common.io.Files.createParentDirs;
import static com.google.common.io.Files.getFileExtension;
import static org.javabits.maven.md.Resources.copyToDir;

/**
 * This plugin is responsible to generate Html files from Markdown input files.
 * The output file is the result of the merge of a template http file and the
 * output of the parsing of the input file. You may want to change the html template file.
 * Or you may simply change the css used in the original template.
 * TODO provide a way to change the template or the css.
 *
 * @author Romain Gilles
 */
@Mojo(name = "generate")
public class MarkdownMojo extends AbstractMojo {

    private static final String DEFAULT_FILE_EXTENSION = "md";
    private static final String FILE_FILTER = "**/*";
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

    @Parameter(property = "md.css")
    private File css;

    @Parameter(property = "md.file.extension", defaultValue = DEFAULT_FILE_EXTENSION)
    private String fileExtension;

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
        final String templateFile = getTemplate();

        Path targetCss = prepareCss().getAbsoluteFile().toPath();
        for (String includedFile : includedFiles) {
            try {
                if (fileExtension.equals(getFileExtension(includedFile))) {
                    Charset charset = Charset.forName(this.charset);
                    String file = Files.toString(getInputFile(includedFile), charset);
                    getLog().debug("Transform file: " + includedFile);
                    String html = pegDownProcessor.markdownToHtml(file);
                    File destinationFile = getDestinationFileForTransformation(includedFile);
                    createParentDirs(destinationFile);
                    String title = Markdowns.getTitle(file);
                    getLog().debug("Document title: " + title);

                    String templateFileWithTile = templateFile.replace("${title}", title).replace("${css}", getCssRelativePath(targetCss, destinationFile));
                    html = templateFileWithTile.replace("${content}", html);
                    Files.write(html, destinationFile, charset);
                } else {
                    //just copy static resource.
                    File destinationFileForCopy = getDestinationFileForCopy(includedFile);
                    createParentDirs(destinationFileForCopy);
                    Files.copy(getInputFile(includedFile), destinationFileForCopy);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("IO exception when generated from: " + includedFile, e);
            }
        }
    }

    private String getCssRelativePath(Path targetCss, File destinationFile) {
        return destinationFile.getAbsoluteFile().toPath().getParent().relativize(targetCss).toString().replace("\\", "/");
    }

    private File prepareCss() throws MojoExecutionException {
        if (outputDir.mkdirs()) {
            getLog().debug("Create the root directory: " + outputDir);
        }
        File targetCss = null;
        try {
            if (css != null) {
                targetCss = new File(outputDir, css.toPath().getFileName().toString());

                Files.copy(css, targetCss);
            } else {
                targetCss = copyToDir("/base.css", outputDir);
            }
            return targetCss;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy css file: " + targetCss, e);
        }
    }

    private String getTemplate() throws MojoExecutionException {
        String templateFile;
        try {
            templateFile = Resources.toString("/file-template.html");
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read the template-file", e);
        }
        return templateFile;
    }

    private File getInputFile(String includedFile) {
        return new File(sources, includedFile);
    }

    private File getDestinationFile(String includedFile, String extension) {
        String nameWithoutExtension = getDestinationFilePath(includedFile, extension);
        return new File(outputDir, nameWithoutExtension);
    }

    private File getDestinationFileForTransformation(String includedFile) {
        return getDestinationFile(includedFile, "html");
    }

    private File getDestinationFileForCopy(String includedFile) {
        return getDestinationFile(includedFile, getFileExtension(includedFile));
    }


    static String getDestinationFilePath(String includedFile) {
        return getDestinationFilePath(includedFile, "html");
    }

    static String getDestinationFilePath(String includedFile, String extension) {
        Path path = Paths.get(includedFile);
        String fileName = Files.getNameWithoutExtension(includedFile) + '.' + extension;
        Path parent = path.getParent();
        if (parent != null) {
            return parent.resolve(fileName).toString();
        }
        return fileName;
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
                    result |= (int) field.get(extensions);
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
