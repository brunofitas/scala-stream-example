package brunofitas.scala_stream

import brunofitas.scala_stream.TimeSeries.TimeSeriesException

import scala.util.{Failure, Success, Try}


object Application {

  def main(args: Array[String]) = {
    Try {
      args.isEmpty match {
        case true => println(s"[ERROR][1][File path is required]"); System.exit(1)
        case false =>
          TimeSeries(args(0))
      }
    }
    match {
      case Failure(e) => e match {
        case e: TimeSeriesException => println(s"[ERROR][${e.code}][${e.msg}]"); System.exit(1)
        case other => println(other.printStackTrace()); System.exit(1)
      }
      case Success(s) => System.exit(0)
    }
  }

}
