package brunofitas.scala_stream.timeseries

import scala.io.Source
import scala.util.Try
import scala.util.matching.Regex


class TimeSeries2(filename:String, bufferSize:Int = 100, windowTime:Int = 60) {

  import TimeSeries2._

  private val lineRegex: Regex = """([\d]*)[\t\s]*([\d.]*)""".r

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

object TimeSeries2 {

  case class Line(time: Int, ratio: Double)

  case class Buffer(chars:String, lines:List[Line], flush:Boolean)

  def apply(filename:String, bufferSize:Int = 100, windowTime:Int = 60) = new TimeSeries2(filename,bufferSize, windowTime)

}
