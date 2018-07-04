package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Description.Builder;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import java.util.Optional;
import javax.lang.model.element.Modifier;

@BugPattern(
    name = "Slf4jIllegalPassedClass",
    summary = "LoggerFactory.getLogger(Class) should get the class that defines variable",
    tags = {"SLF4J"},
    severity = WARNING)
@AutoService(BugChecker.class)
public class IllegalPassedClass extends BugChecker implements VariableTreeMatcher {

  private static final long serialVersionUID = 8309704818374164342L;

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Optional<TypeSymbol> type = tree.getInitializer().accept(new LoggerInitializerVisitor(), null);
    if (!type.isPresent()) {
      return Description.NO_MATCH;
    }

    ClassTree enclosing = state.findEnclosing(ClassTree.class);
    if (enclosing == null) {
      return Description.NO_MATCH;
    }

    ClassSymbol enclosingSymbol = ASTHelpers.getSymbol(enclosing);
    if (type.get().equals(enclosingSymbol)) {
      return Description.NO_MATCH;
    }

    Builder builder =
        Description.builder(
            tree,
            "Slf4jIllegalPassedClass",
            "https://github.com/KengoTODA/findbugs-slf4j#slf4j_illegal_passed_class",
            WARNING,
            "LoggerFactory.getLogger(Class) should get "
                + enclosingSymbol.className()
                + " but it gets "
                + type.get().getSimpleName());
    if (!tree.getModifiers().getFlags().contains(Modifier.STATIC)) {
      builder.addFix(
          SuggestedFix.builder()
              .replace(tree.getInitializer(), "LoggerFactory.getLogger(getClass())")
              .build());
    }
    builder.addFix(
        SuggestedFix.builder()
            .replace(
                tree.getInitializer(),
                "LoggerFactory.getLogger(" + enclosingSymbol.getSimpleName() + ".class)")
            .build());
    return builder.build();
  }

  private static final class LoggerInitializerVisitor
      extends TreeScanner<Optional<TypeSymbol>, Void> {
    @Override
    public Optional<TypeSymbol> visitMethodInvocation(MethodInvocationTree node, Void v) {
      Symbol method = ASTHelpers.getSymbol(node.getMethodSelect());
      Symbol clazz = method.enclClass();
      String methodName = method.toString();

      if ("org.slf4j.LoggerFactory".equals(clazz.toString())
          && methodName.startsWith("getLogger(")) {
        switch (methodName) {
          case "getLogger(java.lang.String)":
            // nothing to check
            break;
          case "getLogger(java.lang.Class<?>)":
            ExpressionTree arg = node.getArguments().get(0);
            ClassType type = (ClassType) ASTHelpers.getType(arg);
            Type typeParameter = type.getTypeArguments().get(0);
            return Optional.of(typeParameter.asElement());
          default:
            throw new AssertionError("Unknown getLogger method at " + node);
        }
      }
      return Optional.empty();
    }
  }
}
