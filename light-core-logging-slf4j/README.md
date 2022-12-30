# light-core-logger-slf4j

`slf4j` implementation of `LightLogger`.

If you are using a different version of `slf4j-api` or 
just don't want to import any more dependencies, 
you can simply copy the `LightSlf4jLogger` class to your project.

In most cases it will work fine.

To add dependencies on `light-core-logger-slf4j` using Maven, use the following:
```xml
<dependencies>
    <dependency>
        <groupId>space.lingu.light</groupId>
        <artifactId>light-core-logger-slf4j</artifactId>
        <version>0.4.1</version>
    </dependency>
</dependencies>
```

Or Gradle:
```groovy
dependencies {
    implementation("space.lingu.light:light-core-logger-slf4j:0.4.1")
}
```
