package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BugPattern(
    name = "Slf4jPlaceholderMismatch",
    summary = "Count of placeholder does not match with count of parameter",
    tags = {"SLF4J"},
    severity = ERROR)
@AutoService(BugChecker.class)
public class PlaceholderMismatch extends BugChecker implements MethodInvocationTreeMatcher {

  private static final long serialVersionUID = 1442638758364703416L;
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(.?)(\\\\\\\\)*\\{\\}");

  private static final ImmutableSet<String> TARGET_METHOD_NAMES =
      ImmutableSet.of("trace", "debug", "info", "warn", "error");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    MethodSymbol method = ASTHelpers.getSymbol(tree);
    String methodName = method.toString();
    if (!TARGET_METHOD_NAMES.contains(methodName)) {
      return Description.NO_MATCH;
    }

    Symbol clazz = method.enclClass();
    if (!"org.slf4j.Logger".equals(clazz.toString())) {
      return Description.NO_MATCH;
    }

    List<VarSymbol> parameters = method.getParameters();
    int parameterSize = parameters.size() - 1; // -1 means 'formatString' is not parameter
    int formatIndex = 0;
    if (equals(parameters.get(0), "org.slf4j.Marker")) {
      parameterSize--;
      formatIndex = 1;
    }
    if (equals(parameters.get(parameters.size() - 1), "java.lang.Throwable")) {
      parameterSize--;
    }
    if (parameterSize <= 0) {
      return Description.NO_MATCH;
    }

    String format = parameters.get(formatIndex).getConstantValue().toString();
    if (format == null) {
      // format is not resolved at compile-phase
      return Description.NO_MATCH;
    }

    int placeholders = countPlaceholder(format);
    if (parameterSize != placeholders) {
      String message =
          String.format(
              "Count of placeholder (%d) does not match with count of parameter (%d)",
              placeholders, parameterSize);
      return Description.builder(
              tree,
              "Slf4jPlaceholderMismatch",
              "https://github.com/KengoTODA/findbugs-slf4j#slf4j_place_holder_mismatch",
              ERROR,
              message)
          .build();
    }
    return Description.NO_MATCH;
  }

  boolean equals(VarSymbol symbol, String name) {
    return name.equals(symbol.getQualifiedName().toString());
  }

  int countPlaceholder(String format) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(format);
    int count = 0;
    while (matcher.find()) {
      if (!"\\".equals(matcher.group(1))) {
        ++count;
      }
    }
    return count;
  }
}
