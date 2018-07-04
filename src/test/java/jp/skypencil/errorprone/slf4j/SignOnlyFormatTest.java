package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;

public class SignOnlyFormatTest {
  private CompilationTestHelper helper;

  @Before
  public void setup() {
    helper = CompilationTestHelper.newInstance(SignOnlyFormat.class, getClass());
  }

  @Test
  public void testPlaceholderOnly() {
    helper
        .addSourceLines(
            "NonConstantFormat.java",
            "import org.slf4j.Logger;\n"
                + "import org.slf4j.LoggerFactory;\n"
                + "\n"
                + "public class NonConstantFormat {\n"
                + "    private final Logger logger = LoggerFactory.getLogger(getClass());\n"
                + "    void method() {\n"
                + "        // BUG: Diagnostic contains: SLF4J logging format should contain non-sign text, but it is \'{}, {}\'\n"
                + "        logger.info(\"{}, {}\", 1, 2);"
                + "    }\n"
                + "}")
        .doTest();
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
                + "        logger.info(this + \" is me\");"
                + "    }\n"
                + "}")
        .doTest();
  }

  @Test
  public void testMarker() {
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
                + "        // BUG: Diagnostic contains: SLF4J logging format should contain non-sign text, but it is \'{}: {}\'\n"
                + "        logger.info(marker, \"{}: {}\", 1, 2);"
                + "    }\n"
                + "}")
        .doTest();
  }
}
