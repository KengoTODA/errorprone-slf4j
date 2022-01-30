1. Confirm that we use Java 11 to build by `mvn -v`
2. Confirm that we can sign artifacts by gpg by `mvn package gpg:sign -P release`
3. Update the version of prject in `CHANGELOG.md` and `pom.xml` to stable version, and commit the change
4. Run `mvn clean deploy -P release`
5. Update the version of prject in `CHANGELOG.md` and `pom.xml` to SNAPSHOT version, and commit the change
