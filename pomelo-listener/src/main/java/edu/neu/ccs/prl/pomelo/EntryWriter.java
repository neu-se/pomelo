package edu.neu.ccs.prl.pomelo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class EntryWriter {
    private final File file;

    public EntryWriter(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("Report file does not exists or is not a normal file: " + file);
        }
        this.file = file;
    }

    public void appendAll(Iterable<String> entries) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            try (FileChannel channel = accessFile.getChannel()) {
                FileLock lock = channel.lock();
                try {
                    Files.write(file.toPath(), entries, StandardOpenOption.APPEND);
                } finally {
                    lock.release();
                }
            }
        }
    }

    public void appendAll(String... entries) throws IOException {
        appendAll(Arrays.asList(entries));
    }
}