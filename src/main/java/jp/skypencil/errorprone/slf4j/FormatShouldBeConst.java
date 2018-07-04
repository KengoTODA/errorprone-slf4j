package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.CompileTimeConstantExpressionMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@BugPattern(
    name = "Slf4jFormatShouldBeConst",
    summary = "Format of SLF4J logging should be constant value",
    tags = {"SLF4J"},
    severity = ERROR)
@AutoService(BugChecker.class)
public class FormatShouldBeConst extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 3271269614137732880L;
  private static final ImmutableSet<String> TARGET_METHOD_NAMES =
      ImmutableSet.of("trace", "debug", "info", "warn", "error");

  private static final CompileTimeConstantExpressionMatcher IS_CONST =
      new CompileTimeConstantExpressionMatcher();
  private static final MethodNameMatcher IS_LOGGING_METHOD =
      MethodMatchers.instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .withNameMatching(
              Pattern.compile(
                  TARGET_METHOD_NAMES
                      .stream()
                      .map(Object::toString)
                      .collect(Collectors.joining("|"))));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!IS_LOGGING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<VarSymbol> parameters = ASTHelpers.getSymbol(tree).getParameters();
    int formatIndex =
        ASTHelpers.isSubtype(
                parameters.get(0).type, state.getTypeFromString("org.slf4j.Marker"), state)
            ? 1
            : 0;

    if (IS_CONST.matches(tree.getArguments().get(formatIndex), state)) {
      return Description.NO_MATCH;
    }

    String message =
        String.format(
            "SLF4J logging format should be constant value, but it is \'%s\'",
            tree.getArguments().get(formatIndex));
    return Description.builder(
            tree,
            "Slf4jFormatShouldBeConst",
            "https://github.com/KengoTODA/findbugs-slf4j#slf4j_format_should_be_const",
            ERROR,
            message)
        .build();
  }
}
