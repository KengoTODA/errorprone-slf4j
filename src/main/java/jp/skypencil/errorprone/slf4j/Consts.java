package jp.skypencil.errorprone.slf4j;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import java.util.regex.Pattern;

class Consts {
  private static final String FQN_SLF4J_LOGGER = "org.slf4j.Logger";
  private static final String FQN_SLF4J_MARKER = "org.slf4j.Marker";
  static final ImmutableSet<String> TARGET_METHOD_NAMES =
      ImmutableSet.of("trace", "debug", "info", "warn", "error");

  static final MethodNameMatcher IS_LOGGING_METHOD =
      MethodMatchers.instanceMethod()
          .onDescendantOf(FQN_SLF4J_LOGGER)
          .withNameMatching(Pattern.compile(String.join("|", TARGET_METHOD_NAMES)));

  static final Matcher<VariableTree> SLF4J_LOGGER = Matchers.isSubtypeOf(FQN_SLF4J_LOGGER);
  static final Matcher<ExpressionTree> IS_MARKER = Matchers.isSubtypeOf(FQN_SLF4J_MARKER);

  private Consts() {}
}
