package jp.skypencil.errorprone.slf4j;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Consts {
  static final ImmutableSet<String> TARGET_METHOD_NAMES =
      ImmutableSet.of("trace", "debug", "info", "warn", "error");

  static final MethodNameMatcher IS_LOGGING_METHOD =
      MethodMatchers.instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .withNameMatching(
              Pattern.compile(
                  Consts.TARGET_METHOD_NAMES.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining("|"))));

  private Consts() {}
}
