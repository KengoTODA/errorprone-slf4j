package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;

public class Slf4jFormatShouldBeConstTest {
  private CompilationTestHelper helper;

  @Before
  public void setup() {
    helper = CompilationTestHelper.newInstance(Slf4jFormatShouldBeConst.class, getClass());
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
                // BUG: Diagnostic contains: constant value, but it is 'this + " is me"'
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
                // BUG: Diagnostic contains: SLF4J logging format should be constant value, but it is 'this + "
                // is me"'
                logger.info(marker, this + " is me");
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
              public static boolean DEBUG = false;
              public static final boolean DEBUG_FINAL = false;
              private static final Logger logger = LoggerFactory.getLogger(TernaryInStaticBlock.class);

              static {
                // BUG: Diagnostic contains: SLF4J logging format should be constant value, but it is '"Debug
                // mode " + (DEBUG ? "enabled." : "disabled.")'
                logger.info("Debug mode " + (DEBUG ? "enabled." : "disabled."));
                logger.info("Debug mode " + (DEBUG_FINAL ? "enabled." : "disabled."));
              }
            }
            """)
        .doTest();
  }
}
