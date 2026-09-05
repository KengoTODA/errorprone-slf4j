package jp.skypencil.errorprone.slf4j;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import java.io.IOException;
import org.junit.Test;

public class Slf4jIllegalPassedClassTest {
  @Test
  public void testSupportedVersion() {
    assertTrue(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("3.0.0"));
    assertTrue(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("2.11.0"));
    assertFalse(Slf4jIllegalPassedClass.MatherHolder.checkSupportedVersion("2.10.0"));
  }

  @Test
  public void testRefactoringInstanceField() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private final Logger logger = LoggerFactory.getLogger(String.class);
            }
            """)
        .addOutputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private final Logger logger = LoggerFactory.getLogger(getClass());
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringStaticField() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass());
    helper
        .addInputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static final Logger LOGGER = LoggerFactory.getLogger(String.class);
            }
            """)
        .addOutputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static final Logger LOGGER = LoggerFactory.getLogger(PrivateLogger.class);
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void test2ndFixForInstanceField() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass())
            .setFixChooser(FixChoosers.SECOND);
    helper
        .addInputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private final Logger logger = LoggerFactory.getLogger(String.class);
            }
            """)
        .addOutputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringInnerClass() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass())
            .setFixChooser(FixChoosers.SECOND);
    helper
        .addInputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static class InnerClass {
                private static final Logger LOGGER = LoggerFactory.getLogger(String.class);
              }
            }
            """)
        .addOutputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static class InnerClass {
                private static final Logger LOGGER = LoggerFactory.getLogger(PrivateLogger.class);
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testRefactoringInnerClass2() throws IOException {
    BugChecker checker = new Slf4jIllegalPassedClass();
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(checker, getClass())
            .setFixChooser(FixChoosers.FIRST);
    helper
        .addInputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static class InnerClass {
                private static final Logger LOGGER = LoggerFactory.getLogger(String.class);
              }
            }
            """)
        .addOutputLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static class InnerClass {
                private static final Logger LOGGER = LoggerFactory.getLogger(InnerClass.class);
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testOtherField() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "WithLoggerFactory.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.ILoggerFactory;
            import org.slf4j.LoggerFactory;

            public class WithLoggerFactory {
              private final String HELLO = "World";
              private final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
              private final Logger logger = LoggerFactory.getLogger("string");
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testClassWithoutProblem() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testInnerClassWithoutProblem() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private static class InnerClass {
                private static final Logger LOGGER = LoggerFactory.getLogger(InnerClass.class);
                private final Logger logger = LoggerFactory.getLogger(PrivateLogger.class);
              }
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }

  @Test
  public void testMethodParameter() throws IOException {
    CompilationTestHelper helper =
        CompilationTestHelper.newInstance(Slf4jIllegalPassedClass.class, getClass());
    helper
        .addSourceLines(
            "PrivateLogger.java",
            """
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;

            public class PrivateLogger {
              private void method(String string) {}
            }
            """)
        .expectNoDiagnostics()
        .doTest();
  }
}
