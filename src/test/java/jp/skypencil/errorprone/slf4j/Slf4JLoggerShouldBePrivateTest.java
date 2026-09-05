package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class Slf4JLoggerShouldBePrivateTest {
  @Test
  public void testRefactoringPublicLogger() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBePrivate();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PublicLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PublicLogger {
              public Logger logger = LoggerFactory.getLogger(PublicLogger.class);
            }
            """)
        .addOutputLines(
            "PublicLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PublicLogger {
              private Logger logger = LoggerFactory.getLogger(PublicLogger.class);
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringProtectedLogger() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBePrivate();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "ProtectedLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class ProtectedLogger {
              protected Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .addOutputLines(
            "ProtectedLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class ProtectedLogger {
              private Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringPackagePrivateLogger() throws IOException {
    BugChecker checker = new Slf4jLoggerShouldBePrivate();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PackagePrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PackagePrivateLogger {
              Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .addOutputLines(
            "PackagePrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PackagePrivateLogger {
              private Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
