[![](https://jitpack.io/v/sethijatin/cucumber-jrc-maven-plugin.svg)](https://jitpack.io/#sethijatin/cucumber-jrc-maven-plugin)

# Cucumber JSON Reports Compiler Plugin

This plugin can be used to compile multiple JSON reports into a single JSON report. It makes sure that if a feature is spread across multiple physical feature files, then all the scenarios are combined under one head.

## How to include it in POM

```java

<pluginRepositories>
    <pluginRepository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </pluginRepository>
</pluginRepositories>

<plugin>
    <groupId>com.github.sethijatin</groupId>
    <artifactId>cucumber-jrc-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>compile-report</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <readJsonReportsFromFolder>src/test/JsonReports</readJsonReportsFromFolder> <!-- Change path as per project need -->
        <writeCompiledReportsToFolder>src/test/JsonReportsCompiled</writeCompiledReportsToFolder> <!-- Change path as per project need -->
    </configuration>
</plugin>
```
