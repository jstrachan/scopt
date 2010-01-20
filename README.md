scopt
=====

scopt is a little command line options parsing library.

Its based on the code from Aaron Harnly mentioned [http://old.nabble.com/-scala--CLI-library--ts19391923.html#a19391923](in this thread)
which is available [http://gist.github.com/246481](as a gist) or [http://harnly.net/tmp/OptionsParser.scala](here).


Usage
-----

Create an *OptionParser* and customise it with the options you need, passing in functions to process each option or argument.

    val parser = new OptionParser {
      intOpt("f", "foo", "foo is an integer property", {v: Int => config.foo = v})
      opt("b", "bar", "bar is a string property", {v: String => config.bar = v})
      booleanOpt("x", "xyz", "xyz is a boolean property", {v: Boolean => config.xyz = v})
      arg("whatnot", "some argument", {v: String => config.whatnot = v})
    }
    if (parser.parse(args)) {
       // do stuff
    }
    else {
      // arguments are bad, usage message will have been displayed
    }


Building
--------

You should be able to use either [http://maven.apache.org](maven) or [http://code.google.com/p/simple-build-tool/](sbt) to build scopt.

  