package brunofitas.scala_stream

import java.util.Calendar
import scala.runtime.BoxedUnit
import org.scalatest.{FlatSpec, Matchers}
import brunofitas.scala_stream.timeseries.{TimeSeriesException, TimeSeries1}
import brunofitas.scala_stream.timeseries.TimeSeries1._


class TimeSeries1Tests extends FlatSpec with Matchers{


  val validStream = "/time_series.txt"


  "stream" should "return a Stream[Char] with a valid path" in {
    val ts = new TimeSeries1
    val filePath = getClass.getResource(validStream).getPath

    ts.stream(filePath) shouldBe a [Stream[_]]
  }


  it should "return a TimeSeriesException with an invalid path" in {

    val ts = new TimeSeries1

    val stream = intercept[TimeSeriesException] {
      ts.stream("/invalid/path/to/data")
    }
    stream shouldBe a [TimeSeriesException]

  }


  "fromChar" should "return a Stream[Option[Line]] with valid data" in {
    val ts = new TimeSeries1
    val filePath = getClass.getResource(validStream).getPath

    ts.stream(filePath) map ts.toLine shouldBe a [Stream[_]]
  }

  it should "return a matched line when fed with a \\n" in {
    val ts = new TimeSeries1

    val stream = Stream[Char]('1','2','3','4','\t','1','.','2','3', '4','\n')

    val result = stream flatMap(c => ts.toLine(c))

    result.headOption shouldBe Some(Line(1234, 1.234))

  }


  it should "return a matched line when spaced with \\t and \\space and fed with a \\n" in {
    val ts = new TimeSeries1
    val filePath = getClass.getResource(validStream).getPath

    val stream = Stream[Char]('1','2','3','4','\t',' ','\t', '1','.','2','3', '4','\n')

    val result = stream flatMap(c => ts.toLine(c))

    result.headOption shouldBe Some(Line(1234, 1.234))

  }


  it should "return an exception when invalid data is detected" in {
    val ts = new TimeSeries1

    val stream = Stream[Char]('a', 'b', 'c',' ','a','b','c','\n')

    val result = intercept[TimeSeriesException] {
      stream flatMap(c => ts.toLine(c))
    }

    result shouldBe a [TimeSeriesException]

    result.code shouldBe 22

  }


  it should "return an exception when lineBuffer overflows" in {
    val ts = new TimeSeries1

    val stream = (for ( c <- 1 to 111) yield 'c' ).toStream

    val result = intercept[TimeSeriesException] {
      stream flatMap(c => ts.toLine(c))
    }

    result shouldBe a [TimeSeriesException]

    result.code shouldBe 31

  }


  "toRow" should "return a Row object when fed with a Line" in {
    val ts = new TimeSeries1

    val stream = Stream[Char]('1','2','3','4','\t','1','.','2','3', '4','\n')

    val result = stream flatMap(c => ts.toLine(c)) map ts.toRow

    result.head shouldBe Row(1234, 1.234, 1, 1.234, 1.234, 1.234)
  }


  it should "return correct values from more than one event in the current window" in {
    val ts = new TimeSeries1

    val stream = Stream[Char]('1','2','3','4','\t','1','.','2','3', '4','\n', '1','2','3','5','\t','1','.','2','3', '5','\n')

    val result = stream flatMap(c => ts.toLine(c)) map ts.toRow

    val row =  result.last

    row.time shouldBe 1235
    row.ratio shouldBe 1.235
    row.rs shouldBe 1.234 + 1.235
    row.minV shouldBe 1.234
    row.maxV shouldBe 1.235
  }


  it should "return correct values from a single event in current window" in {
    val ts = new TimeSeries1

    val stream = Stream[Char]('1','2','3','4','\t','1','.','2','3', '4','\n', '2','2','3','5','\t','2','.','2','3', '5','\n')

    val result = stream flatMap(c => ts.toLine(c)) map ts.toRow

    val row =  result.last

    row.time shouldBe 2235
    row.ratio shouldBe 2.235
    row.rs shouldBe 2.235
    row.minV shouldBe 2.235
    row.maxV shouldBe 2.235
  }


  "run" should "print a table without errors" in {

    val filePath = getClass.getResource(validStream).getPath
    val init = Calendar.getInstance().getTimeInMillis

    TimeSeries1(filePath) shouldBe a [BoxedUnit]

    val end = Calendar.getInstance().getTimeInMillis

    println(s"Executed in ${end - init} ms")

  }



}
