package bigsky.notch.runtime;

import bigsky.notch.types.NotchMethod;

import java.util.ArrayList;

public record NotchBoundMethod(Object rootValue, NotchMethod method) {
    public Object invoke(ArrayList<Object> argValues) {
        return method.invoke(rootValue, argValues);
    }
}
