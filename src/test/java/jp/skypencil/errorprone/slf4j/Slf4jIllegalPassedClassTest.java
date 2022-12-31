package jp.skypencil.errorprone.slf4j;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class Slf4jIllegalPassedClassTest {
  @Test
  public void testSupportedVersion() {
    assertTrue(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("3.0.0"));
    assertTrue(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("2.11.0"));
    assertFalse(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("2.10.0"));
  }

  @Test
  public void testRefactoringInstanceField() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
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
    BugChecker checker = new Slf4jIllegalPassedClass();
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
    BugChecker checker = new Slf4jIllegalPassedClass();
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
    BugChecker checker = new Slf4jIllegalPassedClass();
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
                + "        private static final Logger LOGGER = LoggerFactory.getLogger(PrivateLogger.class);\n"
                + "    }\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringInnerClass2() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass())
            .setFixChooser(FixChoosers.FIRST);
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

  @Test
  public void testOtherField() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "WithLoggerFactory.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.ILoggerFactory;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class WithLoggerFactory {\n"
                + "    private final String HELLO = \"World\";"
                + "    private final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();\n"
                + "    private final Logger logger = LoggerFactory.getLogger(\"string\");\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testClassWithoutProblem() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testInnerClassWithoutProblem() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private static class InnerClass {\n"
                + "        private static final Logger LOGGER = LoggerFactory.getLogger(InnerClass.class);\n"
                + "        private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);\n"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testMethodParameter() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PrivateLogger {\n"
                + "    private void method (String string) {\n"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }
}
