package edu.neu.ccs.prl.pomelo.scan;

import edu.neu.ccs.prl.pomelo.fuzz.FixedFuzzer;
import edu.neu.ccs.prl.pomelo.fuzz.FuzzForkMain;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;

import java.io.File;

public final class ScanForkMain {
    private ScanForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        String testClassName = args[0];
        String testMethodName = args[1];
        PomeloJUnitListener listener = new PomeloJUnitListener(new File(args[2]));
        SystemPropertyUtil.load(new File(args[3]));
        Class<?> testClass = Class.forName(testClassName, true, ScanForkMain.class.getClassLoader());
        FuzzForkMain.run(testClass, testMethodName, FixedFuzzer.withOriginalArguments(testClass, testMethodName),
                         listener);
        // TODO check whether generators with required types are available
    }
}
