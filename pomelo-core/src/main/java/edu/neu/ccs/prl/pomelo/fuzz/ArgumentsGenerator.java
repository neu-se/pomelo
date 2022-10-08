package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.internal.generator.ServiceLoaderGeneratorSource;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ArgumentsGenerator {
    private static final GeneratorRepository BASE_REPOSITORY =
            new GeneratorRepository(new SourceOfRandomness(new Random()))
                    .register(new ServiceLoaderGeneratorSource());
    private static final long DEFAULT_SEED = 41;
    private final GeneratorRepository repository = createRepository();
    private final List<Generator<?>> generators;

    public ArgumentsGenerator(List<ParameterTypeContext> contexts) {
        this.generators = Collections.unmodifiableList(
                contexts.stream().map(x -> createGenerator(repository, x)).collect(Collectors.toList()));
    }

    public List<Generator<?>> getGenerators() {
        return generators;
    }

    public Object[] generate(SourceOfRandomness source, GenerationStatus status) {
        // Set repository.withRandom(source);
        // Regenerate generators
        return generators.stream().map(g -> g.generate(source, status)).toArray();
    }

    private static GeneratorRepository createRepository() {
        return (GeneratorRepository)
                BASE_REPOSITORY.withRandom(new SourceOfRandomness(new Random(DEFAULT_SEED)));
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

    public static List<ParameterTypeContext> getParameterTypeContexts(Collection<Field> fields) {
        return fields.stream()
                     .map(f -> ParameterTypeContext.forField(f).annotate(f))
                     .collect(Collectors.toList());
    }

    public static List<ParameterTypeContext> getParameterTypeContexts(Executable executable) {
        return Arrays.stream(executable.getParameters())
                     .map(p -> ParameterTypeContext.forParameter(p).annotate(p))
                     .collect(Collectors.toList());
    }

    public static boolean generatorAvailable(ParameterTypeContext context) {
        try {
            createGenerator(BASE_REPOSITORY, context);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
