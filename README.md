#Introduction

The Java Source and Documentation Lookup is a command line tool created for looking up source files and Javadoc with ease. This application is based on the idea of a [`Reference`](https://github.com/thatJavaNerd/JSaDL/blob/master/src/net/dean/jsadl/Reference.java). A reference is an object that contains the path or URL to the base source and Javadoc directory for a JDK. For example, I could have a Reference with a source directory of `$JAVA_HOME/src` and a Javadoc directory of `http://docs.oracle.com/javase/7/docs/api/`.

References are specified in [INI files](en.wikipedia.org/wiki/INI_file). Each Section of the INI file represents a Reference. You can specify the source path by adding a key called `src` and the documentation by adding a key called `doc`

For example: This is a valid Reference:

```ini
[alt_jdk]
src=/usr/lib/jvm/jdk1.7.0_25/src/
doc=http\://docs.oracle.com/javase/7/docs/api/
```

>**N.B**: When using URLs, make sure to escape the colon with a backslash. This is necessary because INI properties can be assigned using the equals sign *OR* the colon. For a full list of escape characters, see [here](http://en.wikipedia.org/wiki/INI_file#Escape_characters)

By default, JSaDL will look for References in a file called `config.ini` in the place where the jar file is executed. If you want to change that, you can use the `--config=<file>` parameter.

#Usage

```bash
java -jar <jarfile> [--help] <classname> [-s | --source] [--lookup=<reference>] [--viewer=<app>] [--nocheck]
```

###Parameters:

* `--help`: Displays a help message
* `classname`: The name of the Java class you want to look up
* `-s`,  `--source`: Look up source instead of Javadoc
* `--lookup=<reference>`: Specifies the reference to use. If none is specified, `java` is used
* `--config=<file>`: Changes where JSaDL will look for References
* `--viewer=<app>`: Specifies the application that will be used to view the source/documentation
* `--nocheck`: Disables checking for existing source files or a 200 HTTP response code for online documentation

###Examples:

View the help documentation:

    java -jar <jarname> --help

Look up the Javadoc of `java.lang.Object`:

    java -jar <jarname> java.lang.Object
    
Look up the source of `java.lang.Object`:

    java -jar <jarname> java.lang.Object -s
    
Look up the Javadoc of `java.lang.Object` with vim:

    java -jar <jarname> java.lang.Object --viewer=vim
    
Look up source of `java.lang.Object` with a [`Reference`](https://github.com/thatJavaNerd/JSaDL/blob/master/src/net/dean/jsadl/Reference.java) named `alt_jdk`

    java -jar <jarname> java.lang.Object --lookup=alt_jdk

###Exit codes

* `0`: Terminated normally
* `1`: No class specified
* `3`: `IOException` while sending a HTTP/GET request to check the availability of a document
* `10`: An unknown protocol was specified (not http/https or a local file)
* `11`: The document could not be found (http[s])
* `12`: The document could not be found (local file)
* `20`: No `config.ini` file found or no file specified with `--config=<file>`
* `21`: An IOException occured while reading the file
* `22`: Bad INI file syntax
    
#Building from source with Apache Ant

If you don't have Ant, you can view directions on how to install it [here](http://ant.apache.org/manual/install.html).

1. First, clone the repo. For Git 1.6.5+, use `git clone --recursive https://github.com/thatJavaNerd/JSaDL.git`. For older versions, use `git clone https://github.com/thatJavaNerd/JSaDL.git`. Then, go into the `JSaDL` directory, and run these two commands: `git submodule init`, `git submodule update`.
2. Run the command `ant`. The jar is now located at `dist/jsadl.jar`.

If for some reason you only want to build DeanCommons, use the command `ant build_dean_commons`.

#Setting up for Eclipse

1. `cd` into your workspace directory and run `git clone --recursive https://github.com/thatJavaNerd/JSaDL`.
2. Create a new Java project using Eclipse for JSaDL using the same directory that it was cloned into.