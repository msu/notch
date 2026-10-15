package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeCoercer;
import edu.montana.notch.util.Pair;

import java.util.*;

import static edu.montana.notch.util.Pair.pair;

public class SubtypeCoercer implements NotchTypeCoercer {
    private HashMap<Pair<NotchType, NotchType>, Integer> cachedDistance = new HashMap<>();

    private int _getCoercionCost(NotchType source, NotchType target) {
        var sourceClass = source.getBackingClass();

        var targetClass = target.getBackingClass();
        if (targetClass == null) return Integer.MAX_VALUE;

        if (sourceClass == null)  {
            if (target.getBackingClass() == Object.class) return 1;
            return Integer.MAX_VALUE;
        }

        if (sourceClass == targetClass) return 0;
        if (!targetClass.isAssignableFrom(sourceClass)) return Integer.MAX_VALUE;

        Queue<Pair<Class, Integer>> queue = new ArrayDeque<>();
        final int baseCost = 2;
        var seen = new HashSet<Class>();
        enqueue(queue, seen, sourceClass.getSuperclass(), baseCost);
        for (var iface : sourceClass.getInterfaces()) {
            enqueue(queue, seen, iface, baseCost + 1);
        }

        while (!queue.isEmpty()) {
            var item = queue.remove();
            var clazz = item.first();
            var cost = item.second();
            if (targetClass == clazz) return item.second();

            enqueue(queue, seen, clazz.getSuperclass(), cost + 1);
            for (var iface : clazz.getInterfaces()) {
                enqueue(queue, seen, iface, cost + 1);
            }
        }

        return Integer.MAX_VALUE;
    }

    void enqueue(Queue<Pair<Class, Integer>> queue, HashSet<Class> seen, Class c, int cost) {
        if (c == null || seen.contains(c)) return;
        seen.add(c);
        queue.add(pair(c, cost));
    }

    @Override
    public int getCoercionCost(NotchType source, NotchType target) {
        var typePair = pair(source, target);
        int distance;
        if (cachedDistance.containsKey(typePair)) {
            distance = cachedDistance.get(typePair);
        } else {
            int cost = _getCoercionCost(source, target);
            cachedDistance.put(typePair, cost);
            distance = cost;
        }
        return distance;
    }

    @Override
    public Object coerce(NotchRuntime runtime, Object value, Span span, NotchType source, NotchType target) {
        return value;
    }
}
