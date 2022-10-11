package edu.neu.ccs.prl.pomelo.util;

import java.io.File;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {
        throw new AssertionError();
    }

    public static File ensureNew(File file) throws IOException {
        File parent = file.getParentFile();
        if (file.getParentFile() != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent);
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete existing file: " + file);
        }
        if (!file.createNewFile()) {
            throw new IOException("Failed to create file: " + file);
        }
        return file;
    }
}
