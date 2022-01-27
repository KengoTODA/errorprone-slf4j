plugins { id("com.gradle.enterprise") version "3.8.1" }

rootProject.name = "errorprone-slf4j"

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}
