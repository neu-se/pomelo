package edu.neu.ccs.prl.pomelo.scan;

import edu.neu.ccs.prl.pomelo.test.*;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.junit.runner.notification.RunNotifier;

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
        ParameterizedTestWrapper wrapper = ParameterizedTestType.getType(testClass)
                                                                .wrap(testClass, testMethodName);
        ParameterSupplier supplier = new ListParameterSupplier(wrapper.getOriginalParameterGroups());
        ParameterizedRunner runner = wrapper.createParameterizedRunner(supplier);
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        runner.run(notifier);
        // TODO check whether generators with required types are available
    }
}
