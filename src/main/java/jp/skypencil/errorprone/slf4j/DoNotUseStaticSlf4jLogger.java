package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.isField;
import static com.google.errorprone.matchers.Matchers.isStatic;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFix.Builder;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.VariableTree;
import java.util.Optional;
import javax.lang.model.element.Modifier;

@BugPattern(
    name = "Slf4jLoggerShouldBeNonStatic",
    summary = "Do not use static Logger field, use non-static one instead",
    tags = {"SLF4J"},
    severity = SUGGESTION)
@AutoService(BugChecker.class)
public class DoNotUseStaticSlf4jLogger extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 2656759159827947106L;
  private static final Matcher<VariableTree> SLF4J_LOGGER = new LoggerMatcher();

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, isStatic()).matches(tree, state)) {
      Builder builder = SuggestedFix.builder();
      SuggestedFixes.removeModifiers(tree, state, Modifier.STATIC).ifPresent(builder::merge);
      suggestRename(tree, state).ifPresent(builder::merge);

      return Description.builder(
              tree,
              "Slf4jLoggerShouldBeNonStatic",
              "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_non_static",
              SUGGESTION,
              "Do not use static Logger field, use non-static one instead")
          .addFix(builder.build())
          .build();
    }
    return Description.NO_MATCH;
  }

  private Optional<SuggestedFix> suggestRename(VariableTree tree, VisitorState state) {
    String name = tree.getName().toString();
    String formatted = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    if (name.equals(formatted)) {
      return Optional.empty();
    }
    return Optional.of(SuggestedFixes.renameVariable(tree, formatted, state));
  }
}
