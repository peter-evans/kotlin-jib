# Containerising Kotlin with Jib [<img align="right" alt="The blog of Peter Evans: Containerising Kotlin with Jib" title="View blog post" src="https://peterevans.dev/img/blog-published-badge.svg">](https://peterevans.dev/posts/containerising-kotlin-with-jib/)
[![CircleCI](https://circleci.com/gh/peter-evans/kotlin-jib/tree/master.svg?style=svg)](https://circleci.com/gh/peter-evans/kotlin-jib/tree/master)

Writing a `Dockerfile` to containerise an application can often be a non-trivial task. Many times I've spent hours fiddling with different base images and configurations and never being quite satisfied with the result. Well I recently tried [Jib](https://github.com/GoogleContainerTools/jib), one of Google's container tools, and I love it! It builds optimised Docker and [Open Container Initiative (OCI)](https://github.com/opencontainers/image-spec) spec images for JVM applications. For containerising JVM apps I will definitely try and use Jib where possible in future.

## Jib images are distroless!

I had been interested in trying [Google's "Distroless" Docker images](https://github.com/GoogleContainerTools/distroless) for a while, and so it was great to read that Jib uses these as base images by default. "Distroless" images are stripped down to the bare essentials. They contain only the application and its runtime dependencies. There is no bloat from unnecessary programs, shells, package managers, etc.

## Jib images are reproducible

The layers, metadata, files and directories are all added to the image by Jib in a consistent order. Additionally, timestamps of all files and directories are set to one second after the Epoch, and the image creation time is set exactly to the Epoch. This makes the image build process deterministic and able to rebuild layers so that they have the exact same digest each time.

Don't be surprised that it reports the image was created 49+ years ago⁠—it's for reproducibility!
```bash
REPOSITORY                        TAG                 IMAGE ID            CREATED             SIZE
peterevans/webservice             0.1                 493233af1b87        49 years ago        137MB
peterevans/webservice             0.1.0               493233af1b87        49 years ago        137MB
peterevans/webservice             latest              493233af1b87        49 years ago        137MB
```

The result is that Jib produces lean, efficient and reproducible images that have a number of benefits.
- An accurate diff of changes and provenance between image releases becomes much less of a burden
- Reduced attack surface
- Improves the signal to noise ratio of vulnerability scanners

## Using Jib with Gradle's Kotlin DSL

Here is a quick introduction of how to use Jib with Gradle's Kotlin DSL. A complete example project is contained in this repository.

First add the plugin to `build.gradle.kts`
```kotlin
plugins {
    id("com.google.cloud.tools.jib") version "2.3.0"
}
```

Add a configuration section for Jib to `build.gradle.kts` specifying the image name. Don't forget to prefix with the registry host for pushing to registries other than `io.dockerhub`.
```kotlin
jib {
    to {
        image = "peterevans/webservice"
    }
}
```

The `jib` gradle task will build and push to the registry. You might also need to specify an [authentication method](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin#authentication-methods). This is the preferred way to build jib images as it is daemonless. There is no requirement for your CI environment to be running the Docker daemon.

```bash
gradle jib
```

The `jibDockerBuild` gradle task will build the image and load it into the Docker daemon. This can be useful if during your CI release process you want to [smoke test](https://peterevans.dev/posts/smoke-testing-containers/) the image after being built but before being pushed to the registry.
```bash
gradle jibDockerBuild
```

## Image Configuration

### Tags

By default, Jib builds every image with no tag, meaning it will always produce an image with the default tag `latest`. A set of additional tags can be added under `jib.to.tags`. The following example tags the image with a `major.minor` version and a `major.minor.build` version.

```kotlin
version = "0.1"
val buildNumber by extra("0")

jib {
    to {
        image = "peterevans/webservice"
        tags = setOf("$version", "$version.${extra["buildNumber"]}")
    }
}
```

### Labels
Labels can be specified as a map under `jib.container.labels` as follows.
```kotlin
jib {
    container {
        labels = mapOf(
            "maintainer" to "Peter Evans <mail@peterevans.dev>",
            "org.opencontainers.image.title" to "webservice",
            "org.opencontainers.image.description" to "An example webservice",
            "org.opencontainers.image.version" to "$version",
            "org.opencontainers.image.authors" to "Peter Evans <mail@peterevans.dev>",
            "org.opencontainers.image.url" to "https://github.com/peter-evans/kotlin-jib",
            "org.opencontainers.image.vendor" to "https://peterevans.dev",
            "org.opencontainers.image.licenses" to "MIT"
        )
    }
}
```

### Further Container Configuration

You can customise almost everything about the image including JVM flags, environment variables, volumes, ports, etc. Further configuration options can be found in the documentation [here](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin).

```kotlin
jib {
    container {
        jvmFlags = listOf(
            "-server",
            "-Djava.awt.headless=true",
            "-XX:InitialRAMFraction=2",
            "-XX:MinRAMFraction=2",
            "-XX:MaxRAMFraction=2",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=100",
            "-XX:+UseStringDeduplication"
        )
        environment = mapOf(
            "USERNAME" to "user1",
            "PASSWORD" to "1234")
        workingDirectory = "/webservice"
        volumes = listOf("/data")
        ports = listOf("8080")
        args = listOf("--help")
    }
}
```

See the code in this repository for a more complete example of using Jib to containerise Kotlin.

## License

MIT License - see the [LICENSE](LICENSE) file for details
