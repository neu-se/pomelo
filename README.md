# Pomelo

Pomelo allows existing parameterized JUnit tests to fuzzed without modification using configuration from Surefire and
Failsafe.

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

## Scanning for fuzzable tests

Pomelo can automatically scan a Maven build session to identify parameterized tests run by Surefire or Failsafe that
can be fuzzed by Pomelo without modification.
The scan checks whether a test can be run successfully with its original input values in isolation, in a forked JVM
created
in the same manner that Pomelo creates fuzzing JVMs.
The scan also checks whether generators are available for the types of the test's parameters.

To scan a Maven session, first add the Pomelo extension to your Maven build.
Then, invoke maven as you would normally to build your project adding the following options:

```
-Dpomelo.phase=scan [-Dpomelo.scan.report=<X>] [-Dpomelo.scan.timeout=<Y>]
```

Where:

* \<X\> is the path of the file to which the scan report should be written.
  The default value is ${project.build.directory}/pomelo-scan.csv.
* \<Y\> is the amount of time in seconds after which forked isolated Pomelo test processes should be killed.
  If set to 0, forked isolated Pomelo test processes are never timed out. The default value is 0.

Pomelo's scan creates a CSV report with one row for each detected parameterized test.
Each row has the following columns:

- project_id: identifier for the Maven project for which the test was run
- plugin_name: the plugin that ran the test, one of the following:
    - surefire
    - failsafe
- execution_id: unique identifier for the plugin execution in which the test was run
- test_class_name: fully qualified name of the test class
- test_method_name: name of the test method
- runner_class_name: fully qualified name of the JUnit runner used to run the test
- unambiguous: true if test_class_name and test_method_name unique identify a single valid method
- original_result: the result of the test when run by Surefire/Failsafe, one of the following:
    - PASSED: if none of the original test inputs failed due to reason other than a violated assumption
    - FAILED: if any of the original test inputs failed due to reason other than a violated assumption
- isolated_result: the result of the test when run in isolation by Pomelo, one of the following:
    - NONE: if the test was not run in isolation because it was ambiguous or originally failed
    - ERROR: if an error occurred while trying to run the test in isolation
    - TIMED_OUT: if the isolated test run timed out
    - PASSED: if none of the test inputs failed due to reason other than a violated assumption
    - FAILED: if any of the test inputs failed due to reason other than a violated assumption
- generators_status: the availability of generators for the test's parameters' types, one of the following:
    - UNKNOWN: if the test was not run in isolation or an error occurred while trying to run the test in isolation
    - MISSING: if a generator could not be found for any of test's parameters' types
    - PRESENT: if a generator could be found for all of test's parameters' types

## Fuzzing an existing test
```
-Dpomelo.phase=fuzz -Dpomelo.project=<J> -Dpomelo.execution=<E> -Dpomelo.plugin=<P> -Dpomelo.testClass=<C> -Dpomelo.testMethod=<M>
```