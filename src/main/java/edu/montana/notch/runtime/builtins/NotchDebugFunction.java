package edu.montana.notch.runtime.builtins;

import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.Pair;
import edu.montana.notch.util.Text;

import java.util.List;

import static edu.montana.notch.util.Pair.pair;

public final class NotchDebugFunction implements NotchMethod {
    private static NotchBoundMethod INST = null;
    public static NotchBoundMethod getInstance() {
        if (INST == null) {
            INST = new NotchBoundMethod(NotchRuntime.BUILTIN, null, new NotchDebugFunction());
        }
        return INST;
    }
    private NotchDebugFunction() {}

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<Object> args) {
        return args.size() == 1;
    }

    @Override
    public Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args) {
        final var arg = args.get(0);
        return Text.repr(arg);
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getDisplayName() {
        return "notch.builtins.debug()";
    }

    @Override
    public String getQualifiedName() {
        return "notch.builtins.debug";
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public List<Pair<List<Pair<String, NotchType>>, NotchType>> getMethodVariants() {
        return List.of(pair(
                List.of(pair("object", TypeSystem.getType(Object.class))),
                TypeSystem.getType(String.class)
        ));
    }
}
