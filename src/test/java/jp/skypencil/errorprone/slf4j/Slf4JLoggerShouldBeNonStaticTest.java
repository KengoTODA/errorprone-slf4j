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
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class StaticLogger {
              private static Logger LOGGER = LoggerFactory.getLogger("static");
            }
            """)
        .addOutputLines(
            "StaticLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class StaticLogger {
              private Logger logger = LoggerFactory.getLogger("static");
            }
            """)
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
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class StaticLogger {
              private static Logger LOGGER = LoggerFactory.getLogger("static");
            }
            """)
        .addOutputLines(
            "StaticLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class StaticLogger {
              private Logger logger = LoggerFactory.getLogger("static");
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
