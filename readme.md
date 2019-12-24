Auxo
------

Another file tagging app written in javaFx.

[![We recommend IntelliJ IDEA](http://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![](https://larryhsiao.com:9082/app/rest/builds/buildType:auxo_build/statusIcon.svg)](https://github.com/LarryHsiao/auxo)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)


Auxo will track the current directory as worksapce the tag can be attach to the same layer of directory.

Build
---

The project is only built with Java 11, older jdks are not tested.

This project have apply plugins to find javafx, just make sure you have right jdk to build this project. And note that in default setup, the workspace is at the project root. If you need to change it, run it in other directory. 

```shell script
./gradlew run
```

@todo #1 remove delete files in file tabel. 