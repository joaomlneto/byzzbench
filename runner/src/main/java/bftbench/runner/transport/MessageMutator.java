package bftbench.runner.transport;

import java.io.Serializable;
import java.util.function.Function;

public abstract class MessageMutator<T extends Serializable> implements Function<T, Serializable> {

    @SuppressWarnings("unchecked")
    public Class getSerializableClass() {
        return null;/*
        var clazz = ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(clazz);
        System.out.println("PART 2:");
        System.out.println(((ParameterizedType) clazz).getClass());
        return ((Class) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);*/
    }
    public abstract String name();
}
