package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Description.Builder;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

@BugPattern(
    name = "Slf4jIllegalPassedClass",
    summary = "LoggerFactory.getLogger(Class) should get the class that defines variable",
    tags = {"SLF4J"},
    severity = WARNING,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
@AutoService(BugChecker.class)
public class IllegalPassedClass extends BugChecker implements MethodInvocationTreeMatcher {

  private static final long serialVersionUID = 8309704818374164342L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    TypeSymbol type = tree.accept(new LoggerInitializerVisitor(), state);
    if (type == null) {
      return Description.NO_MATCH;
    }

    List<ClassSymbol> enclosingClasses = listEnclosingClasses(state);
    String message =
        String.format(
            "LoggerFactory.getLogger(Class) should get one of [%s] but it gets %s",
            enclosingClasses.stream().map(ClassSymbol::className).collect(Collectors.joining(",")),
            type.getSimpleName());
    Builder builder =
        Description.builder(
            tree,
            "Slf4jIllegalPassedClass",
            "https://github.com/KengoTODA/findbugs-slf4j#slf4j_illegal_passed_class",
            WARNING,
            message);

    for (ClassSymbol enclosingSymbol : enclosingClasses) {
      if (ASTHelpers.isSameType(type.type, enclosingSymbol.type, state)) {
        return Description.NO_MATCH;
      }
    }
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

  private List<ClassSymbol> listEnclosingClasses(VisitorState state) {
    ClassTree enclosing = state.findEnclosing(ClassTree.class);
    if (enclosing == null) {
      return Collections.emptyList();
    }

    List<ClassSymbol> result = new ArrayList<>();
    ClassSymbol enclosingSymbol = ASTHelpers.getSymbol(enclosing);
    while (enclosingSymbol != null) {
      result.add(enclosingSymbol);
      enclosingSymbol = ASTHelpers.enclosingClass(enclosingSymbol);
    }
    return result;
  }

  private static final class LoggerInitializerVisitor
      extends TreeScanner<TypeSymbol, VisitorState> {
    @Override
    public TypeSymbol visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (!isGetLogger.matches(node, state)) {
        return null;
      }

      ExpressionTree arg = node.getArguments().get(0);
      ClassType type = (ClassType) ASTHelpers.getType(arg);
      Type typeParameter = type.getTypeArguments().get(0);
      return typeParameter.asElement();
    }

    private final Matcher<ExpressionTree> isGetLogger =
        MethodMatchers.staticMethod()
            .onClass("org.slf4j.LoggerFactory")
            .named("getLogger")
            .withParameters("java.lang.Class");
  }
}
