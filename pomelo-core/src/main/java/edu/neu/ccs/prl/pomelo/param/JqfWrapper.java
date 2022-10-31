package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredInputGenerator;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JqfWrapper implements ParameterizedTest {
    private final Class<?> testClass;
    private final String testMethodName;

    public JqfWrapper(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        } else if (!ParameterizedTestType.JQF_FUZZ.matches(testClass, testMethodName)) {
            throw new IllegalArgumentException(testClass + " is not a JQF test");
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    @Override
    public ParameterizedRunner createParameterizedRunner(Fuzzer fuzzer) throws Throwable {
        return new Runner(testClass, testMethodName, fuzzer, this);
    }

    @Override
    public List<Object[]> getOriginalParameterGroups() {
        return Collections.emptyList();
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

    private static class Runner extends BlockJUnit4ClassRunner implements ParameterizedRunner {
        private final Fuzzer fuzzer;
        private final FrameworkMethod method;
        private final ParameterizedTest test;
        private final List<Class<?>> expectedExceptions;
        private Object[] group;

        private Runner(Class<?> clazz, String methodName, Fuzzer fuzzer, ParameterizedTest test) throws Throwable {
            super(clazz);
            this.test = test;
            if (fuzzer == null) {
                throw new NullPointerException();
            }
            this.fuzzer = fuzzer;
            @SuppressWarnings("unchecked") Class<? extends Annotation> fuzz =
                    (Class<? extends Annotation>) Class.forName("edu.berkeley.cs.jqf.fuzz.Fuzz");
            this.method = JUnitTestUtil.findFrameworkMethod(fuzz, getTestClass(), methodName);
            this.expectedExceptions = Arrays.asList(method.getMethod().getExceptionTypes());
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return Collections.singletonList(method);
        }

        @Override
        protected Statement methodInvoker(FrameworkMethod method, Object test) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        method.invokeExplosively(test, group);
                    } catch (Throwable e) {
                        if (!isExceptionExpected(e.getClass())) {
                            throw e;
                        }
                    }
                }
            };
        }

        @Override
        protected Statement childrenInvoker(RunNotifier notifier) {
            return createStatement(notifier, fuzzer);
        }

        @Override
        public void runWithGroup(RunNotifier notifier, Object[] group) {
            this.group = group;
            Statement statement = new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    methodBlock(method).evaluate();
                }
            };
            runLeaf(statement, describeChild(method), notifier);
        }

        @Override
        public ParameterizedTest getParameterizedTest() {
            return test;
        }

        private boolean isExceptionExpected(Class<? extends Throwable> e) {
            for (Class<?> expectedException : expectedExceptions) {
                if (expectedException.isAssignableFrom(e)) {
                    return true;
                }
            }
            return false;
        }
    }
}
