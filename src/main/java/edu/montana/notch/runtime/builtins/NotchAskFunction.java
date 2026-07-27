package edu.montana.notch.runtime.builtins;

import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.Pair;

import java.util.List;
import java.util.Scanner;

import static edu.montana.notch.util.Pair.pair;

public final class NotchAskFunction implements NotchMethod {
    private static NotchBoundMethod INST = null;
    public static NotchBoundMethod getInstance() {
        if (INST == null) {
            INST = new NotchBoundMethod(NotchRuntime.BUILTIN, null, new NotchAskFunction());
        }
        return INST;
    }
    private NotchAskFunction() {}

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<Object> args) {
        return args.size() == 1;
    }

    @Override
    public Object invoke(NotchRuntime runtime, Object rootVal, List<Object> args) {
        var prompt = (String) args.get(0);
        System.out.print(prompt);
        System.out.flush();
        return new Scanner(System.in).nextLine();
    }

    @Override
    public String getName() {
        return "ask";
    }

    @Override
    public String getDisplayName() {
        return "notch.builtins.ask()";
    }

    @Override
    public String getQualifiedName() {
        return "notch.builtins.ask";
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
                List.of(pair("prompt", TypeSystem.getType(String.class))),
                TypeSystem.getType(String.class)
        ));
    }
}
