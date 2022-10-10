# Pomelo

Support for fuzzing parameterized JUnit tests.

```
-Dpomelo.phase=scan [-Dpomelo.scan.report=<X>]
```

```
-Dpomelo.phase=fuzz -Dpomelo.project=<J> -Dpomelo.execution=<E> -Dpomelo.plugin=<P> -Dpomelo.testClass=<C> -Dpomelo.testMethod=<M>
```

## Adding the Pomelo extension to your Maven build

There are four ways you can add the Pomelo extension to your Maven build

1. Install a copy of the pomelo-maven-lifecycle JAR into ${maven.home}/lib/ext
2. Add pomelo-maven-lifecycle as an extension in your pom:
   ```
   <build>
       ...
       <extensions>
           ...
           <extension>
               <groupId>edu.neu.ccs.prl.pomelo</groupId>
               <artifactId>pomelo-maven-lifecycle</artifactId>
               <version>1.0.0-SNAPSHOT</version>
           </extension>
           ...
       </extensions>
       ...
   </build>
   ```
3. For Maven version 3.0.2+, -Dmaven.ext.class.path=[path to pomelo-maven-lifecycle JAR]
4. For Maven version 3.3.1+, configure your extension in ${maven.projectBasedir}/.mvn/extensions.xml
   ```
   <extensions xmlns="https://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="https://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="https://maven.apache.org/EXTENSIONS/1.0.0 https://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
       <extension>
           <groupId>edu.neu.ccs.prl.pomelo</groupId>
           <artifactId>pomelo-maven-lifecycle</artifactId>
           <version>1.0.0-SNAPSHOT</version>
       </extension>
   </extensions>
   ```
