package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;

public class Slf4jSignOnlyFormatTest {
  private CompilationTestHelper helper;

  @Before
  public void setup() {
    helper = CompilationTestHelper.newInstance(Slf4jSignOnlyFormat.class, getClass());
  }

  @Test
  public void testPlaceholderOnly() {
    helper
        .addSourceLines(
            "PlaceholderOnly.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PlaceholderOnly {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                // BUG: Diagnostic contains: SLF4J logging format should contain non-sign text, but it is '{},
                // {}'
                logger.info("{}, {}", 1, 2);
              }
            }
            """)
        .doTest();
  }

  @Test
  public void testNonConstantFormat() {
    helper
        .addSourceLines(
            "NonConstantFormat.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class NonConstantFormat {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                logger.info(this + " is me");
              }
            }
            """)
        .doTest();
  }

  @Test
  public void testMarker() {
    helper
        .addSourceLines(
            "WithMarker.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.slf4j.MarkerFactory;
            import org.slf4j.Marker;

            public class WithMarker {
              private final Logger logger = LoggerFactory.getLogger(getClass());
              private final Marker marker = MarkerFactory.getMarker("Sample");

              void method() {
                logger.info(marker, "I have one placeholder, one parameter and one marker instance. {}", 1);
                // BUG: Diagnostic contains: non-sign text, but it is '{}: {}'
                logger.info(marker, "{}: {}", 1, 2);
              }
            }
            """)
        .doTest();
  }

  @Test
  public void testTernaryInStaticBlock() {
    helper
        .addSourceLines(
            "TernaryInStaticBlock.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class TernaryInStaticBlock {
              public static boolean INDENT = true;
              public static final boolean INDENT_FINAL = true;
              private static final Logger logger = LoggerFactory.getLogger(TernaryInStaticBlock.class);

              static {
                logger.info((INDENT ? "  " : "") + "{}", 1);
                // BUG: Diagnostic contains: SLF4J logging format should contain non-sign text, but it is '  {}'
                logger.info((INDENT_FINAL ? "  " : "") + "{}", 1);
              }
            }
            """)
        .doTest();
  }
}
