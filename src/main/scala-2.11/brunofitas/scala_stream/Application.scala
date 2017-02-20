package brunofitas.scala_stream


import brunofitas.scala_stream.timeseries.{TimeSeries2, TimeSeries1}

import scala.util.{Failure, Success, Try}


object Application extends App{

  /** Running TimeSeries **/

  args.length match {
    case 0 =>  println("File path is required"); System.exit(1)
    case 1 => TimeSeries1(args(0))
    case _ =>
      args(1) match {
        case "1"  => TimeSeries1(filename = args(0))
        case "2"  => TimeSeries2(filename = args(0))
        case _    => TimeSeries1(filename = args(0)) // default
      }
  }

}
