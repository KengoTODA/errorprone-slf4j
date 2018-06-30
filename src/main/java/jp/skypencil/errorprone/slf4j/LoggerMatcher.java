package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import javax.annotation.Nullable;

final class LoggerMatcher implements Matcher<VariableTree> {
  private static final long serialVersionUID = -682327741943438574L;
  private static final String FQN_SLF4J_LOGGER = "org.slf4j.Logger";

  @Override
  public boolean matches(VariableTree tree, VisitorState state) {
    @Nullable Symbol symbol = ASTHelpers.getSymbol(tree.getType());
    return symbol != null && FQN_SLF4J_LOGGER.equals(symbol.toString());
  }
}
