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
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.Modifier;

@BugPattern(
    name = "Slf4jLoggerShouldBePrivate",
    summary = "Do not publish Logger field, it should be private",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_private",
    linkType = LinkType.CUSTOM,
    severity = WARNING)
@AutoService(BugChecker.class)
public class DoNotPublishSlf4jLogger extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 3718668951312958622L;

  private static final Matcher<VariableTree> PRIVATE = new PrivateMatcher();
  private static final Matcher<VariableTree> SLF4J_LOGGER = new LoggerMatcher();

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, not(PRIVATE)).matches(tree, state)) {
      SuggestedFix.Builder builder = SuggestedFix.builder();
      SuggestedFixes.addModifiers(tree, state, Modifier.PRIVATE).ifPresent(builder::merge);
      SuggestedFixes.removeModifiers(tree, state, Modifier.PUBLIC, Modifier.PROTECTED)
          .ifPresent(builder::merge);
      return Description.builder(
              tree,
              "Slf4jLoggerShouldBePrivate",
              "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_private",
              WARNING,
              "Do not publish Logger field, it should be private")
          .addFix(builder.build())
          .build();
    }
    return Description.NO_MATCH;
  }

  private static final class PrivateMatcher implements Matcher<VariableTree> {
    private static final long serialVersionUID = 4297995943793097263L;

    @Override
    public boolean matches(VariableTree tree, VisitorState state) {
      return tree.getModifiers().getFlags().contains(Modifier.PRIVATE);
    }
  }
}
