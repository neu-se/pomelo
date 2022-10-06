package edu.neu.ccs.prl.pomelo.scan;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

public final class ScanForkMain {
    private ScanForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        String testClassName = args[0];
        String testMethodName = args[1];
        ClassLoader testClassLoader = ScanForkMain.class.getClassLoader();
        Class<?> testClass = java.lang.Class.forName(testClassName, true, testClassLoader);
        JUnitCore core = new JUnitCore();
        core.addListener(new PomeloJUnitListener());
        core.run(Request.method(testClass, testMethodName));
        // TODO check whether generators with required types are available
    }
}
