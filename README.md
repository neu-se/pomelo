# Pomelo

Pomelo allows existing parameterized JUnit tests to fuzzed without modification using Surefire and Failsafe
configurations.

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
3. For Maven version 3.0.2+, add the option -Dmaven.ext.class.path=[path to pomelo-maven-lifecycle JAR] to your Maven command
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
created in the same manner that Pomelo creates fuzzing JVMs.
The scan also checks whether generators are available for the test's parameters' types.

To scan a Maven session, first add the Pomelo extension to your Maven build.
Then, invoke maven as you would normally to run tests adding the following options:

```
-Dpomelo.task=scan
[-Dpomelo.report=<X>]
[-Dpomelo.timeout=<Y>]
[-Dpomelo.verbose]
```

Where:

* \<X\> is the path of the file to which the scan report should be written.
  The default value is ${project.build.directory}/pomelo-scan.csv.
* \<Y\> is the amount of time in seconds after which forked isolated test JVMs should be killed.
  If set to 0, forked isolated test JVMs are never timed out. The default value is 0.
* The presence of -Dpomelo.verbose indicates that the standard output and error of the
  forked isolated test JVMs should be redirected to the standard out and error of the Maven process. By default, the
  standard output and error of the forked isolated test JVMs is discarded.

Pomelo's scan creates a CSV report with one row for each detected parameterized test.
Each row has the following columns:

- project_id: identifier (in the format groupId:artifactId:packaging:version) for the Maven project for which the test
  was run
- plugin: the plugin that ran the test, one of the following:
    - SUREFIRE
    - FAILSAFE
- execution_id: identifier for the plugin execution in which the test was run
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
    - PASSED: if none of the original test inputs failed due to reason other than a violated assumption
    - FAILED: if any of the original test inputs failed due to reason other than a violated assumption
- generators_status: the availability of generators for the test's parameters' types, one of the following:
    - UNKNOWN: if the test was not run in isolation or an error occurred while trying to run the test in isolation
    - MISSING: if a generator could not be found for at least one of test's parameters' types
    - PRESENT: if a generator could be found for all of test's parameters' types

If a parameterized test's isolated_result is PASSED and its generators_status is PRESENT, then Pomelo should be able to
fuzz the test without any modifications.

## Fuzzing an existing test

Pomelo can fuzz an existing parameterized test using the same configuration as Surefire or Failsafe for the
JVM use to run test.
The scan checks whether a test can be run successfully with its original input values in isolation, in a forked JVM
created in the same manner that Pomelo creates fuzzing JVMs.
The scan also checks whether generators are available for the test's parameters' types.

To fuzz an existing parameterized test, first add the Pomelo extension to your Maven build.
Then, invoke maven as you would normally to run the test adding the following options:

```
-Dpomelo.task=fuzz 
-Dpomelo.project=<J> 
-Dpomelo.plugin=<P> 
-Dpomelo.execution=<E> 
-Dpomelo.testClass=<C> 
-Dpomelo.testMethod=<M>
[-Dpomelo.duration=<D>]
[-Dpomelo.outputDirectory=<O>]
[-Dpomelo.maxTraceSize=<Z>]
[-Dpomelo.verbose]
[-D-Dpomelo.debug]
[-Dpomelo.timeout=<Y>]
[-Dpomelo.jacocoFormats=<F>]
```

Where:

* \<J\> is the identifier (in the format groupId:artifactId:packaging:version) of the Maven project whose test plugin
  configuration
  should be used.
* \<P\> is the test plugin whose configuration should be either, either SUREFIRE or FAILSAFE.
* \<E\> is identifier for the plugin execution whose configuration should be used.
* \<C\> is the fully-qualified name of the test class
* \<M\> is the name of the test method
* \<D\> is the maximum amount of time to execute the fuzzing campaign for specified in the ISO-8601 duration format (
  e.g., 2 days, 3 hours, and 4 minutes is "P2DT3H4M"). The default value is one day.
* \<O\> is the path of the directory to which the output files should be written.
  The default value is ${project.build.directory}/pomelo/fuzz/out.
* \<Z\> is the maximum number of frames to include in stack traces taken for failures. By default, a maximum of 5 frames
  are included.
* The presence of -Dpomelo.verbose indicates that the standard output and error of the
  forked analysis JVMs should be redirected to the standard out and error of the Maven process. By default, the
  standard output and error of forked analysis JVMs is discarded.
* The presence of -Dpomelo.debug indicates that forked analysis JVMs should suspend and wait for a debugger to attach
  on port 5005. By default, forked analysis JVMs do not suspend and wait for a debugger to attach.
* \<Y\> is the maximum amount of time in seconds to execute a single input during analysis or -1 if no timeout should be
  used. By default, a timeout value of 600 seconds is used.
* \<F\> is a list of JaCoCo report formats to be generated. The formats XML, HTML, CSV are supported. By default, all
  formats are generated.

## License

This software release is licensed under the BSD 3-Clause License.

Copyright (c) 2022, Katherine Hough and Jonathan Bell.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.