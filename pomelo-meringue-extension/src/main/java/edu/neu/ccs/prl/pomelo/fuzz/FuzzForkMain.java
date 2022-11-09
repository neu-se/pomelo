package edu.neu.ccs.prl.pomelo.fuzz;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.neu.ccs.prl.meringue.SystemPropertyUtil;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;

import java.io.File;

public final class FuzzForkMain {
    public static final String SYSTEM_PROPERTIES_KEY = "pomelo.properties";

    private FuzzForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        // Usage: testClassName testMethodName outputDirectory
        String testClassName = args[0];
        String testMethodName = args[1];
        File outputDirectory = new File(args[2]);
        try {
            // Note: must set system properties before loading the test class
            SystemPropertyUtil.loadSystemProperties(SYSTEM_PROPERTIES_KEY);
            ZestGuidance guidance = new ZestGuidance(testClassName + "#" + testMethodName, null,
                                                     outputDirectory);
            run(testClassName, testMethodName, FuzzForkMain.class.getClassLoader(), guidance);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public static void run(String testClassName, String testMethodName, ClassLoader classLoader, Guidance guidance)
            throws Throwable {
        Class<?> testClass = Class.forName(testClassName, true, classLoader);
        Fuzzer fuzzer = new ZestFuzzer(testClass, testMethodName, guidance);
        ParameterizedTestType.findAndWrap(testClass, testMethodName)
                             .createParameterizedRunner(fuzzer)
                             .run();
    }
}