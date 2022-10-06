package edu.neu.ccs.prl.pomelo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

class TestRecordWriter {
    private final File file;

    TestRecordWriter(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("Record file does not exists or is not a normal file: " + file);
        }
        this.file = file;
    }

    public void appendAll(Set<TestRecord> records) throws IOException {
        for (TestRecord record : records) {
            append(record);
        }
    }

    public void append(TestRecord record) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            try (FileChannel channel = accessFile.getChannel()) {
                FileLock lock = channel.lock();
                try {
                    Files.write(file.toPath(), Collections.singletonList(record.toCsvRow()), StandardOpenOption.APPEND);
                } finally {
                    lock.release();
                }
            }
        }
    }
}
