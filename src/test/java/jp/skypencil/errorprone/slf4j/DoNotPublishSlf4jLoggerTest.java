package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class DoNotPublishSlf4jLoggerTest {
  @Test
  public void testRefactoringPublicLogger() throws IOException {
    BugChecker checker = new DoNotPublishSlf4jLogger();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PublicLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PublicLogger {\n"
                + "    public static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    public Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}")
        .addOutputLines(
            "PublicLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PublicLogger {\n"
                + "    private static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    private Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringProtectedLogger() throws IOException {
    BugChecker checker = new DoNotPublishSlf4jLogger();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "ProtectedLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class ProtectedLogger {\n"
                + "    protected static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    protected Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}")
        .addOutputLines(
            "ProtectedLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class ProtectedLogger {\n"
                + "    private static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    private Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringPackagePrivateLogger() throws IOException {
    BugChecker checker = new DoNotPublishSlf4jLogger();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PackagePrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PackagePrivateLogger {\n"
                + "    static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}")
        .addOutputLines(
            "PackagePrivateLogger.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class PackagePrivateLogger {\n"
                + "    private static Logger LOGGER = LoggerFactory.getLogger(\"static\");\n"
                + "    private Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "}\n"
                + "")
        .doTest(TestMode.TEXT_MATCH);
  }
}
