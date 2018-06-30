package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.isField;
import static com.google.errorprone.matchers.Matchers.isStatic;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Ordering;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  /**
   * Comparator to sort {@link Modifier}. The order is based on styleguide from Google and Open JDK
   * community.
   *
   * @see <a href= "https://google.github.io/styleguide/javaguide.html#s4.8.7-modifiers">Google Java
   *     Style</a>
   * @see <a href=
   *     "http://cr.openjdk.java.net/~alundblad/styleguide/index-v6.html#toc-modifiers">Open JDK
   *     Java Style Guidelines</a>
   */
  private static final Comparator<Modifier> MODIFIER_COMPARATOR =
      Ordering.explicit(
          Modifier.PUBLIC,
          Modifier.PROTECTED,
          Modifier.PRIVATE,
          Modifier.ABSTRACT,
          Modifier.STATIC,
          Modifier.FINAL,
          Modifier.TRANSIENT,
          Modifier.VOLATILE,
          Modifier.DEFAULT,
          Modifier.SYNCHRONIZED,
          Modifier.NATIVE,
          Modifier.STRICTFP);

  private final String lineSeparator = System.lineSeparator();

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (allOf(isField(), SLF4J_LOGGER, isStatic()).matches(tree, state)) {
      Fix fix = createSuggestedFix(tree);
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

  private Fix createSuggestedFix(VariableTree tree) {
    StringBuilder replacement = new StringBuilder();
    List<? extends AnnotationTree> annotations = tree.getModifiers().getAnnotations();
    for (AnnotationTree annotation : annotations) {
      replacement.append(annotation);
      replacement.append(lineSeparator);
    }
    Set<Modifier> flags = createSuggestedFlags(tree.getModifiers());
    String name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tree.getName().toString());
    replacement
        .append(stringify(flags))
        .append(tree.getType())
        .append(" ")
        .append(name)
        .append(" = LoggerFactory.getLogger(getClass());");

    return SuggestedFix.builder()
        .addImport("org.slf4j.LoggerFactory")
        .replace(tree, replacement.toString())
        .build();
  }

  private Set<Modifier> createSuggestedFlags(ModifiersTree modifiers) {
    Set<Modifier> flags = new HashSet<>(modifiers.getFlags());
    flags.remove(Modifier.STATIC);
    return flags;
  }

  private String stringify(Set<Modifier> flags) {
    List<Modifier> sortedFlags = new ArrayList<>(flags);
    sortedFlags.sort(MODIFIER_COMPARATOR);
    return sortedFlags.stream().map(Object::toString).collect(Collectors.joining(" ")).concat(" ");
  }
}
