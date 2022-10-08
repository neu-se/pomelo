package edu.neu.ccs.prl.pomelo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

@RunWith(Parameterized.class)
@SuppressWarnings("all")
public class ComplexTypeTest {
    public final Map<String, int[]> map;
    public final List<LinkedList<Map<Object, String>>> list;
    public final Record record;

    public ComplexTypeTest(Map<String, int[]> map, List<LinkedList<Map<Object, String>>> list, Record record) {
        this.map = map;
        this.list = list;
        this.record = record;
    }

    @Test
    public void test1() {
    }

    @Parameterized.Parameters
    public static Collection<Object[]> arguments() {
        return Arrays.asList(new Object[][]{{null, null, null},});
    }

    private static final class Record {
    }
}
