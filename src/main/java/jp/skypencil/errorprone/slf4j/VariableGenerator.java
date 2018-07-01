package jp.skypencil.errorprone.slf4j;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Ordering;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

class VariableGenerator {
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

  private String stringify(Set<Modifier> flags) {
    List<Modifier> sortedFlags = new ArrayList<>(flags);
    sortedFlags.sort(MODIFIER_COMPARATOR);
    return sortedFlags.stream().map(Object::toString).collect(Collectors.joining(" ")).concat(" ");
  }

  Fix createSuggestedFix(
      VisitorState state, VariableTree tree, Set<Modifier> flags, String initializer) {
    StringBuilder replacement = new StringBuilder();
    String name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tree.getName().toString());
    replacement
        .append(generateAnnotations(state, tree))
        .append(stringify(flags))
        .append(tree.getType())
        .append(" ")
        .append(name)
        .append(" = ")
        .append(initializer)
        .append(";");

    return SuggestedFix.builder()
        .addImport("org.slf4j.LoggerFactory")
        .replace(tree, replacement.toString())
        .build();
  }

  private CharSequence generateAnnotations(VisitorState state, VariableTree tree) {
    StringBuilder builder = new StringBuilder();
    List<? extends AnnotationTree> annotations = tree.getModifiers().getAnnotations();
    if (!annotations.isEmpty()) {
      JCTree first = (JCTree) annotations.get(0);
      JCTree last = (JCTree) annotations.get(annotations.size() - 1);
      builder
          .append(
              state
                  .getSourceCode()
                  .subSequence(first.getStartPosition(), state.getEndPosition(last)))
          .append(lineSeparator);
    }
    return builder;
  }
}
