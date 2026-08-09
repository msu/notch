package edu.montana.notch.types;

import edu.montana.notch.util.Pair;

import java.util.List;

public record NotchFunctionDescription(NotchType returnType, List<Pair<NotchType, String>> parameters) {
}
