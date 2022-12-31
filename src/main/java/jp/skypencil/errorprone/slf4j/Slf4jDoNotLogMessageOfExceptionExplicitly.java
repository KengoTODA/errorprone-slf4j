package jp.skypencil.errorprone.slf4j;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import java.util.Optional;
import java.util.function.Predicate;

@BugPattern(
    altNames = {"ManuallyProvidedMessage"},
    summary =
        "Do not log message returned from Throwable#getMessage and Throwable#getLocalizedMessage",
    tags = {"SLF4J"},
    link = "https://github.com/KengoTODA/findbugs-slf4j#slf4j_manually_provided_message",
    linkType = LinkType.CUSTOM,
    severity = ERROR)
@AutoService(BugChecker.class)
public class Slf4jDoNotLogMessageOfExceptionExplicitly extends BugChecker
    implements MethodInvocationTreeMatcher {

  private static final long serialVersionUID = 7903613628689308557L;
  private static final Predicate<String> MESSAGE_GETTER = "getMessage"::equals;
  private static final Predicate<String> LOCALIZED_MESSAGE_GETTER = "getLocalizedMessage"::equals;
  private static final Predicate<String> THROWABLE = "java.lang.Throwable"::equals;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!Consts.IS_LOGGING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Optional<JCFieldAccess> problem =
        tree.getArguments().stream()
            .filter(arg -> arg.getClass().isAssignableFrom(JCMethodInvocation.class))
            .map(JCMethodInvocation.class::cast)
            .map(arg -> arg.meth)
            .filter(meth -> meth.getClass().isAssignableFrom(JCFieldAccess.class))
            .map(JCFieldAccess.class::cast)
            .filter(
                meth -> MESSAGE_GETTER.or(LOCALIZED_MESSAGE_GETTER).test(meth.sym.name.toString()))
            .filter(meth -> THROWABLE.test(meth.sym.owner.toString()))
            .findFirst();
    if (problem.isPresent()) {
      return buildDescription(tree)
          .setMessage(
              "Do not log message returned from Throwable#getMessage and Throwable#getLocalizedMessage. It is enough to provide throwable instance as the last argument, then binding will log its message.")
          .build();
    }
    return Description.NO_MATCH;
  }
}
