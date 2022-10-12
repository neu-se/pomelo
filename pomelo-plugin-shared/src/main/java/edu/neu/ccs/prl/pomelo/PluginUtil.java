package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.util.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

public final class PluginUtil {
    private PluginUtil() {
        throw new AssertionError();
    }

    public static File createEmptyDirectory(File dir) throws MojoExecutionException {
        try {
            edu.neu.ccs.prl.meringue.FileUtil.ensureEmptyDirectory(dir);
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

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
            throws MojoExecutionException {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to find method " + methodName, e);
        }
    }

    public static <T> T invokeMethod(Method method, Object obj, Class<T> returnType, Object... args)
            throws MojoExecutionException {
        try {
            return returnType.cast(method.invoke(obj, args));
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to invoke method " + method, e);
        }
    }

    public static Class<?> findClass(String className) throws MojoExecutionException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find class: " + className, e);
        }
    }

    public static void setDeclaredField(Class<?> clazz, String name, Object obj, Object value)
            throws MojoExecutionException {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to set field: " + name, e);
        }
    }
}
