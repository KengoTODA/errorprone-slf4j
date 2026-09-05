package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;

public class Slf4jDoNotLogMessageOfExceptionExplicitlyTest {
  @Test
  public void test() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(
            Slf4jDoNotLogMessageOfExceptionExplicitly.class, getClass());
    helper
        .addSourceLines(
            "WithManualMessage.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class WithManualMessage {
              private Logger logger = LoggerFactory.getLogger(WithManualMessage.class);

              void method(Exception e) {
                logger.info("Exception given", e);
                // BUG: Diagnostic contains: Do not log message returned from Throwable#getMessage and
                // Throwable#getLocalizedMessage
                logger.info("Message of given exception: {}", e.getMessage());
                // BUG: Diagnostic contains: Do not log message returned from Throwable#getMessage and
                // Throwable#getLocalizedMessage
                logger.info("Message of given exception: {}", e.getLocalizedMessage());
              }
            }
            """)
        .doTest();
  }
}
