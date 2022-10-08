package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.ArgumentsGenerator;
import junitparams.JUnitParamsRunner;
import junitparams.internal.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class JUnitParamsWrapper implements ParameterizedTestWrapper {
    private final Class<?> testClass;
    private final String testMethodName;

    public JUnitParamsWrapper(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        } else if (!isType(testClass)) {
            throw new IllegalArgumentException(testClass + " is not a JUnitParams test");
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    @Override
    public ParameterizedRunner createParameterizedRunner(ParameterSupplier supplier) throws Throwable {
        return new Runner(testClass, testMethodName, supplier);
    }

    @Override
    public List<Object[]> getOriginalParameterGroups() throws Throwable {
        return getOriginalParameterGroups(testClass, testMethodName);
    }

    @Override
    public List<ParameterTypeContext> getParameterTypeContexts() {
        return ArgumentsGenerator.getParameterTypeContexts(
                JUnitTestUtil.findFrameworkMethod(new TestClass(testClass), testMethodName).getMethod());
    }

    public static boolean isType(Class<?> clazz) {
        return clazz.isAnnotationPresent(RunWith.class) &&
                clazz.getAnnotation(RunWith.class).value().equals(JUnitParamsRunner.class);
    }

    private static List<Object[]> getOriginalParameterGroups(Class<?> clazz, String testMethodName) throws Throwable {
        TestClass testClass = new TestClass(clazz);
        FrameworkMethod testMethod = JUnitTestUtil.findFrameworkMethod(testClass, testMethodName);
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
        private final ParameterSupplier supplier;
        private final FrameworkMethod method;
        private Object[] parameterGroup;

        private Runner(Class<?> clazz, String methodName, ParameterSupplier supplier) throws Throwable {
            super(clazz);
            this.supplier = supplier;
            this.method = JUnitTestUtil.findFrameworkMethod(getTestClass(), methodName);
        }

        @Override
        protected Statement childrenInvoker(RunNotifier notifier) {
            return createStatement(notifier, supplier);
        }

        @Override
        protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    frameworkMethod.invokeExplosively(test, parameterGroup);
                }
            };
        }

        @Override
        public void runWithParameterGroup(RunNotifier notifier, Object[] parameterGroup) {
            Description description = describeChild(method);
            this.parameterGroup = parameterGroup;
            Statement statement = new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    methodBlock(method).evaluate();
                }
            };
            runLeaf(statement, description, notifier);
        }
    }
}
