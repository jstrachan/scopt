package scopt

import collection.mutable.ListBuffer

// Base class for options.
// These are things that get listed when we ask for help,
// and optionally can accept string arguments & perform some kind of action,
// usually mutating a var.
private case class OptionDefinition(
        canBeInvoked: Boolean,
        shortopt: Option[String],
        longopt: String,
        keyName: String,
        valueName: String,
        description: String,
        action: String => Unit,
        gobbleNextArgument: Boolean,
        keyValueArgument: Boolean,
        minOccurs: Int,
        maxOccurs: Int) {
  def shortDescription = "option " + longopt
}

// ----- Some standard option types ---------
private class SeparatorDefinition(
        description: String
        ) extends OptionDefinition(false, null, null, null, null,
          description, {a: String => {}}, false, false, 1, 1)

private class Argument(
        name: String,
        description: String,
        minOccurs: Int,
        maxOccurs: Int,
        action: String => Unit
        ) extends OptionDefinition(false, null, name, null, name, 
          description, action, false, false, minOccurs, maxOccurs) {

  override def shortDescription = "argument " + name
}

private class ArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        valueName: String,
        description: String,
        action: String => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, valueName,
          description, action, true, false, 0, OptionParser.UNBOUNDED)

private class IntArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        valueName: String,
        description: String,
        action: Int => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, valueName,
          description, {a: String => action(a.toInt)}, true, false, 0, OptionParser.UNBOUNDED)

private class DoubleArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        valueName: String,
        description: String,
        action: Double => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, valueName,
          description, {a: String => action(a.toDouble)}, true, false, 0, OptionParser.UNBOUNDED)

private class BooleanArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        valueName: String,
        description: String,
        action: Boolean => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, valueName, 
          description, { a: String =>
    val boolValue = a.toLowerCase match {
      case "true" => true
      case "false" => false
      case "yes" => true
      case "no" => false
      case "1" => true
      case "0" => false
      case _ =>
        throw new IllegalArgumentException("Expected a string I can interpret as a boolean")
    }
    action(boolValue)},
    true, false, 0, OptionParser.UNBOUNDED)
    
private object KeyValueParser {
  def split(s: String): (String, String) = s.indexOf('=') match {
    case -1     => throw new IllegalArgumentException("Expected a key=value pair")
    case n: Int => (s.slice(0, n), s.slice(n + 1, s.length))
  }
}

private class KeyValueArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        keyName: String,
        valueName: String,
        description: String,
        action: (String, String) => Unit
        ) extends OptionDefinition(true, shortopt, longopt, keyName, valueName, 
          description, { a: String => action(KeyValueParser.split(a)._1, KeyValueParser.split(a)._2) },
          false, true, 0, OptionParser.UNBOUNDED)

private class KeyIntValueArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        keyName: String,
        valueName: String,
        description: String,
        action: (String, Int) => Unit
        ) extends OptionDefinition(true, shortopt, longopt, keyName, valueName, 
          description, { a: String => action(KeyValueParser.split(a)._1, KeyValueParser.split(a)._2.toInt) },
          false, true, 0, OptionParser.UNBOUNDED)

private class KeyDoubleValueArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        keyName: String,
        valueName: String,
        description: String,
        action: (String, Double) => Unit
        ) extends OptionDefinition(true, shortopt, longopt, keyName, valueName, 
          description, { a: String => action(KeyValueParser.split(a)._1, KeyValueParser.split(a)._2.toDouble) },
          false, true, 0, OptionParser.UNBOUNDED)

private class KeyBooleanValueArgOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        keyName: String,
        valueName: String,
        description: String,
        action: (String, Boolean) => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, valueName, 
          description, { a: String => 
            val x = KeyValueParser.split(a)
            val key = x._1
            val boolValue = x._2.toLowerCase match {
              case "true" => true
              case "false" => false
              case "yes" => true
              case "no" => false
              case "1" => true
              case "0" => false
              case _ =>
                throw new IllegalArgumentException("Expected a string I can interpret as a boolean")
            }
            action(key, boolValue)
          },
          false, true, 0, OptionParser.UNBOUNDED)
      
private class FlagOptionDefinition(
        shortopt: Option[String],
        longopt: String,
        description: String,
        action: => Unit
        ) extends OptionDefinition(true, shortopt, longopt, null, null,
          description, {a: String => action}, false, false, 0, OptionParser.UNBOUNDED)

