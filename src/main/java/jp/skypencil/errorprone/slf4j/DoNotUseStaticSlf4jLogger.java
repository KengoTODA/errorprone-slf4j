package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.isField;
import static com.google.errorprone.matchers.Matchers.isStatic;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.util.HashSet;
import java.util.Set;
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
  private final VariableGenerator generator = new VariableGenerator();

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, isStatic()).matches(tree, state)) {
      Fix fix =
          generator.createSuggestedFix(
              state,
              tree,
              createSuggestedFlags(tree.getModifiers()),
              "LoggerFactory.getLogger(getClass())");
      return Description.builder(
              tree,
              "Slf4jLoggerShouldBeNonStatic",
              "https://github.com/KengoTODA/findbugs-slf4j#slf4j_logger_should_be_non_static",
              SUGGESTION,
              "Do not use static Logger field, use non-static one instead")
          .addFix(fix)
          .build();
    }
    return Description.NO_MATCH;
  }

  private Set<Modifier> createSuggestedFlags(ModifiersTree modifiers) {
    Set<Modifier> flags = new HashSet<>(modifiers.getFlags());
    flags.remove(Modifier.STATIC);
    return flags;
  }
}
