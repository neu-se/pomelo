package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredFuzzTarget;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredInputGenerator;
import junitparams.JUnitParamsRunner;
import junitparams.internal.TestMethod;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class JUnitParamsWrapper implements ParameterizedTest {
    private final Class<?> testClass;
    private final String testMethodName;

    public JUnitParamsWrapper(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        } else if (!ParameterizedTestType.JUNIT_PARAMS.matches(testClass, testMethodName)) {
            throw new IllegalArgumentException(testClass + " is not a JUnitParams test");
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    @Override
    public ParameterizedRunner createParameterizedRunner(Fuzzer fuzzer) throws Throwable {
        return new Runner(testClass, testMethodName, fuzzer, this);
    }

    @Override
    public List<Object[]> getOriginalParameterGroups() throws Throwable {
        return getOriginalParameterGroups(testClass, testMethodName);
    }

    @Override
    public List<ParameterTypeContext> getParameterTypeContexts() {
        return StructuredInputGenerator.getParameterTypeContexts(
                JUnitTestUtil.findFrameworkMethod(Test.class, new TestClass(testClass), testMethodName).getMethod());
    }

    @Override
    public String getDescriptor() {
        return testClass.getName() + "#" + testMethodName;
    }

    private static List<Object[]> getOriginalParameterGroups(Class<?> clazz, String testMethodName) throws Throwable {
        TestClass testClass = new TestClass(clazz);
        FrameworkMethod testMethod = JUnitTestUtil.findFrameworkMethod(Test.class, testClass, testMethodName);
        Object[] parametersSets = new TestMethod(testMethod, testClass).parametersSets();
        RecordingFrameworkMethod m = new RecordingFrameworkMethod(testMethod.getMethod());
        for (int i = 0; i < parametersSets.length; i++) {
            createInvokeParameterisedMethod(testClass, m, parametersSets[i], i).evaluate();
        }
        return m.items;
    }

    private static Statement createInvokeParameterisedMethod(TestClass testClass, FrameworkMethod testMethod,
                                                             Object parametersSet, int index)
            throws ReflectiveOperationException {
        Constructor<?> c = Class.forName("junitparams.internal.InvokeParameterisedMethod")
                                .getDeclaredConstructor(FrameworkMethod.class, Object.class, Object.class, int.class);
        c.setAccessible(true);
        return (Statement) c.newInstance(testMethod, testClass, parametersSet, index);
    }

    private static final class RecordingFrameworkMethod extends FrameworkMethod {
        List<Object[]> items = new ArrayList<>();

        public RecordingFrameworkMethod(Method method) {
            super(method);
        }

        @Override
        public Object invokeExplosively(Object target, Object... params) {
            items.add(params);
            return null;
        }
    }

    private static class Runner extends JUnitParamsRunner implements ParameterizedRunner {
        private final Fuzzer fuzzer;
        private final FrameworkMethod method;
        private final ParameterizedTest test;
        private Object[] group;
        private Throwable error;

        private Runner(Class<?> clazz, String methodName, Fuzzer fuzzer, ParameterizedTest test) throws Throwable {
            super(clazz);
            this.fuzzer = fuzzer;
            this.method = JUnitTestUtil.findFrameworkMethod(Test.class, getTestClass(), methodName);
            this.test = test;
        }

        @Override
        protected Statement childrenInvoker(RunNotifier notifier) {
            return new StructuredFuzzTarget(this, notifier).createStatement(fuzzer, (t) -> error = t);
        }

        @Override
        public void run(RunNotifier notifier) {
            super.run(notifier);
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else if (error != null) {
                // Wrap checked exceptions
                throw new RuntimeException(error);
            }
        }

        @Override
        protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    frameworkMethod.invokeExplosively(test, group);
                }
            };
        }

        @Override
        public void runWithGroup(RunNotifier notifier, Object[] group) {
            Description description = describeChild(method);
            this.group = group;
            Statement statement = new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    methodBlock(method).evaluate();
                }
            };
            runLeaf(statement, description, notifier);
        }

        @Override
        public ParameterizedTest getParameterizedTest() {
            return test;
        }
    }
}