/** OptionParser is instantiated within your object,
 * set up by an (ordered) sequence of invocations of 
 * the various builder methods such as
 * <a href="#opt(String,String,String,String,(String) ⇒ Unit):Unit"><code>opt</code></a> method or
 * <a href="#arg(String,String,(String) ⇒ Unit):Unit"><code>arg</code></a> method.
 * {{{
 * val parser = new OptionParser("scopt") {
 *   intOpt("f", "foo", "foo is an integer property", {v: Int => config.foo = v})
 *   opt("o", "output", "<file>", "output is a string property", {v: String => config.bar = v})
 *   booleanOpt("xyz", "xyz is a boolean property", {v: Boolean => config.xyz = v})
 *   keyValueOpt("l", "lib", "<libname>", "<filename>", "load library <libname>",
 *     {(key: String, value: String) => { config.libname = key; config.libfile = value } })
 *   arg("<singlefile>", "<singlefile> is an argument", {v: String => config.whatnot = v})
 *   // arglist("<file>...", "arglist allows variable number of arguments",
 *   //   {v: String => config.files = (v :: config.files).reverse })
 * }
 * if (parser.parse(args)) {
 *   // do stuff
 * }
 * else {
 *   // arguments are bad, usage message will have been displayed
 * }
 * }}}
 */
