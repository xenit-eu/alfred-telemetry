# Alfred Telemetry for Alfresco

[![Maven Central](https://img.shields.io/maven-central/v/eu.xenit.alfred.telemetry/alfred-telemetry-platform.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22eu.xenit.alfred.telemetry%22%20AND%20a%3A%22alfred-telemetry-platform%22)

Alfred Telemetry integrates Alfresco and [Micrometer](https://micrometer.io/), an application metrics facade that
supports numerous monitoring systems.

To learn more about Micrometer’s capabilities, please refer to its [reference documentation](https://micrometer.io/docs)
, in particular the [concepts section](https://micrometer.io/docs/concepts).

![Grafana Dashboard](docs/images/grafanana.png)

## Usage

Alfred Telemetry is available in Maven Central both as an
[Alfresco Simple Module](https://docs.alfresco.com/6.1/concepts/dev-extensions-packaging-techniques-jar-files.html) and as an
[Alfresco Module Package](https://docs.alfresco.com/6.1/concepts/dev-extensions-packaging-techniques-amps.html).

Depending on the technique or build system used, you can include this artifacts as an extension in your
distribution of the Alfresco Platform.

### Gradle

The [Alfresco Docker Gradle Plugins](https://github.com/xenit-eu/alfresco-docker-gradle-plugin) can be
used to build an Alfresco docker image with Alfred Telemetry installed:

```groovy
// Alfresco Simple Module: 
alfrescoSM "eu.xenit.alfred.telemetry:alfred-telemetry-platform:${last-version}"
// Alfresco Module Package:
alfrescoAmp "eu.xenit.alfred.telemetry:alfred-telemetry-platform:${last-version}@amp"
```

### Maven

If you are using the Alfresco Maven SDK, the same artifacts can be used to install Alfred Telemetry
in your distribution:

```xml
<!-- Alfresco Simple Module: -->
<dependency>
    <groupId>eu.xenit.alfred.telemetry</groupId>
    <artifactId>alfred-telemetry-platform</artifactId>
    <version>${last-version}</version>
</dependency>
<!-- Alfresco Module Package: -->
<dependency>
    <groupId>eu.xenit.alfred.telemetry</groupId>
    <artifactId>alfred-telemetry-platform</artifactId>
    <version>${last-version}</version>
    <type>amp</type>
</dependency>
```

### Manual download and install
Please consult the official Alfresco documentation on how to install Simple Modules and Module Packages manually.

### Supported Alfresco versions

Alfred Telemetry is systematically integration tested against Alfresco 5.2, 6.0, 6.1 and 6.2.
Furthermore the extension is also known to work on Alfresco 5.0 and 5.1.


## Configuration

By default, Alfred Telemetry exposes all metrics on
[the `alfresco/s/alfred/telemetry/metrics` endpoint](docs/README.md#metrics-endpoint). If
metrics should be exported to a specific monitoring system, the corresponding
`micrometer-registry-${monitoring-system}` should be included in the classpath of Alfresco. For a detailed
description have a look at the [relevant documentation](docs/README.md#supported-monitoring-systems).

Once the desired monitoring systems are configured, out of the box metrics are available and custom
metrics can be very easily exposed with minimal lines of code:

```java
public class SampleBean {

    private final Counter counter;

    public SampleBean(MeterRegistry registry) {
        this.counter = registry.counter("received.messages");
    }

    public void handleMessage(String message) {
        this.counter.increment();
        // handle message implementation
    }

}
```

## Documentation

How to setup metrics exposure to different monitoring systems and how to create custom metrics, is described
in the [project's documentation](docs)


## Contributing

These instructions will get you a copy of the project up and running on your local machine for development and
testing purposes.

### Prerequisites

The Alfred Telemetry Alfresco extension **compiles** against Alfresco Enterprise Edition because:
* it includes metrics for some enterprise components (e.g. Hazelcast cache)
* it contains an implementation of the `MetricsController` interface, which is only available since AEE 6.1.

> Please note that although Alfred Telemetry compiles against AEE, it **does work on ACE**. Necessary abstractions
> are made to hide enterprise only features when running on community edition.

This means access to the [private Alfresco Nexus repository](https://artifacts.alfresco.com/nexus/content/groups/private)
is required to build this project. To access this repository, the following two Gradle properties need to
be present when the build is executed:

```properties
org.alfresco.maven.nexus.username
org.alfresco.maven.nexus.password
```

Gradle provides several mechanisms to provide these properties, which are thoroughly described in the
[official Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties).

### Local build

The Gradle `assemble` task can be used to build the Simple Module and AMP artifact:

```
./gradlew assemble
```

After execution, the artifacts will be available in the `build/libs` and `build/dist` directories.

### Running tests

Unit tests can be executed with the Gradle `test` task:

```
./gradlew test
```

The projects also includes integration tests which startup and test
an Alfresco with the Alfred Telemetry extension installed.

To execute all the available integration tests for community versions, following Gradle task can be used:

```
./gradlew integrationTest -Pcommunity
```

Leaving out the `-Pcommunity` will also trigger the Alfresco Enterprise integration tests. You need extra credentials to
make that work.
To only run the integration tests for a specific Alfresco version, execute the task in the corresponding subproject:

```
./gradlew :integration-tests:alfresco-community-61:integrationTest
```