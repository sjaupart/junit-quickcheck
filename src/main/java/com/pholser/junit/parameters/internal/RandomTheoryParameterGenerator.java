package com.pholser.junit.parameters.internal;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javaruntype.type.Types;

import com.pholser.junit.parameters.ForAll;
import org.junit.experimental.theories.PotentialAssignment;

public class RandomTheoryParameterGenerator implements TheoryParameterGenerator {
    private final SourceOfRandomness random;
    private final Map<Class<?>, RandomValueExtractor<?>> extractors =
        new HashMap<Class<?>, RandomValueExtractor<?>>();

    public RandomTheoryParameterGenerator(SourceOfRandomness random) {
        this.random = random;
        extractors.put(int.class, new IntegerExtractor());
        extractors.put(Integer.class, new IntegerExtractor());
        extractors.put(short.class, new ShortExtractor());
        extractors.put(Short.class, new ShortExtractor());
        extractors.put(char.class, new CharExtractor());
        extractors.put(Character.class, new CharExtractor());
        extractors.put(byte.class, new ByteExtractor());
        extractors.put(Byte.class, new ByteExtractor());
        extractors.put(boolean.class, new BooleanExtractor());
        extractors.put(Boolean.class, new BooleanExtractor());
        extractors.put(long.class, new LongExtractor());
        extractors.put(Long.class, new LongExtractor());
        extractors.put(float.class, new FloatExtractor());
        extractors.put(Float.class, new FloatExtractor());
        extractors.put(double.class, new DoubleExtractor());
        extractors.put(Double.class, new DoubleExtractor());
        extractors.put(BigInteger.class, new BigIntegerExtractor());
        extractors.put(BigDecimal.class, new BigDecimalExtractor());
    }

    @Override
    public List<PotentialAssignment> generate(ForAll quantifier, Type type) {
        List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
        RandomValueExtractor<?> extractor = extractorFor(type);

        for (int i = 0, sampleSize = quantifier.sampleSize(); i < sampleSize; ++i) {
            Object nextValue = extractor.extract(random);
            assignments.add(PotentialAssignment.forValue(String.valueOf(nextValue), nextValue));
        }

        return assignments;
    }

    private RandomValueExtractor<?> extractorFor(Type type) {
        org.javaruntype.type.Type<?> typeToken = Types.forJavaLangReflectType(type);

        if (typeToken.isArray()) {
            Class<?> componentType = typeToken.getComponentClass();
            return new ArrayExtractor(componentType, extractors.get(componentType));
        } else if (List.class.equals(typeToken.getRawClass())) {
            Class<?> componentType = typeToken.getTypeParameters().get(0).getType().getRawClass();
            return new ListExtractor(extractors.get(componentType));
        }

        return extractors.get(type);
    }
}
