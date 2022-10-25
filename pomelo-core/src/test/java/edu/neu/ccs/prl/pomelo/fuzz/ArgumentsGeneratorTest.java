package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.Generator;
import edu.neu.ccs.prl.pomelo.examples.*;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.scan.TestMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentsGeneratorTest {
    @Test
    public void generatorsCorrectConstructorInjection() {
        List<Generator<?>> generators = new ArgumentsGenerator(
                ParameterizedTestType.findAndWrap(new TestMethod(ParameterizedConstructorExample.class, "test1"))
                                     .getParameterTypeContexts()).getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(int.class));
        Assert.assertTrue(generators.get(1).types().contains(long.class));
    }

    @Test
    public void generatorsCorrectFieldInjection() {
        List<Generator<?>> generators = new ArgumentsGenerator(
                ParameterizedTestType.findAndWrap(new TestMethod(ParameterizedFieldExample.class, "test1"))
                                     .getParameterTypeContexts()).getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(String.class));
        Assert.assertTrue(generators.get(1).types().contains(int.class));
    }

    @Test
    public void generatorsCorrectFieldInjectionFrom() {
        List<Generator<?>> generators = new ArgumentsGenerator(
                ParameterizedTestType.findAndWrap(new TestMethod(ParameterizedFieldFromExample.class, "test1"))
                                     .getParameterTypeContexts()).getGenerators();
        Assert.assertEquals(Collections.singletonList(MyStringGenerator.class),
                            generators.stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @Test
    public void generatorsCorrectMethodInjection() {
        List<Generator<?>> generators = new ArgumentsGenerator(
                ParameterizedTestType.findAndWrap(new TestMethod(JUnitParamsExample.class, "test1"))
                                     .getParameterTypeContexts()).getGenerators();
        Assert.assertEquals(2, generators.size());
        Assert.assertTrue(generators.get(0).types().contains(int.class));
        Assert.assertTrue(generators.get(1).types().contains(boolean.class));
    }

    @Test
    public void generatorsCorrectMethodInjectionFrom() {
        List<Generator<?>> generators = new ArgumentsGenerator(
                ParameterizedTestType.findAndWrap(new TestMethod(JUnitParamsExample.class, "testWithGenerator"))
                                     .getParameterTypeContexts()).getGenerators();
        Assert.assertEquals(Collections.singletonList(MyStringGenerator.class),
                            generators.stream().map(Object::getClass).collect(Collectors.toList()));
    }
}