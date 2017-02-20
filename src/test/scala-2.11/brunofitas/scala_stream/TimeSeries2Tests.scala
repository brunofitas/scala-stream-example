package brunofitas.scala_stream

import java.util.Calendar
import org.scalatest.{Matchers, FlatSpec}
import brunofitas.scala_stream.timeseries.TimeSeries2


class TimeSeries2Tests extends FlatSpec with Matchers{

  val validStream = "/time_series.txt"

  it should "print a table without errors" in {

    val filePath = getClass.getResource(validStream).getPath
    val init = Calendar.getInstance().getTimeInMillis

    TimeSeries2(filePath) shouldBe a [TimeSeries2]

    val end = Calendar.getInstance().getTimeInMillis

    println(s"Executed in ${end - init} ms")

  }

}
