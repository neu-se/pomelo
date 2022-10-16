package edu.neu.ccs.prl.pomelo.fuzz;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.neu.ccs.prl.pomelo.param.ParameterizedRunner;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestWrapper;
import edu.neu.ccs.prl.pomelo.scan.TestMethod;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.io.IOException;

public final class FuzzForkMain {
    private FuzzForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        // Usage: testClassName testMethodName outputDirectory
        String testClassName = args[0];
        String testMethodName = args[1];
        File outputDirectory = new File(args[2]);
        try {
            run(testClassName, testMethodName,
                new ZestGuidance(testClassName + "#" + testMethodName, null, outputDirectory),
                FuzzForkMain.class.getClassLoader());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public static void run(String testClassName, String testMethodName, Guidance guidance, ClassLoader classLoader)
            throws Throwable {
        // Note: must set system properties before loading the test class
        loadSystemProperties();
        Class<?> testClass = Class.forName(testClassName, true, classLoader);
        ParameterizedTestWrapper wrapper = ParameterizedTestType.wrap(new TestMethod(testClass, testMethodName));
        FuzzerAdapter fuzzer =
                new FuzzerAdapter(new ZestFuzzer(testClass, testMethodName, guidance), testClass, testMethodName);
        ParameterizedRunner runner = wrapper.createParameterizedRunner(fuzzer);
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(fuzzer.getListener());
        try {
            fuzzer.setUp();
            runner.run(notifier);
        } finally {
            fuzzer.tearDown();
        }
    }

    private static void loadSystemProperties() throws IOException {
        String path = System.getProperty("pomelo.properties");
        if (path != null) {
            SystemPropertyUtil.load(new File(path));
        }
    }
}