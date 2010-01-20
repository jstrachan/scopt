package org.github.scopt


import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite

/**
 * Tests the use of the options parser
 *
 * @version $Revision : 1.1 $
 */

case class Config(var foo: Int = -1, var bar: String = null, var xyz: Boolean = false, var whatnot: String = null)

@RunWith(classOf[JUnitRunner])
class OptionsTest extends FunSuite {
  var config: Config = _

  val parser = new OptionParser {
    onInt("f", "foo", "foo is an integer property", {v: Int => config.foo = v})
    on("b", "bar", "bar is a string property", {v: String => config.bar = v})
    onBoolean("x", "xyz", "xyz is a boolean property", {v: Boolean => config.xyz = v})
    arg("whatnot", "some argument", {v: String => config.whatnot = v})
  }

  test("valid arguments are parsed correctly") {
    validArguments(Config(whatnot = "blah"), "blah")
    validArguments(Config(foo = 35, whatnot = "abc"), "-f", "35", "abc")
    validArguments(Config(foo = 22, bar = "beer", whatnot = "drink"), "-b", "beer", "-f", "22", "drink")
    validArguments(Config(foo = 22, bar = "beer", whatnot = "drink"), "-f", "22", "--bar", "beer", "drink")
  }

  test("invalid arguments fail") {
    invalidArguments()
    invalidArguments("-z", "blah")
    invalidArguments("blah", "blah")
    invalidArguments("-z", "abc", "blah")
    invalidArguments("-f", "22", "-z", "abc", "blah")
  }

  test("bad numbers fail to parse nicely") {
    invalidArguments("-f", "shouldBeNumber", "blah")
  }

  test("bad booleans fail to parse nicely") {
    invalidArguments("-x", "shouldBeBoolean", "blah")
  }

  def validArguments(expectedConfig: Config, args: String*) {
    config = new Config()
    expect(true) {
      parser.parse(args)
    }

    expect(expectedConfig) {
      config
    }
  }

  def invalidArguments(args: String*) {
    config = new Config()
    expect(false) {
      parser.parse(args)
    }
  }
}