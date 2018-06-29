# Find and fix misusage of SLF4J with Google Error Prone

[![Build Status](https://travis-ci.com/KengoTODA/errorprone-slf4j.svg?branch=master)](https://travis-ci.com/KengoTODA/errorprone-slf4j)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jp.skypencil.errorprone.slf4j%3Aerrorprone-slf4j&metric=alert_status)](https://sonarcloud.io/dashboard?id=jp.skypencil.errorprone.slf4j%3Aerrorprone-slf4j)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/jp.skypencil.errorprone.slf4j/errorprone-slf4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/jp.skypencil.errorprone.slf4j/errorprone-slf4j)

## Install

After [installation of Error Prone](https://errorprone.info/docs/installation), introduce errorprone-slf4j as plugin. Refer [official document](https://errorprone.info/docs/plugins#build-system-support) or following examples:

### Maven

Add `<annotationProcessorPaths>` into configuration of `maven-compiler-plugin` like below:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <compilerId>javac-with-errorprone</compilerId>
    ...
    <annotationProcessorPaths>
      <path>
        <groupId>jp.skypencil.errorprone.slf4j</groupId>
        <artifactId>errorprone-slf4j</artifactId>
        <version>0.1.0-SNAPSHOT</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

### Gradle

From v4.6, [Gradle supports `annotationProcessor` configuration](https://docs.gradle.org/4.6/release-notes.html#convenient-declaration-of-annotation-processor-dependencies) so you can configure your project like below:

```groovy
dependencies {
  annotationProcessor 'jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.0-SNAPSHOT'
}
```

## Copyright

Copyright 2012-2017 Kengo TODA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
