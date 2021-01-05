package io.fabric8.quikstarts.maven;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo( name = "updateDoc")
public class UpdateDocMojo  extends AbstractMojo {
    private static final String README_FILE_NAME = "README.adoc";
    private static final String KARAF_QUICKSTART_TEMPLATE = "karaf-quickstart-template.adoc";
    private static final String SPRINGBOOT_QUICKSTART_TEMPLATE = "springboot-quickstart-template.adoc";
    private static final String KARAF = "karaf";
    public static final String FUSE_KARAF_OPENSHIFT_IMAGE_NAME = "fuse-karaf-openshift";
    public static final String FUSE_JAVA_OPENSHIFT_IMAGE_NAME = "fuse-java-openshift";
    public static final String REGISTRY_REDHAT_IO_FUSE = "registry.redhat.io/fuse";

    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.sourceEncoding}", required = true, property = "quickstart.encoding")
    String encoding;

    public void execute() throws MojoExecutionException
    {
        final Charset charset = Charset.forName(encoding);
        final Path basePath = project.getBasedir().toPath();
        final Path docPagePath = basePath.resolve(README_FILE_NAME);

        final Map<String, Object> model = new HashMap<>();
        model.put("project", project);

        boolean childPagesExist;
        try {
            childPagesExist = loadPages(model, basePath, charset);
        } catch (IOException e) {
            throw new RuntimeException("Could not load doc pages from " + docPagePath, e);
        }

        String pageText;
        if(childPagesExist) {
            final Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setTemplateLoader(new ClassTemplateLoader(UpdateDocMojo.class, "/doc-templates"));
            cfg.setDefaultEncoding(encoding);
            cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
            cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);

            String template = project.getName().toLowerCase().contains(KARAF) ? KARAF_QUICKSTART_TEMPLATE : SPRINGBOOT_QUICKSTART_TEMPLATE;

            pageText  = evalTemplate(cfg, template, model, new StringWriter()).toString();
            //remove empty lines
            pageText = pageText.replaceAll("\\n{3,}", "\n\n");
        } else {
            //if there are no child pages, then no generation is necessary, read README.adoc instead
            try {
                Stream<String> lines = java.nio.file.Files.lines(basePath.resolve("README.adoc"));
                pageText = lines.collect(Collectors.joining("\n"));
                lines.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + basePath.resolve("README.adoc"), e);
            }
        }

        try {
            //align versions
            pageText = alignVersions(pageText);
        } catch (IOException e) {
            throw new RuntimeException("Could not align versions", e);
        }
        try (FileOutputStream outputStream = new FileOutputStream(docPagePath.toFile().getAbsolutePath())) {
            outputStream.write(pageText.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException("Could not write to " + docPagePath, e);
        }
    }

    private <T extends Writer> T evalTemplate(Configuration cfg, String templateUri, Map<String, Object> model, T out) {
        try {
            final Template template = cfg.getTemplate(templateUri);
            try {
                template.process(model, out);
            } catch (TemplateException e) {
                throw new RuntimeException("Could not process template " + templateUri + ":\n\n" + out.toString(), e);
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Could not evaluate template " + templateUri, e);
        }
    }

    private String loadSection(Path basePath, String fileName, Charset charset, String default_) {
        Path p = basePath.resolve("src/main/doc/" + fileName);
        if (java.nio.file.Files.exists(p)) {
            try {
                return new String(java.nio.file.Files.readAllBytes(p), charset);
            } catch (IOException e) {
                throw new RuntimeException("Could not read " + p, e);
            }
        } else {
            return default_;
        }
    }

    private boolean loadPages(Map<String, Object> model, Path basePath, Charset charset) throws IOException {
        File docFile = basePath.resolve("src/main/doc/").toFile();
        if(!docFile.exists()) {
            return false;
        }
        final File[] files = docFile.listFiles();
        for(int i = 0; i < files.length; i++) {
            String name = files[i].getName().replace(".adoc", "");
            //convert from "ab-cd-ef" to "abCdEf"
            StringBuffer sb = new StringBuffer();
            for(int j =0; j < name.length(); j++) {
                if(name.charAt(j) == '-' && j + 2 < name.length()) {
                    sb.append(name.substring(j+1, j+2).toUpperCase());
                    j++;
                } else {
                    sb.append(name.charAt(j));
                }
            }
            model.put(sb.toString(), loadSection(basePath, name + ".adoc", charset, null));
        }
        return true;
    }

    private String alignVersions(String text) throws IOException {
        String pomVersion = project.getProperties().getProperty("fuse.bom.version");

        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\..*");
        Matcher matcher = pattern.matcher(pomVersion);


        if (matcher.matches()) {
            int firstVersion = Integer.parseInt(matcher.group(1));
            int secondVersion = Integer.parseInt(matcher.group(2));

            //version 7.x has tag of image 1:x
            String imageName = project.getName().toLowerCase().contains("karaf") ? FUSE_KARAF_OPENSHIFT_IMAGE_NAME : FUSE_JAVA_OPENSHIFT_IMAGE_NAME;
            String fuseImage =  imageName + ":" + (firstVersion - 6) + "." + secondVersion;
            String fuseImageRepository = REGISTRY_REDHAT_IO_FUSE + firstVersion + '/' + fuseImage;

            getLog().info("Replacing version for import-image or documentation to version " + firstVersion + '.' + secondVersion + ". Image: " + fuseImage);

            StringBuffer sb = new StringBuffer();
            StringReader reader = new StringReader(text);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while((line=br.readLine())!=null)
            {
                sb.append(line.replaceAll("oc import-image .*", "oc import-image " + fuseImage + " --from=" + fuseImageRepository + " --confirm")
                    .replaceAll("red_hat_fuse/\\d+.\\d+/html", "red_hat_fuse/" + firstVersion + '.' + secondVersion + "/html")
                    .replaceAll("-Dfabric8.generator.from=MY_PROJECT_NAME/.*","-Dfabric8.generator.from=MY_PROJECT_NAME/" + fuseImage))
                .append(System.getProperty("line.separator"));
            }

            return sb.toString();
        }
        return text;
    }

}