package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.param.ParameterizedRunner;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestWrapper;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.LinkedList;

public final class FuzzForkMain {
    private FuzzForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        LinkedList<String> argumentList = new LinkedList<>(Arrays.asList(args));
        String testClassName = argumentList.poll();
        String testMethodName = argumentList.poll();
        Class<?> testClass = Class.forName(testClassName, true, FuzzForkMain.class.getClassLoader());
        ParameterizedTestWrapper wrapper = ParameterizedTestType.findAndWrap(testClass, testMethodName);
        Fuzzer fuzzer = createFuzzer(testClass, testMethodName, argumentList);
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

    private static Fuzzer createFuzzer(Class<?> testClass, String testMethodName, LinkedList<String> argumentList)
            throws Exception {
        if (argumentList.isEmpty()) {
            return new QuickcheckFuzzerAdapter(new UnguidedQuickcheckFuzzer(), testClass, testMethodName);
        } else {
            Class<?> builderClass = Class.forName(argumentList.poll(), true, FuzzForkMain.class.getClassLoader());
            FuzzerBuilder builder = (FuzzerBuilder) builderClass.newInstance();
            return builder.build(testClass, testMethodName, argumentList.toArray(new String[0]));
        }
    }
}