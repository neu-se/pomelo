package edu.neu.ccs.prl.pomelo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtil {
    private FileUtil() {
        throw new AssertionError();
    }

    /**
     * Tries to the specified directory and all of its contents/
     *
     * @param dir the directory to be deleted
     * @throws NullPointerException if the specified directory is {@code null}
     * @throws IOException          if {@code dir} is not a directory or an I/O error occurs
     */
    public static void deleteDirectory(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException();
        }
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    /**
     * Ensures that the specified directory exists and is empty.
     *
     * @param dir the directory to be created or emptied
     * @throws IOException if the specified directory could not be created or emptied
     */
    public static void ensureEmptyDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            deleteDirectory(dir);
        } else {
            delete(dir);
        }
        ensureDirectory(dir);
    }

    /**
     * Tries to non-recursively delete all existing files in the specified directory.
     *
     * @param dir the directory to be cleared
     * @throws NullPointerException if the specified directory is {@code null}
     * @throws IOException          if {@code dir} is not a directory or an I/O error occurs
     */
    public static void clearDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Unable to clear: " + dir);
        }
        for (File file : files) {
            if (!file.delete()) {
                throw new IOException("Unable to delete existing file: " + file);
            }
        }
    }

    /**
     * Creates the specified directory if it does not already exist.
     *
     * @param dir the directory to create
     * @throws NullPointerException if {@code dir} is {@code null}
     * @throws IOException          if the specified directory did not already exist and was not successfully created
     * @throws SecurityException    a security manager exists and denies access to {@code dir}
     */
    public static void ensureDirectory(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
    }

    public static void delete(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete existing file: " + file);
        }
    }

    public static File ensureNew(File file) throws IOException {
        FileUtil.ensureDirectory(file.getParentFile());
        FileUtil.delete(file);
        if (!file.createNewFile()) {
            throw new IOException("Failed" +
                                          " to create file: " + file);
        }
        return file;
    }
}
