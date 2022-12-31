package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class Slf4JLoggerShouldBeFinalTest {
  @Test
  public void testRefactoringStaticLogger() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBeFinal();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "NonFinalLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NonFinalLogger {\n"
                + "    private Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}")
        .addOutputLines(
            "NonFinalLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NonFinalLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }
}
