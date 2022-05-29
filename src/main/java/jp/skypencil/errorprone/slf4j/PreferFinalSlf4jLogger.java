package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.isField;
import static com.google.errorprone.matchers.Matchers.not;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.Modifier;

@BugPattern(
    name = "Slf4jLoggerShouldBeFinal",
    summary = "Logger field should be final",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_final",
    linkType = LinkType.CUSTOM,
    severity = WARNING)
@AutoService(BugChecker.class)
public class PreferFinalSlf4jLogger extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = -5127926153475887075L;
  private static final Matcher<VariableTree> FINAL = new FinalMatcher();
  private static final Matcher<VariableTree> SLF4J_LOGGER = new LoggerMatcher();

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, not(FINAL)).matches(tree, state)) {
      return Description.builder(
              tree,
              "Slf4jLoggerShouldBeFinal",
              "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_final",
              WARNING,
              "Logger field should be final")
          .addFix(SuggestedFixes.addModifiers(tree, state, Modifier.FINAL))
          .build();
    }
    return Description.NO_MATCH;
  }

  private static final class FinalMatcher implements Matcher<VariableTree> {
    private static final long serialVersionUID = -8036242499062168842L;

    @Override
    public boolean matches(VariableTree tree, VisitorState state) {
      return tree.getModifiers().getFlags().contains(Modifier.FINAL);
    }
  }
}
