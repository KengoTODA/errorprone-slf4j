package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PlaceholderMismatchTest {
  private CompilationTestHelper helper;

  @Before
  public void setup() {
    helper = CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
  }

  @Test
  public void testNonConstantFormat() {
    helper
        .addSourceLines(
            "NonConstantFormat.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NonConstantFormat {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(this + \"{}\");"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
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
                + "import org.slf4j.Marker;\n"
                + "\n"
                + "public class WithMarker {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    private final Marker marker = MarkerFactory.getMarker(\"Sample\");\n"
                + "    void method() {\n"
                + "        logger.info(marker, \"I have one placeholder, one parameter and one marker instance. {}\", 1);"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testThrowable() {
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
                + "        logger.info(\"I have one placeholder, one parameter and one throwable instance. {}\", 1, new Exception());"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
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
                + "        // BUG: Diagnostic contains: Count of placeholder (2) does not match with count of parameter (1)\n"
                + "        logger.info(\"I have two placeholders and one parameter! {} {}\", 1);"
                + "    }\n"
                + "}")
        .doTest();
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
                + "        // BUG: Diagnostic contains: Count of placeholder (1) does not match with count of parameter (2)\n"
                + "        logger.info(\"I have one placeholder and two parameters! {}\", 1, 2);"
                + "    }\n"
                + "}")
        .doTest();
  }

  @Test
  public void testVarArg() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "VarArg.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class VarArg {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(\"I have four placeholders and parameters! {}, {}, {}, {}\", 1, 2, 3, 4);"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testVarArgWithException() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "VarArg.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class VarArg {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        logger.info(\"I have four placeholders and parameters! {}, {}, {}, {}\", 1, 2, 3, 4, new Error());"
                + "    }\n"
                + "}")
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testNoParams() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(PlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "NoParam.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NoParam {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        // BUG: Diagnostic contains: Count of placeholder (1) does not match with count of parameter (0)\n"
                + "        logger.info(\"I have one placeholder and no parameter! {}\");"
                + "    }\n"
                + "}")
        .doTest();
  }
}
