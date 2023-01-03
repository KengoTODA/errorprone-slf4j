package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isField;
import static com.google.errorprone.matchers.Matchers.not;
import static jp.skypencil.errorprone.slf4j.Consts.SLF4J_LOGGER;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.Modifier;

@BugPattern(
    altNames = {"DoNotPublishSlf4jLogger"},
    summary = "Do not publish Logger field, it should be private",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_private",
    linkType = LinkType.CUSTOM,
    severity = WARNING)
@AutoService(BugChecker.class)
public class Slf4jLoggerShouldBePrivate extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 3718668951312958622L;

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, not(hasModifier(Modifier.PRIVATE))).matches(tree, state)) {
      SuggestedFix.Builder builder = SuggestedFix.builder();
      SuggestedFixes.addModifiers(tree, state, Modifier.PRIVATE).ifPresent(builder::merge);
      SuggestedFixes.removeModifiers(tree, state, Modifier.PUBLIC, Modifier.PROTECTED)
          .ifPresent(builder::merge);
      return buildDescription(tree).addFix(builder.build()).build();
    }
    return Description.NO_MATCH;
  }
}
