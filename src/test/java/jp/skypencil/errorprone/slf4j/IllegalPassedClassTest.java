package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class IllegalPassedClassTest {
  @Test
  public void testRefactoringInstanceField() throws IOException {
    BugChecker checker = new IllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(String.class);\n"
                + "}")
        .addOutputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringStaticField() throws IOException {
    BugChecker checker = new IllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private static final Logger LOGGER = LoggerFactory.getLogger(String.class);\n"
                + "}")
        .addOutputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateLogger.class);\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void test2ndFixForInstanceField() throws IOException {
    BugChecker checker = new IllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass())
            .setFixChooser(FixChoosers.SECOND);
    helper
        .addInputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(String.class);\n"
                + "}")
        .addOutputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringInnerClass() throws IOException {
    BugChecker checker = new IllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private static class InnerClass {\n"
                + "        private static final Logger LOGGER = LoggerFactory.getLogger(String.class);\n"
                + "    }\n"
                + "}")
        .addOutputLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private static class InnerClass {\n"
                + "        private static final Logger LOGGER = LoggerFactory.getLogger(InnerClass.class);\n"
                + "    }\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }
}
