package edu.neu.ccs.prl.pomelo.examples;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

@RunWith(JQF.class)
@SuppressWarnings("all")
public class JqfExample {
    public static final List<String> values = new LinkedList<>();

    @Before
    public void before() {
        values.add("b");
    }

    @After
    public void after() {
        values.add("a");
    }

    @Fuzz
    public void test1(int param1, boolean param2) {
        values.add("t");
        if (param1 == 42 && param2) {
            throw new IllegalStateException();
        }
    }

    @BeforeClass
    public static void beforeClass() {
        values.add("bc");
    }

    @AfterClass
    public static void afterClass() {
        values.add("ac");
    }
}