Introduction
=====

The Java Source and Documentation Lookup is a simple command line tool created for looking up source files and Javadoc with ease. This application is based on the idea of a [`Reference`](https://github.com/thatJavaNerd/JSaDL/blob/master/src/net/dean/jsadl/Reference.java). A reference is an object that contains the path or URL to the base source and Javadoc directory for a JDK. For example, I could have a Reference with a source directory of `$JAVA_HOME/src` and a Javadoc directory of `http://docs.oracle.com/javase/7/docs/api/`.

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
java -jar <jarfile> [--help] <classname> [-s | --src] [--lookup=<reference>] [--viewer=<app>] [--nocheck]
```

###Parameters:

* `--help`: Displays a help message
* `classname`: The name of the Java class you want to look up
* `-s`,  `--src`: Look up source instead of Javadoc
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
    
###Exit Codes:

* `0`: Terminated normally
* `1`: No class specified
* `2`: No `config.ini` file found or no file specified with `--config=<file>`
* `3`: `IOException` while sending a HTTP/GET request to check the availability of a document
* `10`: An unknown protocol was specified (not http/https or a local file)
* `11`: The document could not be found (http[s])
* `12`: The document could not be found (local file)
