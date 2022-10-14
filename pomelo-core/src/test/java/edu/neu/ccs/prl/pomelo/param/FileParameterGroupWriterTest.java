package edu.neu.ccs.prl.pomelo.param;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class FileParameterGroupWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void writeNullParameter() throws IOException {
        String[] s = writeRead(new Object[]{null});
        Assert.assertArrayEquals(new String[]{"null"}, s);
    }

    @Test
    public void writeStringParameter() throws IOException {
        Object parameter = "hello";
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"hello"}, s);
    }

    @Test
    public void writeStringArrayParameter() throws IOException {
        Object parameter = new String[]{"hello", "world"};
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"[hello, world]"}, s);
    }

    @Test
    public void writeStringArrayNullElementParameter() throws IOException {
        Object parameter = new String[]{"hello", null};
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"[hello, null]"}, s);
    }

    @Test
    public void writeIntArrayParameter() throws IOException {
        Object parameter = new int[]{8, 99, 77};
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"[8, 99, 77]"}, s);
    }

    @Test
    public void writeIntDoubleArrayParameter() throws IOException {
        Object parameter = new int[][]{{8}, {99, 7}, {}};
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"[[8], [99, 7], []]"}, s);
    }

    @Test(timeout = 500)
    public void writeObjectArraySelfReference() throws IOException {
        Object[] parameter = new Object[]{"hello", "world"};
        parameter[1] = parameter;
        String[] s = writeRead(new Object[]{parameter});
        Assert.assertArrayEquals(new String[]{"[hello, [...]]"}, s);
    }

    @Test
    public void writeMultiArrayParameters() throws IOException {
        int[] parameter0 = new int[]{8, 99, 77};
        int[] parameter1 = new int[]{7, -2};
        String[] s = writeRead(new Object[]{parameter0, parameter1});
        Assert.assertArrayEquals(new String[]{"[8, 99, 77]", "[7, -2]"}, s);
    }

    private String[] writeRead(Object[] parameterGroup) throws IOException {
        File source = folder.newFile();
        File outputDir = folder.newFolder();
        ParameterGroupWriter writer = new FileParameterGroupWriter(outputDir);
        writer.write(source, parameterGroup);
        return Arrays.stream(Objects.requireNonNull(outputDir.listFiles()))
                     .sorted()
                     .map(FileParameterGroupWriterTest::readString)
                     .toArray(String[]::new);
    }

    private static String readString(File file) {
        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}