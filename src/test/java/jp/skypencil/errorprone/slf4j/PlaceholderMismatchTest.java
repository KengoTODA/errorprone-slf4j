package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;

public class PlaceholderMismatchTest {
  @Test
  public void testNonConstantFormat() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "NonConstantFormat.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NonConstantFormat {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(this + \"{}\")"
                + "    }\n"
                + "}")
        .expectNoDiagnostics();
  }

  @Test
  public void testMarker() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "WithMarker.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "import org.slf4j.MarkerFactory;\n"
                + "\n"
                + "public class WithMarker {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    private final Marker marker = MarkerFactory.getMarker(\"Sample\");"
                + "    void method() {\n"
                + "        logger.info(marker, \"I have one placeholder, one parameter and one marker instance. {}\", 1)"
                + "    }\n"
                + "}")
        .expectNoDiagnostics();
  }

  @Test
  public void testThroable() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "WithThrowable.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class WithThrowable {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(\"I have one placeholder, one parameter and one throwable instance. {}\", 1, new Exception())"
                + "    }\n"
                + "}")
        .expectNoDiagnostics();
  }

  @Test
  public void testTooManyPlaceholders() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "TooManyPlaceholders.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class TooManyPlaceholders {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(\"I have two placeholders and one parameter! {} {}\", 1)"
                + "    }\n"
                + "}")
        .expectErrorMessage(
            "Slf4jPlaceholderMismatch",
            "Count of placeholder (2) does not match with count of parameter (1)"::equals);
  }

  @Test
  public void testTooManyParams() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "TooManyParams.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class TooManyParams {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(\"I have one placeholder and two parameters! {}\", 1, 2)"
                + "    }\n"
                + "}")
        .expectErrorMessage(
            "Slf4jPlaceholderMismatch",
            "Count of placeholder (1) does not match with count of parameter (2)"::equals);
  }
}
