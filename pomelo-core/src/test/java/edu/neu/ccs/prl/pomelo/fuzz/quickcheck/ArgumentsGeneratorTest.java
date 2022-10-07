package edu.neu.ccs.prl.pomelo.fuzz.quickcheck;

import com.pholser.junit.quickcheck.generator.Generator;
import edu.neu.ccs.prl.pomelo.fuzz.examples.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentsGeneratorTest {
    @Test
    public void generatorsCorrectConstructorInjection() {
        List<Generator<?>> generators =
                ArgumentsGenerator.create(ParameterizedConstructorExample.class, "test1").getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(int.class));
        Assert.assertTrue(generators.get(1).types().contains(long.class));
    }

    @Test
    public void generatorsCorrectFieldInjection() {
        List<Generator<?>> generators =
                ArgumentsGenerator.create(ParameterizedFieldExample.class, "test1").getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(String.class));
        Assert.assertTrue(generators.get(1).types().contains(int.class));
    }

    @Test
    public void generatorsCorrectFieldInjectionFrom() {
        List<Generator<?>> generators =
                ArgumentsGenerator.create(ParameterizedFieldFromExample.class, "test1").getGenerators();
        Assert.assertEquals(Collections.singletonList(MyStringGenerator.class),
                            generators.stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @Test
    public void generatorsCorrectMethodInjection() {
        List<Generator<?>> generators = ArgumentsGenerator.create(JUnitParamsExample.class, "test1").getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(int.class));
        Assert.assertTrue(generators.get(1).types().contains(boolean.class));
    }

    @Test
    public void generatorsCorrectMethodInjectionFrom() {
        List<Generator<?>> generators =
                ArgumentsGenerator.create(JUnitParamsExample.class, "testWithGenerator").getGenerators();
        Assert.assertEquals(Collections.singletonList(MyStringGenerator.class),
                            generators.stream().map(Object::getClass).collect(Collectors.toList()));
    }
}