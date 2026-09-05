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
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class NonFinalLogger {
              private Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .addOutputLines(
            "NonFinalLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class NonFinalLogger {
              private final Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
