package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

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
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.util.List;

@BugPattern(
    name = "Slf4jSignOnlyFormat",
    summary = "To make log readable, log format should contain not only sign but also texts",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_sign_only_format",
    linkType = LinkType.CUSTOM,
    severity = ERROR)
@AutoService(BugChecker.class)
public class SignOnlyFormat extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 3271269614137732880L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!Consts.IS_LOGGING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<VarSymbol> parameters = ASTHelpers.getSymbol(tree).getParameters();
    int formatIndex =
        ASTHelpers.isSubtype(
                parameters.get(0).type, state.getTypeFromString("org.slf4j.Marker"), state)
            ? 1
            : 0;

    ExpressionTree expression = tree.getArguments().get(formatIndex);
    Object constValue = ASTHelpers.constValue(expression);
    if (constValue == null) {
      return Description.NO_MATCH;
    }
    String format = constValue.toString();
    if (verifyFormat(format)) {
      return Description.NO_MATCH;
    }
    String message =
        String.format(
            "SLF4J logging format should contain non-sign text, but it is \'%s\'", format);
    return Description.builder(
            tree,
            "Slf4jSignOnlyFormat",
            "https://github.com/KengoTODA/findbugs-slf4j#slf4j_sign_only_format",
            ERROR,
            message)
        .build();
  }

  private boolean verifyFormat(String formatString) {
    CodepointIterator iterator = new CodepointIterator(formatString);
    while (iterator.hasNext()) {
      if (Character.isLetter(iterator.next().intValue())) {
        // found non-sign character.
        return true;
      }
    }
    return false;
  }
}
