package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.ErrorProneVersion;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

@BugPattern(
    altNames = {"IllegalPassedClass"},
    summary = "LoggerFactory.getLogger(Class) should get the class that defines variable",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_illegal_passed_class",
    linkType = LinkType.CUSTOM,
    severity = WARNING)
@AutoService(BugChecker.class)
public class Slf4jIllegalPassedClass extends BugChecker implements MethodInvocationTreeMatcher {

  private static final long serialVersionUID = 8309704818374164342L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    TypeSymbol type = tree.accept(new LoggerInitializerVisitor(), state);
    if (type == null) {
      return Description.NO_MATCH;
    }

    ImmutableList<ClassSymbol> enclosingClasses = listEnclosingClasses(state);
    for (ClassSymbol enclosingSymbol : enclosingClasses) {
      if (ASTHelpers.isSameType(type.type, enclosingSymbol.type, state)) {
        return Description.NO_MATCH;
      }
    }

    String message =
        String.format(
            "LoggerFactory.getLogger(Class) should get one of [%s] but it gets %s",
            enclosingClasses.stream().map(ClassSymbol::className).collect(Collectors.joining(",")),
            type.getSimpleName());
    Description.Builder builder = buildDescription(tree).setMessage(message);

    VariableTree variableTree = state.findEnclosing(VariableTree.class);
    if (variableTree != null && !variableTree.getModifiers().getFlags().contains(Modifier.STATIC)) {
      builder.addFix(
          SuggestedFix.builder().replace(tree.getArguments().get(0), "getClass()").build());
    }
    for (ClassSymbol enclosingSymbol : enclosingClasses) {
      builder.addFix(
          SuggestedFix.builder()
              .replace(tree.getArguments().get(0), enclosingSymbol.getSimpleName() + ".class")
              .build());
    }
    return builder.build();
  }

  private static ImmutableList<ClassSymbol> listEnclosingClasses(VisitorState state) {
    ClassTree enclosing = state.findEnclosing(ClassTree.class);
    if (enclosing == null) {
      return ImmutableList.of();
    }

    List<ClassSymbol> result = new ArrayList<>();
    ClassSymbol enclosingSymbol = ASTHelpers.getSymbol(enclosing);
    while (enclosingSymbol != null) {
      result.add(enclosingSymbol);
      enclosingSymbol = ASTHelpers.enclosingClass(enclosingSymbol);
    }
    return ImmutableList.copyOf(result);
  }

  private static final class LoggerInitializerVisitor
      extends TreeScanner<TypeSymbol, VisitorState> {
    @Override
    public TypeSymbol visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (!MatherHolder.isGetLogger.matches(node, state)) {
        return null;
      }

      ExpressionTree arg = node.getArguments().get(0);
      ClassType type = (ClassType) ASTHelpers.getType(arg);
      Type typeParameter = type.getTypeArguments().get(0);
      return typeParameter.asElement();
    }
  }

  /**
   * Apply the initialization-on-demand holder idiom to check the version of Errorprone only when we
   * use the matcher which depends on new API definition from {@code 2.11.0}.
   *
   * @see <a href="https://github.com/google/error-prone/issues/2909">GitHub Issue about the API
   *     change on the Errorprone side</a>
   * @see <a
   *     href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand
   *     holder idiom (Wikipedia)</a>
   */
  static final class MatherHolder {
    static {
      boolean supported =
          ErrorProneVersion.loadVersionFromPom()
              .transform(MatherHolder::checkSupportedVersion)
              .or(true);
      if (!supported) {
        throw new IllegalStateException("Run this rule with Errorprone 2.11.0 or later.");
      }
    }

    static boolean checkSupportedVersion(String version) {
      String[] split = version.split("\\.", 3);
      int major = Integer.parseInt(split[0], 10);
      if (major > 2) {
        // assuming this version uses new API definition
        return true;
      }
      int minor = Integer.parseInt(split[1], 10);
      return minor >= 11;
    }

    static Matcher<ExpressionTree> isGetLogger =
        MethodMatchers.staticMethod()
            .onClass("org.slf4j.LoggerFactory")
            .named("getLogger")
            .withParameters("java.lang.Class");
  }
}
