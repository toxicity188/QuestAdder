package kor.toxicity.questadder.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public enum PrimitiveType {
    INTEGER(Integer.TYPE,Integer.class),
    BOOLEAN(Boolean.TYPE,Boolean.class),
    CHARACTER(Character.TYPE,Character.class),
    DOUBLE(Double.TYPE,Double.class),
    FLOAT(Float.TYPE,Float.class),
    SHORT(Short.TYPE,Short.class),
    BYTE(Byte.TYPE,Byte.class),
    LONG(Long.TYPE,Long.class)
    ;
    private final Class<?> primitive;
    private final Class<?> reference;
    PrimitiveType(Class<?> primitive, Class<?> reference) {
        this.primitive = primitive;
        this.reference = reference;
    }

    public @NotNull Class<?> getPrimitive() {
        return primitive;
    }

    public @NotNull Class<?> getReference() {
        return reference;
    }

    public static @NotNull Class<?> convertToReferenceClass(@NotNull Class<?> primitive) {
        var clazz = Arrays.stream(values()).filter(e -> e.primitive == primitive).findFirst().map(e -> e.reference).orElse(null);
        return (clazz != null) ? clazz : Objects.requireNonNull(primitive);
    }
}
