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
        <version>0.4.6</version>
        <scope>provided</scope>
    </dependency>

    <!-- Runtime Core Module -->
    <dependency>
        <groupId>space.lingu.light</groupId>
        <artifactId>light-core</artifactId>
        <version>0.4.6</version>
    </dependency>
</dependencies>
```

Or using Gradle:

```groovy
dependencies {
    compileOnly("space.lingu.light:light-compiler:0.4.6")

    implementation("space.lingu.light:light-core:0.4.6")
}
```

After adding dependencies, you can now use Light to help you build your database.

## Usage Example

Here is a simple example of using Light to build a database with tables.

### Define a table

Here defines a `User` table.
Each instance of `User` represents a column in the data table.

```java

@DataTable
public class User {
    @PrimaryKey
    @DataColumn
    private long uid;

    @DataColumn(name = "first_name")
    private String firstName;

    @DataColumn(name = "last_name")
    private String lastName;

    // ... setters and getters
}
```

Light uses the class name as the table name unless you specify the table name manually.

In this case, the table name is _User_.

Similar rules apply to column names. Light uses the field name as the column name
unless you specify the column name manually.

### Create a Data Access Object (DAO)

To access the data in the database, you need to create a DAO.

Here is a simple example for the `User` table.

```java

@Dao
public interface UserDao {
    @Insert
    void insert(User... users);

    @Update
    void update(User... users);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM User")
    List<User> get();

    @Query("SELECT * FROM User WHERE uid IN ({ids})")
    List<User> getByIds(int[] ids);
}
```

The `@Dao` annotation indicates that this is a DAO,
and the DAO class needs to be an interface or abstract class.

By use the `{}` to specify the SQL parameter,
Light will automatically replace the parameter with the corresponding value.

What's more, you can write a simple expression in the `{}`. Such as:

```java

@Query("SELECT * FROM User WHERE first_name = {user.getFirstName()} AND last_name = {user.getLastName()}")
List<User> getByName(User user);

```

> Currently, Light only supports field access and parameterless method calls in the expression.

### Create a database

After completing the above work, you also need to define a database class
to get the DAO instance.

Here is an example of a database class, it needs to be an abstract class
and extends `LightDatabase`.

```java

@Database(name = "example", version = 1, tables = {User.class})
public abstract class ExampleDatabase extends LightDatabase {
    public abstract UserDao getUserDao();
}

```

When connecting to the database, Light will try to create the database.
But if the database is already specified in the jdbc URL,
this step will be skipped.

After connecting to the database, Light will try to create tables 
that are not yet created. 

### Set up connection URL

When connecting to the database, you need to specify the connection URL
and also the jdbc driver class name in the `light.properties` file
(or you can specify the path in the `@Database` annotation) in the
classpath.

Here we use the MySQL database as an example.

```properties
light.data.url=jdbc:mysql://localhost:3306/
light.data.jdbcName=com.mysql.cj.jdbc.Driver
# Set your username and password if needed
light.data.username=root
light.data.password=123456
```

Or you can replace `data` with your database name, in this case is `example`.

### Last step, get ready to go!

To get the DAO instance, you need to build the database class instance.

Define in somewhere of your code:

```java
public static ExampleDatabase buildDatabase() {
    return Light.databaseBuilder(ExampleDatabase.class, MySQLDialectProvider.class)
            // This connection pool implementation is low performance and is used only as a test.
            // Can be replaced with Hikari Connection Pool, etc.
            .setConnectionPool(DisposableConnectionPool.class)
            // Optional, use light-core-logger-slf4j/LightSlf4jLogger as logger
            .setLogger(LightSlf4jLogger.createLogger(ExampleDatabase.class))
            .build();
}
```

Note: `DialectProvider` and `ConnectionPool` are must be specified.

We suggest that you use a singleton pattern to save the database instance.

## License

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
