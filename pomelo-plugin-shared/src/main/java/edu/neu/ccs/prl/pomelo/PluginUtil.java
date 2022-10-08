package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.util.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class PluginUtil {
    private PluginUtil() {
        throw new AssertionError();
    }

    public static File getClassPathElement(Class<?> clazz) {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    public static void deleteDirectory(File dir) throws MojoExecutionException {
        try {
            FileUtil.deleteDirectory(dir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to delete directory: " + dir, e);
        }
    }

    public static File createEmptyDirectory(File dir) throws MojoExecutionException {
        try {
            FileUtil.ensureEmptyDirectory(dir);
            return dir;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create empty directory: " + dir, e);
        }
    }

    public static File ensureNew(File file) throws MojoExecutionException {
        try {
            return FileUtil.ensureNew(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create file: " + file, e);
        }
    }

    public static List<String> readLines(File file) throws MojoExecutionException {
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read lines from : " + file, e);
        }
    }
}
