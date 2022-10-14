package edu.neu.ccs.prl.pomelo.param;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class FileParameterGroupWriter implements ParameterGroupWriter {
    private final File outputDirectory;

    public FileParameterGroupWriter(File outputDirectory) throws IOException {
        if (!outputDirectory.isDirectory()) {
            throw new IOException();
        }
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void write(File source, Object[] parameterGroup) throws IOException {
        for (int i = 0; i < parameterGroup.length; i++) {
            File file = new File(outputDirectory, String.format("%s.%d", source.getName(), i));
            try (PrintWriter out = new PrintWriter(file)) {
                out.print(format(parameterGroup[i]));
            }
        }
    }

    private String format(Object object) {
        if (object == null) {
            return "null";
        } else if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object);
        } else if (object instanceof short[]) {
            return Arrays.toString((short[]) object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[]) object);
        } else if (object instanceof long[]) {
            return Arrays.toString((long[]) object);
        } else if (object instanceof char[]) {
            return Arrays.toString((char[]) object);
        } else if (object instanceof float[]) {
            return Arrays.toString((float[]) object);
        } else if (object instanceof double[]) {
            return Arrays.toString((double[]) object);
        } else if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        } else if (object instanceof Object[]) {
            return Arrays.deepToString((Object[]) object);
        } else {
            return object.toString();
        }
    }
}
