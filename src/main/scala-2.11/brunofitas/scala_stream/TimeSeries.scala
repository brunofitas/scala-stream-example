package brunofitas.scala_stream

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}


class TimeSeries{

  import TimeSeries._

  /** ###################################################################################### **/
  /** # Strategy 1 **/
  /** ###################################################################################### **/

  /** Serves as a char buffer when parsing lines */
  private var lineBuffer: String = ""

  /** Sets the maximum chars per line **/
  private val bufferSize = 100

  /** TimeSeries line regex to get values */
  private val lineRegex: Regex = """([\d]*)[\t\s]*([\d.]*)""".r

  /** Holds Line objects from current window */
  private var windowBuffer: ListBuffer[Line] = ListBuffer[Line]()

  /** Sets the window time **/
  private val windowTime = 60


  /** Returns a Stream[Char] from a file */
  val stream : (String) => Stream[Char] = {
    (path: String ) => {
      Try(Source.fromFile(path).toStream) match {
        case Failure(f) => throw TimeSeriesException(11, "File not found")
        case Success(s) => s
      }
    }
  }

  /** Flushes the line buffer into a Option[Line] if it receives the char '\n'. */
  val toLine : (Char) => Option[Line] = {
    case '\n' =>
      val parts = lineRegex.findFirstMatchIn(lineBuffer)
      lineBuffer = ""
      parts match {
        case None => throw TimeSeriesException(21, "Error parsing line")
        case Some(p) =>
          Try(Line(time = p.group(1).trim.toInt, ratio = p.group(2).trim.toDouble)) match {
            case Failure(f) => throw TimeSeriesException(22, "Line could not be parsed")
            case Success(l) => Some(l)
          }
      }
    case other =>
      if(lineBuffer.length > bufferSize)
        throw TimeSeriesException(31, "Buffer overflow")
      lineBuffer += other
      None
  }

  /** Transforms Line objects into Row objects */
  val toRow : (Line) => Row = {
    (l:Line) => {
      windowBuffer = windowBuffer.filter(r => l.time - r.time <= windowTime) += l
      val ratios = windowBuffer.map(_.ratio)
      Row(l.time, l.ratio, windowBuffer.length, ratios.sum, ratios.min, ratios.max)
    }
  }

  /** Prints header */
  val renderHeader : () => Unit = {
    () => println( s"""T          V       N RS      MinV    MaxV
                      |--------------------------------------------- """.stripMargin)
  }

  /** Prints out rows */
  val renderRow : (Row) => Unit = {
    (r:Row) => {
        println(s"${r.time} ${"%.5f".format(r.ratio)} ${r.n} ${"%.5f".format(r.rs)} ${"%.5f".format(r.minV)} ${"%.5f".format(r.maxV)}")
    }
  }

  def run1(filename: String): Unit = {

    stream(filename) flatMap(c => toLine(c)) map toRow foreach renderRow

  }

  /** ###################################################################################### **/
  /** # Strategy 2 **/
  /** ###################################################################################### **/

  def run2(filename: String):Unit = {
    Try(Source.fromFile(filename).toStream)
      .getOrElse(throw TimeSeriesException(1, "File not found"))
      .scanLeft(Buffer(chars = "", lines = List.empty[Line], flush = false)) {
        (b, c) => {
          c match {

            case '\n' =>
              val parts = {
                lineRegex.findFirstMatchIn(b.chars)
                  .getOrElse(throw TimeSeriesException(2, "Error parsing line"))
              }
              val line = {
                Try(Line(time = parts.group(1).trim.toInt, ratio = parts.group(2).trim.toDouble))
                  .getOrElse(throw TimeSeriesException(3, "Line could not be parsed"))
              }
              Buffer(chars = "", lines = b.lines.filter(r => line.time - r.time <= 60) ++ List(line), flush = true)

            case other => b.copy(chars = b.chars + other, flush = false)
          }
        }
      }
      .collect {
        case b: Buffer if b.flush => b.lines
      }
      .foreach {
        w => {
          val ratios = w.map(_.ratio)
          val row = (w.last.time, "%.5f".format(w.last.ratio), w.length, "%.5f".format(ratios.sum), "%.5f".format(ratios.min), "%.5f".format(ratios.max))
          println(row.productIterator.toList.mkString(" "))
        }
      }
  }

  /** ###################################################################################### **/
  /** # RUN **/
  /** ###################################################################################### **/

  /** Starting point */
  def run(filename: String, strategy: Int = 0): Unit = {

    renderHeader()

    strategy match {
      case 1 => run1(filename)
      case _ => run2(filename)
    }

  }

}


object TimeSeries {

  case class Line(time: Int, ratio: Double)

  case class Row(time:Int, ratio:Double, n:Int, rs:Double, minV:Double, maxV:Double)

  case class Buffer(chars:String, lines:List[Line], flush:Boolean)

  case class TimeSeriesException(code: Int, msg: String) extends Exception(msg)

  def apply(filename:String, strategy:Int = 1) = new TimeSeries().run(filename, strategy)

}
