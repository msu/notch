package edu.montana.notch.types.coercions;

import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.MAX_VALUE;

/**
 * Directed graph of value coercions. Nodes are {@link NotchType}s and edges are
 * {@link Coercion}s weighted by {@link Coercion#rank()}; {@link #find} returns
 * the cheapest walk between two types.
 * <p>
 * The search is Dijkstra rather than A* on purpose — an arbitrary user-extended
 * type graph gives no admissible estimate of the remaining distance, so any
 * heuristic worth trusting is the zero heuristic, and A* with a zero heuristic
 * <em>is</em> Dijkstra. The pieces are laid out so a heuristic could be dropped
 * into the queue comparator later if one ever exists.
 * <p>
 * <b>Widening only applies to the source.</b> Before the walk starts, the
 * source type is expanded across its superclasses and interfaces, so an edge
 * declared on {@code Number} is reachable from an {@code Integer} value. Types
 * reached <em>through</em> a coercion are not expanded: once the walk leaves
 * the source's hierarchy only exact outgoing edges apply. This keeps a chain
 * from laundering an unrelated conversion through some shared supertype.
 */
public class CoercionGraph {

    /** Hop cost from a type to an interface it directly implements. */
    public static final int INTERFACE_COST = 1;
    /** Hop cost from a type to its direct superclass. */
    public static final int SUPERCLASS_COST = 2;

    private final Map<NotchType, List<Coercion>> edges = new ConcurrentHashMap<>();
    private final Map<Query, Optional<CoercionPath>> cache = new ConcurrentHashMap<>();

    public CoercionGraph() {
    }

    /** Copies {@code parent}'s edges so a child runtime can extend them independently. */
    public CoercionGraph(CoercionGraph parent) {
        for (var entry : parent.edges.entrySet()) {
            edges.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    public void add(Coercion coercion) {
        edges.computeIfAbsent(coercion.from(), key -> new ArrayList<>()).add(coercion);
        cache.clear();
    }

    public void addAll(Collection<? extends Coercion> coercions) {
        for (Coercion coercion : coercions) {
            add(coercion);
        }
    }

    public List<Coercion> edgesFrom(NotchType type) {
        return edges.getOrDefault(type, List.of());
    }

    public CoercionPath find(NotchType from, Class<?> to) {
        return to == null ? null : find(from, TypeRegistry.getType(to));
    }

    /**
     * The cheapest coercion chain from {@code from} to {@code to}, or null when
     * the target is unreachable.
     */
    public CoercionPath find(NotchType from, NotchType to) {
        if (from == null || to == null) {
            return null;
        }
        return cache
                .computeIfAbsent(new Query(from, to), query -> Optional.ofNullable(search(query.from(), query.to())))
                .orElse(null);
    }

    private CoercionPath search(NotchType from, NotchType to) {
        var open = new PriorityQueue<Step>(Comparator.comparingInt(Step::cost));
        var best = new HashMap<NotchType, Integer>();

        // Seed with the source and everything it already is — this is the only
        // point at which supertypes are consulted.
        for (var seed : widen(from).entrySet()) {
            best.put(seed.getKey(), seed.getValue());
            open.add(new Step(seed.getKey(), seed.getValue(), null, null));
        }

        while (!open.isEmpty()) {
            Step current = open.poll();
            if (current.cost() > best.getOrDefault(current.type(), MAX_VALUE)) {
                continue; // stale entry, already settled cheaper
            }
            if (current.type().equals(to)) {
                return pathTo(from, to, current);
            }
            for (Coercion edge : edgesFrom(current.type())) {
                int cost = current.cost() + edge.rank();
                if (cost >= best.getOrDefault(edge.to(), MAX_VALUE)) {
                    continue;
                }
                best.put(edge.to(), cost);
                open.add(new Step(edge.to(), cost, edge, current));
            }
        }
        return null;
    }

    /**
     * Distance from {@code from} to each of its supertypes, interfaces one hop
     * and superclasses two — the weighting the old hand-rolled
     * {@code distanceTo} used, so overload resolution keeps preferring an
     * interface match over a superclass match.
     */
    private static Map<NotchType, Integer> widen(NotchType from) {
        var distances = new LinkedHashMap<NotchType, Integer>();
        var queue = new ArrayDeque<NotchType>();
        distances.put(from, 0);
        queue.add(from);
        while (!queue.isEmpty()) {
            NotchType type = queue.poll();
            int distance = distances.get(type);
            for (NotchType supertype : type.getDirectSupertypes()) {
                if (supertype == null) {
                    continue;
                }
                int cost = distance + (supertype.getBackingClass().isInterface() ? INTERFACE_COST : SUPERCLASS_COST);
                Integer known = distances.get(supertype);
                if (known != null && known <= cost) {
                    continue;
                }
                distances.put(supertype, cost);
                queue.add(supertype);
            }
        }
        return distances;
    }

    private static CoercionPath pathTo(NotchType from, NotchType to, Step end) {
        var steps = new LinkedList<Coercion>();
        for (Step step = end; step != null && step.edge() != null; step = step.previous()) {
            steps.addFirst(step.edge());
        }
        return new CoercionPath(from, to, end.cost(), steps);
    }

    private record Step(NotchType type, int cost, Coercion edge, Step previous) {
    }

    private record Query(NotchType from, NotchType to) {
    }
}