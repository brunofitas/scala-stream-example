package brunofitas.scala_stream

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}


class TimeSeries{

  import TimeSeries._

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
  val fromChar : (Char) => Option[Line] = {
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


  /** Collects Line objects only */
  val toLine : PartialFunction[Option[Line], Line] = {
    new PartialFunction[Option[Line], Line] {
      def isDefinedAt(line: Option[Line]) = line.nonEmpty
      def apply(line: Option[Line]) = line.get
    }
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
  val render : (Row) => Unit = {
    (r:Row) => {
        println(s"${r.time} ${"%.5f".format(r.ratio)} ${r.n} ${"%.5f".format(r.rs)} ${"%.5f".format(r.minV)} ${"%.5f".format(r.maxV)}")
    }
  }


  /** Starting point
    *
    * @param filename path to file
    */
  def run(filename: String): Unit = {

    renderHeader()

    stream(filename) map fromChar collect toLine map toRow foreach render
  }

}


object TimeSeries {

  case class Line(time: Int, ratio: Double)

  case class Row(time:Int, ratio:Double, n:Int, rs:Double, minV:Double, maxV:Double)

  case class TimeSeriesException(code: Int, msg: String) extends Exception(msg)

  def apply(filename:String) = new TimeSeries().run(filename)

}
