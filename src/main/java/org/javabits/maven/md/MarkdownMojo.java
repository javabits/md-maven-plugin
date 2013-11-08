package org.javabits.maven.md;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
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
    private static final String DEFAULT_OUTPUT_DIRECTORY = "${project.build.directory}/docs";
    private static final String DEFAULT_TARGET_FILE_NAME = "${project.build.finalName}-docs";
    private static final String TARGET_FILE_EXTENSION = "zip";
//    private static final String DEFAULT_TARGET_FILE_NAME = "${project.artifactId}-${project.version}-docs.zip";
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

    /**
     * You can provide an optional css that will be applied to the generated documentation.
     */
    @Parameter(property = "md.css")
    private File css;

    /**
     * The Markdown file extension without the dote '.'. By default is set to {@code 'md'}
     */
    @Parameter(property = "md.file.extension", defaultValue = DEFAULT_FILE_EXTENSION)
    private String fileExtension;

    /**
     * The target archive file path.
     */
    @Parameter(property = "md.target.name", defaultValue = DEFAULT_TARGET_FILE_NAME)
    private String targetName;

    /**
     * Project build directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File projectBuildDirectory;

    /**
     * Artifact final name.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    /**
     * add the artifact final name as root directory into the archive before
     * the {@link #outputDir} name its default value is {@code "docs"}.
     */
    @Parameter(property = "md.add.final.name", defaultValue = "false")
    private boolean addFinalName;

    @Component
    private MavenProject project;
    @Component
    private MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Markdown");
        PegDownProcessor pegDownProcessor = new PegDownProcessor(getOptions());
        DirectoryScanner markDownScanner = new DirectoryScanner();
        if (!sources.exists()) {
            getLog().info("Skip project no documentation found at: " + sources);
            return;
        }
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
        packageDoc();
    }

    private int getLogLevel() {
        if (getLog().isDebugEnabled()) {
            return Logger.LEVEL_DEBUG;
        }
        if (getLog().isInfoEnabled()) {
            return Logger.LEVEL_INFO;
        }
        if (getLog().isWarnEnabled()) {
            return Logger.LEVEL_WARN;
        }
        if (getLog().isErrorEnabled()) {
            return Logger.LEVEL_ERROR;
        }
        return Logger.LEVEL_DISABLED;
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


    private void packageDoc() throws MojoExecutionException {
        //prepare docs structure
        AbstractBuildDir buildDirectory = createBuildDir();

        File buildDocsDirectory = buildDirectory.docsDir();
        Path buildDocsDirPath = buildDocsDirectory.toPath();
        Path outputDirPath = outputDir.toPath();

        try {
            java.nio.file.Files.move(outputDirPath, buildDocsDirPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot move the docs dir into the build directory.", e);
        }

        //create the zip archive
        ZipArchiver archive = new ZipArchiver();
        ConsoleLogger plexusLogger = new ConsoleLogger(getLogLevel(), "md-maven-plugin:archive");
        archive.enableLogging(plexusLogger);
        archive.addDirectory(buildDirectory.dir());
        archive.setDestFile(getTargetFile());
        try {
            archive.createArchive();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot produce the documentation archive.", e);
        }
        projectHelper.attachArtifact(this.project,
                TARGET_FILE_EXTENSION,
                "docs",
                getTargetFile());
        // clean up
        try {
            java.nio.file.Files.move(buildDocsDirPath, outputDirPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot move the docs dir into the build directory.", e);
        }

        buildDirectory.clean();
    }

    private File getBuildDirectory() {
        return new File(projectBuildDirectory, "md-build");
    }

    private File getTargetFile() {
        return new File(projectBuildDirectory, targetName + '.' + TARGET_FILE_EXTENSION);
    }


    private AbstractBuildDir createBuildDir() {
        AbstractBuildDir buildDir;
        if (addFinalName) {
            buildDir = new FinalNameBuildDir();
        } else {
            buildDir = new DefaultBuildDir();
        }
        buildDir.init();
        return buildDir;
    }

    private abstract class AbstractBuildDir {
        File buildDirectory = getBuildDirectory();

        abstract void init();

        File dir() {
            return buildDirectory;
        }

        abstract void clean();

        File docsDir() {
            File parent = dir();
            return getDocsDir(parent);
        }

        File getDocsDir(File parent) {
            return new File(parent, outputDir.getName());
        }
    }


    private class DefaultBuildDir extends AbstractBuildDir {

        void init() {
            buildDirectory.mkdir();
        }

        void clean() {
            if (buildDirectory.exists()) buildDirectory.delete();
        }
    }


    private class FinalNameBuildDir extends AbstractBuildDir {
        File finalNameBuildDirectory = new File(buildDirectory, finalName);

        void init() {
            buildDirectory.mkdir();
            finalNameBuildDirectory.mkdir();
        }

        @Override
        File docsDir() {
            return getDocsDir(finalNameBuildDirectory);
        }

        void clean() {
            if (finalNameBuildDirectory.exists()) finalNameBuildDirectory.delete();
            if (buildDirectory.exists()) buildDirectory.delete();
        }
    }
}
