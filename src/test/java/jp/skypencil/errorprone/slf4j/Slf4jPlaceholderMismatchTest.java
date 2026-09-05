package jp.skypencil.errorprone.slf4j;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Slf4jPlaceholderMismatchTest {
  private CompilationTestHelper helper;

  @Before
  public void setup() {
    helper = CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
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
                logger.info(this + "{}");
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testMarker() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
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
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testThrowable() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "WithThrowable.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class WithThrowable {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                logger.info(
                    "I have one placeholder, one parameter and one throwable instance. {}", 1, new Exception());
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testTooManyPlaceholders() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "TooManyPlaceholders.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class TooManyPlaceholders {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                // BUG: Diagnostic contains: Count of placeholder (2) does not match with count of parameter (1)
                logger.info("I have two placeholders and one parameter! {} {}", 1);
              }
            }
            """)
        .doTest();
  }

  @Test
  public void testTooManyParams() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "TooManyParams.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class TooManyParams {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                // BUG: Diagnostic contains: Count of placeholder (1) does not match with count of parameter (2)
                logger.info("I have one placeholder and two parameters! {}", 1, 2);
              }
            }
            """)
        .doTest();
  }

  @Test
  public void testVarArg() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "VarArg.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class VarArg {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                logger.info("I have four placeholders and parameters! {}, {}, {}, {}", 1, 2, 3, 4);
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testVarArgWithException() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "VarArg.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class VarArg {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                logger.info("I have four placeholders and parameters! {}, {}, {}, {}", 1, 2, 3, 4, new Error());
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testNoParams() {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jPlaceholderMismatch.class, getClass());
    helper
        .addSourceLines(
            "NoParam.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class NoParam {
              private final Logger logger = LoggerFactory.getLogger(getClass());

              void method() {
                // BUG: Diagnostic contains: Count of placeholder (1) does not match with count of parameter (0)
                logger.info("I have one placeholder and no parameter! {}");
              }
            }
            """)
        .doTest();
  }
}
