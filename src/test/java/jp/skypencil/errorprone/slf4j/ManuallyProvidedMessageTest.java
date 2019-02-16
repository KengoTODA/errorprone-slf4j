package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;

public class ManuallyProvidedMessageTest {
  @Test
  public void test() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(ManuallyProvidedMessage.class, getClass());
    helper
        .addSourceLines(
            "WithManualMessage.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class WithManualMessage {\n"
                + "    private Logger logger = LoggerFactory.getLogger(WithManualMessage.class);\n"
                + "    void method(Exception e) {\n"
                + "        logger.info(\"Exception given\", e);\n"
                + "        // BUG: Diagnostic contains: Do not log message returned from Throwable#getMessage and Throwable#getLocalizedMessage\n"
                + "        logger.info(\"Message of given exception: {}\", e.getMessage());\n"
                + "        // BUG: Diagnostic contains: Do not log message returned from Throwable#getMessage and Throwable#getLocalizedMessage\n"
                + "        logger.info(\"Message of given exception: {}\", e.getLocalizedMessage());\n"
                + "    }\n"
                + "}")
        .doTest();
  }
}
