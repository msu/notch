package edu.montana.notch.types;

import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

import static edu.montana.notch.util.Pair.pair;

public record NotchFunctionDescription(NotchType returnType, List<Pair<NotchType, String>> parameters) {
    private static List<Pair<NotchType, String>> getMethodParameterPairs(Method method) {
        final var list = new BetterList<Pair<NotchType, String>>();
        for (Parameter parameter : method.getParameters()) {
            var ty = NotchTypeSystem.getType(parameter.getType());
            list.add(pair(ty, parameter.getName()));
        }
        return Collections.unmodifiableList(list);
    }

    public NotchFunctionDescription(Method method) {
        this(NotchTypeSystem.getType(method.getReturnType()), getMethodParameterPairs(method));
    }
}
