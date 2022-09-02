package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static jp.skypencil.errorprone.slf4j.Consts.IS_MARKER;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BugPattern(
    name = "Slf4jPlaceholderMismatch",
    summary = "Count of placeholder does not match with count of parameter",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_place_holder_mismatch",
    linkType = LinkType.CUSTOM,
    severity = ERROR)
@AutoService(BugChecker.class)
public class PlaceholderMismatch extends BugChecker implements MethodInvocationTreeMatcher {

  private static final long serialVersionUID = 1442638758364703416L;
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(.?)(\\\\\\\\)*\\{\\}");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!Consts.IS_LOGGING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    java.util.List<? extends ExpressionTree> arguments = tree.getArguments();
    int argumentSize = arguments.size() - 1; // -1 means 'formatString' is not parameter
    int formatIndex = 0;
    if (IS_MARKER.matches(arguments.get(0), state)) {
      argumentSize--;
      formatIndex = 1;
    }
    if (IS_THROWABLE.matches(arguments.get(arguments.size() - 1), state)) {
      argumentSize--;
    }
    if (argumentSize < 0) {
      return Description.NO_MATCH;
    }
    Object constant = ASTHelpers.constValue(tree.getArguments().get(formatIndex));
    if (constant == null) {
      // format is not resolved at compile-phase
      return Description.NO_MATCH;
    }
    String format = constant.toString();

    int placeholders = countPlaceholder(format);
    if (argumentSize != placeholders) {
      String message =
          String.format(
              "Count of placeholder (%d) does not match with count of parameter (%d)",
              placeholders, argumentSize);
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

  private static final com.google.errorprone.matchers.Matcher<ExpressionTree> IS_THROWABLE =
      isSubtypeOf("java.lang.Throwable");

  private static int countPlaceholder(String format) {
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
