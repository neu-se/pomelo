package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredInputGenerator;
import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.model.*;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class JUnit4ParameterizedWrapper implements ParameterizedTest {
    private final Class<?> testClass;
    private final String testMethodName;

    public JUnit4ParameterizedWrapper(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        } else if (!isType(testClass)) {
            throw new IllegalArgumentException(testClass + " is not a JUnitParams test");
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    @Override
    public ParameterizedRunner createParameterizedRunner(Fuzzer supplier) throws Throwable {
        return new Runner(testClass, testMethodName, supplier, this);
    }

    @Override
    public List<Object[]> getOriginalParameterGroups() throws Throwable {
        Object parameters = getParametersMethod(new TestClass(testClass)).invokeExplosively(null);
        Iterable<?> allParameters;
        if (parameters instanceof Iterable) {
            allParameters = (Iterable<?>) parameters;
        } else if (parameters instanceof Object[]) {
            allParameters = Arrays.asList((Object[]) parameters);
        } else {
            throw new IllegalStateException("Invalid Parameters method");
        }
        List<Object[]> result = new ArrayList<>();
        for (Object p : allParameters) {
            result.add((p instanceof Object[]) ? (Object[]) p : new Object[]{p});
        }
        return result;
    }

    @Override
    public List<ParameterTypeContext> getParameterTypeContexts() {
        TestClass c = new TestClass(testClass);
        List<FrameworkField> fields = c.getAnnotatedFields(Parameterized.Parameter.class);
        return fields.isEmpty() ? StructuredInputGenerator.getParameterTypeContexts(c.getOnlyConstructor()) :
                StructuredInputGenerator.getParameterTypeContexts(getInjectableFields(c));
    }

    @Override
    public String getDescriptor() {
        return testClass.getName() + "#" + testMethodName;
    }

    public static boolean isType(Class<?> clazz) {
        return clazz.isAnnotationPresent(RunWith.class) &&
                clazz.getAnnotation(RunWith.class).value().equals(Parameterized.class);
    }

    private static List<Field> getInjectableFields(TestClass clazz) {
        return clazz.getAnnotatedFields(Parameterized.Parameter.class).stream().map(FrameworkField::getField)
                    .sorted(Comparator.comparing(f -> f.getAnnotation(Parameterized.Parameter.class).value()))
                    .collect(Collectors.toList());
    }

    private static FrameworkMethod getParametersMethod(TestClass testClass) {
        return testClass.getAnnotatedMethods(Parameterized.Parameters.class).stream().filter(FrameworkMethod::isPublic)
                        .filter(FrameworkMethod::isStatic).findFirst().orElseThrow(IllegalStateException::new);
    }

    private static class Runner extends Parameterized implements ParameterizedRunner {
        private final Fuzzer supplier;
        private final FrameworkMethod method;
        private final ParameterizedTest test;

        private Runner(Class<?> clazz, String methodName, Fuzzer supplier, ParameterizedTest test) throws Throwable {
            super(clazz);
            this.test = test;
            if (supplier == null) {
                throw new NullPointerException();
            }
            this.supplier = supplier;
            this.method = JUnitTestUtil.findFrameworkMethod(getTestClass(), methodName);
        }

        @Override
        protected Statement childrenInvoker(RunNotifier notifier) {
            return createStatement(notifier, supplier);
        }

        @Override
        public void runWithGroup(RunNotifier notifier, Object[] group) throws InitializationError {
            new BlockJUnit4ClassRunnerWithParameters(
                    new TestWithParameters("[0]", getTestClass(), Arrays.asList(group))) {
                @Override
                protected void runChild(FrameworkMethod method, RunNotifier notifier) {
                    if (method.equals(Runner.this.method)) {
                        super.runChild(method, notifier);
                    }
                }
            }.run(notifier);
        }

        @Override
        public ParameterizedTest getParameterizedTest() {
            return test;
        }
    }
}
