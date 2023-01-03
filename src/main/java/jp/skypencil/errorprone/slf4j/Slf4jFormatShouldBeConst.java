package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
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

@BugPattern(
    altNames = {"FormatShouldBeConst"},
    summary = "Format of SLF4J logging should be constant value",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_format_should_be_const",
    linkType = LinkType.CUSTOM,
    severity = ERROR)
@AutoService(BugChecker.class)
public class Slf4jFormatShouldBeConst extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 3271269614137732880L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!Consts.IS_LOGGING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    int formatIndex = IS_MARKER.matches(tree.getArguments().get(0), state) ? 1 : 0;

    ExpressionTree expression = tree.getArguments().get(formatIndex);
    Object constValue = ASTHelpers.constValue(expression);
    if (constValue != null) {
      return Description.NO_MATCH;
    }

    String message =
        String.format(
            "SLF4J logging format should be constant value, but it is \'%s\'",
            state.getSourceForNode(expression));
    return buildDescription(tree).setMessage(message).build();
  }
}
