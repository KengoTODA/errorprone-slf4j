package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class Slf4JLoggerShouldBeNonStaticTest {
  @Test
  public void testRefactoringStaticLogger() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBeNonStatic();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "StaticLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class StaticLogger {\n"
                + "    private static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "}")
        .addOutputLines(
            "StaticLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class StaticLogger {\n"
                + "    private Logger logger = LoggerFactory.getLogger(\"static\");\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringWithAnnotation() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBeNonStatic();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "StaticLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class StaticLogger {\n"
                + "    private static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "}")
        .addOutputLines(
            "StaticLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class StaticLogger {\n"
                + "    private Logger logger = LoggerFactory.getLogger(\"static\");\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }
}
