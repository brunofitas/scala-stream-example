package brunofitas.scala_stream

import brunofitas.scala_stream.TimeSeries.TimeSeriesException
import scala.util.{Failure, Success, Try}


object Application extends App{

    if(args.isEmpty) {
      println(s"[ERROR][1][File path is required]")
      System.exit(1)
    }

    /** Running TimeSeries **/
    Try( TimeSeries( filename = args(0), strategy = Try(args(1).toInt).getOrElse(2))) match {
      case Success(s) => System.exit(0)
      case Failure(e) => e match {
        case e: TimeSeriesException => println(s"[ERROR][${e.code}][${e.msg}]"); System.exit(1)
        case other => println(other.printStackTrace()); System.exit(1)
      }
    }


}
