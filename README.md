# Light
[![Maven Central][mcBadge]][mcLink] [![License][liBadge]][liLink]

A simple and lightweight relational database development module.

## Required

JDK 1.8 or higher.

## Getting Started

To add dependencies on Light using Maven, use the following:
```xml
<dependencies>
    <!-- Code Generator Module -->
    <dependency>
        <groupId>space.lingu.light</groupId>
        <artifactId>light-compiler</artifactId>
        <version>0.3.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Runtime Core Module -->
    <dependency>
        <groupId>space.lingu.light</groupId>
        <artifactId>light-core</artifactId>
        <version>0.3.0</version>
    </dependency>
</dependencies>
```
Or using Gradle: 
```gradle
dependencies {
    compileOnly("space.lingu.light:light-compiler:0.3.0")
  
    implementation("space.lingu.light:light-core:0.3.0")
}
```

After adding dependencies, you can now use Light to help you build your database.

# License

```text
   Copyright (C) 2022 Lingu Light Project

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

[liBadge]: https://img.shields.io/github/license/Roll-W/light?color=569cd6&style=flat-square
[liLink]: https://github.com/Roll-W/light/blob/master/LICENSE
[mcBadge]: https://img.shields.io/maven-central/v/space.lingu.light/light-parent?style=flat-square
[mcLink]: https://search.maven.org/search?q=g:space.lingu.light
