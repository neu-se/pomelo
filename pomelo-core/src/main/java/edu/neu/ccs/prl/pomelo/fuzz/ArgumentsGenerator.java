package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.internal.generator.ServiceLoaderGeneratorSource;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.pomelo.test.JUnitTestUtil;
import edu.neu.ccs.prl.pomelo.test.ParameterizedTestType;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentsGenerator {
    private static final long SEED = 41;
    private final List<Generator<?>> generators;

    public ArgumentsGenerator(List<Field> fields, long seed) {
        this(fields.stream().map(f -> ParameterTypeContext.forField(f).annotate(f)), seed);
    }

    public ArgumentsGenerator(Executable executable, long seed) {
        this(Arrays.stream(executable.getParameters()).map(p -> ParameterTypeContext.forParameter(p).annotate(p)),
             seed);
    }

    private ArgumentsGenerator(Stream<ParameterTypeContext> contexts, long seed) {
        SourceOfRandomness randomness = new SourceOfRandomness(new Random(seed));
        GeneratorRepository repository =
                new GeneratorRepository(randomness).register(new ServiceLoaderGeneratorSource());
        this.generators = Collections.unmodifiableList(
                contexts.map(x -> createGenerator(repository, x)).collect(Collectors.toList()));
    }

    public List<Generator<?>> getGenerators() {
        return generators;
    }

    public Object[] generate(SourceOfRandomness source, GenerationStatus status) {
        return generators.stream().map(g -> g.generate(source, status)).toArray();
    }

    private static Generator<?> createGenerator(GeneratorRepository repository, ParameterTypeContext context) {
        Generator<?> generator = repository.generatorFor(context);
        generator.provide(repository);
        generator.configure(context.annotatedType());
        if (context.topLevel()) {
            generator.configure(context.annotatedElement());
        }
        return generator;
    }

    public static ArgumentsGenerator create(Class<?> clazz, String methodName) {
        TestClass testClass = new TestClass(clazz);
        switch (ParameterizedTestType.getType(testClass.getJavaClass())) {
            case JUNIT4_PARAMETERIZED:
                List<FrameworkField> fields = testClass.getAnnotatedFields(Parameterized.Parameter.class);
                return fields.isEmpty() ? new ArgumentsGenerator(testClass.getOnlyConstructor(), SEED) :
                        new ArgumentsGenerator(getInjectableFields(testClass), SEED);
            case JUNIT_PARAMS:
                return new ArgumentsGenerator(JUnitTestUtil.findFrameworkMethod(testClass, methodName).getMethod(),
                                              SEED);
            default:
                throw new AssertionError();
        }
    }

    private static List<Field> getInjectableFields(TestClass clazz) {
        return clazz.getAnnotatedFields(Parameterized.Parameter.class).stream().map(FrameworkField::getField)
                    .sorted(Comparator.comparing(f -> f.getAnnotation(Parameterized.Parameter.class).value()))
                    .collect(Collectors.toList());
    }
}