case class OptionParser(
        programName: Option[String],
        version: Option[String],
        errorOnUnknownArgument: Boolean) {
  def this() = this(None, None, true)
  def this(programName: String) = this(Some(programName), None, true)
  def this(programName: String, version: String) = this(Some(programName), Some(version), true)
  def this(errorOnUnknownArgument: Boolean) = this(None, None, errorOnUnknownArgument)
  def this(programName: String, errorOnUnknownArgument: Boolean) =
    this(Some(programName), None , errorOnUnknownArgument)

  private val options = new ListBuffer[OptionDefinition]
  private val arguments = new ListBuffer[Argument]
  private val NL = System.getProperty("line.separator")
  private val TB = "        "
  private val NLTB = NL + TB
  private val NLNL = NL + NL
  private val defaultKeyName = "<key>"
  private val defaultValueName = "<value>"
  private var argList: Option[Argument] = None 
  
  // -------- Defining options ---------------
  private def add(option: OptionDefinition) {
    option match {
      case a: Argument if a.maxOccurs > 1 => argList = Some(a)
      case a: Argument => arguments += a
      case _ => options += option
    }
  }

  /** adds a `String` option invoked by `-shortopt x` or `--longopt x`.
   * @param shortopt short option
   * @param longopt long option
   * @param description description in the usage text
   * @param action callback function
   */
  def opt(shortopt: String, longopt: String, description: String, action: String => Unit) =
    add(new ArgOptionDefinition(Some(shortopt), longopt, defaultValueName, description, action))

  /** adds a `String` option invoked by `--longopt x`.
   * @param longopt long option
   * @param description description in the usage text
   * @param action callback function
   */
  def opt(longopt: String, description: String, action: String => Unit) =
    add(new ArgOptionDefinition(None, longopt, defaultValueName, description, action))

  /** adds a `String` option invoked by `-shortopt x` or `--longopt x`.
   * @param shortopt short option  
   * @param longopt long option
   * @param valueName value name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */      
  def opt(shortopt: String, longopt: String, valueName: String,
      description: String, action: String => Unit) =
    add(new ArgOptionDefinition(Some(shortopt), longopt, valueName, description, action))

  /** adds a `String` option invoked by `-shortopt x` or `--longopt x`.
   * @param shortopt short option, or `None`  
   * @param longopt long option
   * @param valueName value name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */  
  def opt(shortopt: Option[String], longopt: String, valueName: String,
      description: String, action: String => Unit) =
    add(new ArgOptionDefinition(shortopt, longopt, valueName, description, action))

  /** adds a flag option invoked by `-shortopt` or `--longopt`.
   * @param shortopt short option
   * @param longopt long option
   * @param description description in the usage text
   * @param action callback function
   */      
  def opt(shortopt: String, longopt: String, description: String, action: => Unit) =
    add(new FlagOptionDefinition(Some(shortopt), longopt, description, action))

  /** adds a flag option invoked by `--longopt`.
   * @param longopt long option
   * @param description description in the usage text
   * @param action callback function
   */
  def opt(longopt: String, description: String, action: => Unit) =
    add(new FlagOptionDefinition(None, longopt, description, action))
      
  // we have to give these typed options separate names, because of &^@$! type erasure
  def intOpt(shortopt: String, longopt: String, description: String, action: Int => Unit) =
    add(new IntArgOptionDefinition(Some(shortopt), longopt, defaultValueName, description, action))

  def intOpt(longopt: String, description: String, action: Int => Unit) =
    add(new IntArgOptionDefinition(None, longopt, defaultValueName, description, action))
      
  def intOpt(shortopt: String, longopt: String, valueName: String,
      description: String, action: Int => Unit) =
    add(new IntArgOptionDefinition(Some(shortopt), longopt, valueName, description, action))

  def intOpt(shortopt: Option[String], longopt: String, valueName: String,
      description: String, action: Int => Unit) =
    add(new IntArgOptionDefinition(shortopt, longopt, valueName, description, action))
      
  def doubleOpt(shortopt: String, longopt: String, description: String, action: Double => Unit) =
    add(new DoubleArgOptionDefinition(Some(shortopt), longopt, defaultValueName, description, action))

  def doubleOpt(longopt: String, description: String, action: Double => Unit) =
    add(new DoubleArgOptionDefinition(None, longopt, defaultValueName, description, action))
      
  def doubleOpt(shortopt: String, longopt: String, valueName: String,
      description: String, action: Double => Unit) =
    add(new DoubleArgOptionDefinition(Some(shortopt), longopt, valueName, description, action))

  def doubleOpt(shortopt: Option[String], longopt: String, valueName: String,
      description: String, action: Double => Unit) =
    add(new DoubleArgOptionDefinition(shortopt, longopt, valueName, description, action))
    
  def booleanOpt(shortopt: String, longopt: String, description: String, action: Boolean => Unit) =
    add(new BooleanArgOptionDefinition(Some(shortopt), longopt, defaultValueName, description, action))

  def booleanOpt(longopt: String, description: String, action: Boolean => Unit) =
    add(new BooleanArgOptionDefinition(None, longopt, defaultValueName, description, action))
  
  def booleanOpt(shortopt: String, longopt: String, valueName: String,
      description: String, action: Boolean => Unit) =
    add(new BooleanArgOptionDefinition(Some(shortopt), longopt, valueName, description, action))

  def booleanOpt(shortopt: Option[String], longopt: String, valueName: String,
      description: String, action: Boolean => Unit) =
    add(new BooleanArgOptionDefinition(shortopt, longopt, valueName, description, action))
      
  def keyValueOpt(shortopt: String, longopt: String, description: String, action: (String, String) => Unit) =
    add(new KeyValueArgOptionDefinition(Some(shortopt), longopt, defaultKeyName, defaultValueName, description, action))

  def keyValueOpt(longopt: String, description: String, action: (String, String) => Unit) =
    add(new KeyValueArgOptionDefinition(None, longopt, defaultKeyName, defaultValueName, description, action))
  
  def keyValueOpt(shortopt: String, longopt: String, keyName: String, valueName: String,
      description: String, action: (String, String) => Unit) =
    add(new KeyValueArgOptionDefinition(Some(shortopt), longopt, keyName, valueName, description, action))

  def keyValueOpt(shortopt: Option[String], longopt: String, keyName: String, valueName: String,
      description: String, action: (String, String) => Unit) =
    add(new KeyValueArgOptionDefinition(shortopt, longopt, keyName, valueName, description, action))
  
  def keyIntValueOpt(shortopt: String, longopt: String, description: String, action: (String, Int) => Unit) =
    add(new KeyIntValueArgOptionDefinition(Some(shortopt), longopt, defaultKeyName, defaultValueName, description, action))

  def keyIntValueOpt(longopt: String, description: String, action: (String, Int) => Unit) =
    add(new KeyIntValueArgOptionDefinition(None, longopt, defaultKeyName, defaultValueName, description, action))
  
  def keyIntValueOpt(shortopt: String, longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Int) => Unit) =
    add(new KeyIntValueArgOptionDefinition(Some(shortopt), longopt, keyName, valueName, description, action))

  def keyIntValueOpt(shortopt: Option[String], longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Int) => Unit) =
    add(new KeyIntValueArgOptionDefinition(shortopt, longopt, keyName, valueName, description, action))
  
  def keyDoubleValueOpt(shortopt: String, longopt: String, description: String, action: (String, Double) => Unit) =
    add(new KeyDoubleValueArgOptionDefinition(Some(shortopt), longopt, defaultKeyName, defaultValueName, description, action))

  def keyDoubleValueOpt(longopt: String, description: String, action: (String, Double) => Unit) =
    add(new KeyDoubleValueArgOptionDefinition(None, longopt, defaultKeyName, defaultValueName, description, action))
    
  def keyDoubleValueOpt(shortopt: String, longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Double) => Unit) =
    add(new KeyDoubleValueArgOptionDefinition(Some(shortopt), longopt, keyName, valueName, description, action))

  def keyDoubleValueOpt(shortopt: Option[String], longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Double) => Unit) =
    add(new KeyDoubleValueArgOptionDefinition(shortopt, longopt, keyName, valueName, description, action))

  def keyBooleanValueOpt(shortopt: String, longopt: String, description: String, action: (String, Boolean) => Unit) =
    add(new KeyBooleanValueArgOptionDefinition(Some(shortopt), longopt, defaultKeyName, defaultValueName, description, action))

  def keyBooleanValueOpt(longopt: String, description: String, action: (String, Boolean) => Unit) =
    add(new KeyBooleanValueArgOptionDefinition(None, longopt, defaultKeyName, defaultValueName, description, action))

  def keyBooleanValueOpt(shortopt: String, longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Boolean) => Unit) =
    add(new KeyBooleanValueArgOptionDefinition(Some(shortopt), longopt, keyName, valueName, description, action))

  def keyBooleanValueOpt(shortopt: Option[String], longopt: String, keyName: String, valueName: String,
      description: String, action: (String, Boolean) => Unit) =
    add(new KeyBooleanValueArgOptionDefinition(shortopt, longopt, keyName, valueName, description, action))
  
  def help(shortopt: String, longopt: String, description: String) =
    add(new FlagOptionDefinition(Some(shortopt), longopt, description, {this.showUsage; sys.exit}))

  def help(shortopt: Option[String], longopt: String, description: String) =
    add(new FlagOptionDefinition(shortopt, longopt, description, {this.showUsage; sys.exit}))
  
  def separator(description: String) =
    add(new SeparatorDefinition(description))
  
  /** adds an argument invoked by an option without `-` or `--`.
   * @param name name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */  
  def arg(name: String, description: String, action: String => Unit) =
    add(new Argument(name, description, 1, 1, action))

  /** adds an optional argument invoked by an option without `-` or `--`.
   * @param name name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */  
  def argOpt(name: String, description: String, action: String => Unit) =
    add(new Argument(name, description, 0, 1, action))
      
  /** adds a list of arguments invoked by options without `-` or `--`.
   * @param name name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */
  def arglist(name: String, description: String, action: String => Unit) =
    add(new Argument(name, description, 1, OptionParser.UNBOUNDED, action))

  /** adds an optional list of arguments invoked by options without `-` or `--`.
   * @param name name in the usage text
   * @param description description in the usage text
   * @param action callback function
   */
  def arglistOpt(name: String, description: String, action: String => Unit) =
    add(new Argument(name, description, 0, OptionParser.UNBOUNDED, action))
    
  // -------- Getting usage information ---------------
  private def descriptions: Seq[String] = options.map(opt => opt match {
    //case x: Argument => x.longopt + " :" + NLTB + opt.description
    case x if !x.canBeInvoked => x.description
    case x if x.keyValueArgument =>
      (x.shortopt map { o => "-" + o + ":" + x.keyName + "=" + x.valueName + " | " } getOrElse { "" }) +
      "--" + x.longopt + ":" + x.keyName + "=" + x.valueName + NLTB + x.description    
    case x if x.gobbleNextArgument =>
      (x.shortopt map { o => "-" + o + " " + x.valueName + " | " } getOrElse { "" }) +
      "--" + x.longopt + " " + x.valueName + NLTB + x.description
    case _ =>
      (opt.shortopt map { o => "-" + o + " | " } getOrElse { "" }) + 
      "--" + opt.longopt + NLTB + opt.description
  }) ++ (argList match {
    case Some(x: Argument) => List(x.valueName + NLTB + x.description)
    case None              => arguments.map(a => a.valueName + NLTB + a.description)
  })
  
  def usage: String = {
    val prorgamText = programName map { _ + " " } getOrElse { "" }
    val versionText = programName map { pg =>
      version map { NL + pg + " " + _ } getOrElse { "" }
    } getOrElse { "" }
    val optionText = if (options.isEmpty) {""} else {"[options] "}
    val argumentList = argumentNames.mkString(" ")

    versionText + NL + "Usage: " + prorgamText + optionText + argumentList + NLNL +
    "  " + descriptions.mkString(NL + "  ") + NL
  }

  def showUsage = Console.err.println(usage)

  private def argumentNames: Seq[String] = argList match {
    case Some(x: Argument) => List(x.valueName)
    case None              => arguments.map(_.valueName)
  }

  private def applyArgument(option:OptionDefinition, arg:String) :Boolean ={
      try {
        option.action.apply(arg)
        true
      } catch {
        case e:NumberFormatException => System.err.println("Error: " +
                option.shortDescription + " expects a number but was given '" + arg + "'")
        false
        case e:Throwable => System.err.println("Error: " +
                option.shortDescription + " failed when given '" + arg + "'. " + e.getMessage)
        false
      }
  }

  /** parses the given `args`.
   * @return `true` if successful, `false` otherwise
   */
  def parse(args: Seq[String]): Boolean = {
    var i = 0
    val unseenArgs = arguments.clone
    var answer = true
    var argListCount = 0
    var indexOutOfBounds = false

    while (i < args.length) {
      val arg = args(i)
      val matchingOption = options.find(opt =>
        opt.canBeInvoked &&
          ((!opt.keyValueArgument &&
            (arg == "--" + opt.longopt ||
            (opt.shortopt map { o => arg == "-" + o } getOrElse { false }))) ||
          (opt.keyValueArgument &&
            (arg.startsWith("--" + opt.longopt + ":") ||
            (opt.shortopt map { o => arg.startsWith("-" + o + ":") } getOrElse { false }))))
      )
      
      matchingOption match {
        case None =>
          if (arg.startsWith("-")) {
            if (errorOnUnknownArgument) {
              System.err.println("Error: Unknown argument '" + arg + "'")
              answer = false              
            } else
              System.err.println("Warning: Unknown argument '" + arg + "'")
          } else if (argList.isDefined) {
            argListCount += 1
            if (!applyArgument(argList.get, arg)) {
              answer = false
            }            
          } else if (unseenArgs.isEmpty) {
            if (errorOnUnknownArgument) {
              System.err.println("Error: Unknown argument '" + arg + "'")
              answer = false              
            } else
              System.err.println("Warning: Unknown argument '" + arg + "'")
          } else {
            val first = unseenArgs.remove(0)
            
            if (!applyArgument(first, arg)) {
              answer = false
            }
          }
          
        case Some(option) =>
          val argToPass: String = if (option.gobbleNextArgument) {
            i += 1;
            
            if (i >= args.length) {
              indexOutOfBounds = true
              if (errorOnUnknownArgument) {
                System.err.println("Error: missing value after '" + arg + "'")
                answer = false
              } else
                System.err.println("Warning: missing value after '" + arg + "'")
              ""
            } else
              args(i)
          } else if (option.keyValueArgument &&
              (option.shortopt map { o => arg.startsWith("-" + o + ":") } getOrElse { false })) {
            arg.drop(("-" + option.shortopt.get + ":").length)
          } else if (option.keyValueArgument &&
              arg.startsWith("--" + option.longopt + ":")) {
            arg.drop(("--" + option.longopt + ":").length)
          } else
            ""
          
          if (!indexOutOfBounds && !applyArgument(option, argToPass)) {
            answer = false
          }
      }
      i += 1
    }
    
    if ((unseenArgs.toList exists { _.minOccurs > 0 }) ||
        (argListCount == 0 && (argList match {
          case Some(a: Argument) => a.minOccurs > 0
          case _ => false
        }))) {
      System.err.println("Error: missing arguments: " + argumentNames.mkString(", "))
      answer = false      
    }
    if (!answer)
      showUsage
    answer
  }
}

object OptionParser {
  private[scopt] val UNBOUNDED = 1024  
}
