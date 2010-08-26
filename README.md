scopt
=====

scopt is a little command line options parsing library.

Its based on the code from Tim Perrett which his based on Aaron Harnly's code
mentioned [in this thread](http://old.nabble.com/-scala--CLI-library--ts19391923.html#a19391923) and
[this thread](http://old.nabble.com/Parsing-command-lines-argument-in-a-%22scalaesque%22-way-tp26592006p26595257.html)
which is available [as a gist](http://gist.github.com/246481) or [here](http://harnly.net/tmp/OptionsParser.scala).


Usage
-----

Create an *OptionParser* and customise it with the options you need, passing in functions to process each option or argument.

    val parser = new OptionParser("scopt") {
      intOpt("f", "foo", "foo is an integer property", {v: Int => config.foo = v})
      opt("o", "output", "<file>", "output is a string property", {v: String => config.bar = v})
      booleanOpt("x", "xyz", "xyz is a boolean property", {v: Boolean => config.xyz = v})
      keyValueOpt("l", "lib", "<libname>", "<filename>", "load library <libname>",
        {(key: String, value: String) => { config.libname = key; config.libfile = value } })
      arg("<singlefile>", "<singlefile> is an argument", {v: String => config.whatnot = v})
      // arglist("<file>...", "arglist allows variable number of arguments",
      //   {v: String => config.files = (v :: config.files).reverse })
    }
    if (parser.parse(args)) {
       // do stuff
    }
    else {
      // arguments are bad, usage message will have been displayed
    }

The above generates the following usage text:

    Usage: scopt [options] <filename>
    
      -f <value> | --foo <value>
            foo is an integer property
      -o <file> | --output <file>
            output is a string property
      -x <value> | --xyz <value>
            xyz is a boolean property
      -l:<libname>=<filename> | --lib:<libname>=<filename>
            load library <libname>
      <singlefile>
            <singlefile> is an argument

Building
--------

You should be able to use either [maven](http://maven.apache.org) or [sbt](http://code.google.com/p/simple-build-tool/) to build scopt.


License
-------

Do whatever you like with it :)

Changes
-------

* added -x:key=value option 
* updated usage text style 
* updated sbt build to build against 2.8.0.Beta1
* added maven and sbt builds
* added ScalaTest test cases
* added arguments which then are displayed in help
* minor refactoring of names; opt and arg for options and args
* updated sbt and maven to build against Scala 2.8.0 

