# light-core-connection-hikari

Hikari Connection Pool implementation of ConnectionPool.

To be compatible with JDK 1.8, this module uses version 4.0.3 of HikariCP. 

Do not add dependency of this module if you are using a newer version (5.x.x) of HikariCP.

> May be will upgrade to JDK 11 later.

If you are using a different version of `HikariCP` or just 
don't want to import any more dependencies, you can simply copy
the class `HikariConnectionPool` to your project.

In the most cases it will work fine.
