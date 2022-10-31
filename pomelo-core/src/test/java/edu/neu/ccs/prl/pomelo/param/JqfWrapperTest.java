package edu.neu.ccs.prl.pomelo.param;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.neu.ccs.prl.pomelo.examples.JqfExample;
import edu.neu.ccs.prl.pomelo.fuzz.FixedListFuzzer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunNotifier;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class JqfWrapperTest {
    @Test
    public void runnerRespectsJunitAnnotations() throws Throwable {
        JqfExample.values.clear();
        TestGuidance guidance = new TestGuidance();
        Method m = GuidedFuzzing.class.getDeclaredMethod("setGuidance", Guidance.class);
        m.setAccessible(true);
        m.invoke(null, guidance);
        new JUnitCore().run(JqfExample.class);
        List<String> expected = new LinkedList<>(JqfExample.values);
        JqfExample.values.clear();
        ParameterizedTest test = new JqfWrapper(JqfExample.class, "test1");
        ParameterizedRunner runner = test.createParameterizedRunner(new FixedListFuzzer(guidance.parameterGroups));
        runner.run(new RunNotifier());
        Assert.assertEquals(expected, JqfExample.values);
    }

    private static final class TestGuidance implements Guidance {
        List<Object[]> parameterGroups = new LinkedList<>();
        private int inputs = 1;

        @Override
        public InputStream getInput() throws IllegalStateException, GuidanceException {
            return new InputStream() {
                @Override
                public int read() {
                    return 0;
                }
            };
        }

        @Override
        public boolean hasInput() {
            return inputs-- > 0;
        }

        @Override
        public void observeGeneratedArgs(Object[] args) {
            parameterGroups.add(args);
        }

        @Override
        public void handleResult(Result result, Throwable error) throws GuidanceException {
        }

        @Override
        public Consumer<TraceEvent> generateCallBack(Thread thread) {
            return (t) -> {
            };
        }
    }
}