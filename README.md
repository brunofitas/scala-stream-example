
<h1>Scala stream example</h1>

author: Bruno Fitas


<br/>
Example application that processes time series from a file using a stream from scala standard library.

The application takes an argument with the path to a file containing time series and prints out the analysis results taken from a rolling window of 60 seconds.


**Input:**

`[time_in_seconds] [ratio]`

```
1355270609 1.80215
1355270621 1.80185
1355270646 1.80195
1355270702 1.80225
1355270702 1.80215
1355270829 1.80235
1355270854 1.80205
1355270868 1.80225
1355271000 1.80245
1355271023 1.80285
```



**Output:**

`[time_in_seconds] [ratio] [w_events] [w_ratio_sum] [w_min_ratio] [w_max_ratio]`

```
T          V       N RS      MinV    MaxV
--------------------------------------------- 
1355270609 1,80215 1 1,80215 1,80215 1,80215
1355270621 1,80185 2 3,60400 1,80185 1,80215
1355270646 1,80195 3 5,40595 1,80185 1,80215
1355270702 1,80225 2 3,60420 1,80195 1,80225
1355270702 1,80215 3 5,40635 1,80195 1,80225
1355270829 1,80235 1 1,80235 1,80235 1,80235
1355270854 1,80205 2 3,60440 1,80205 1,80235
1355270868 1,80225 3 5,40665 1,80205 1,80235
1355271000 1,80245 1 1,80245 1,80245 1,80245
1355271023 1,80285 2 3,60530 1,80245 1,80285
```

<br/>

The method `run` starts the stream:


```
def run(filename: String): Unit = {
  println(
    s"""T          V       N RS      MinV    MaxV
        |--------------------------------------------- """.stripMargin)

  stream(filename) map fromChar collect toLine map toRow foreach render
}


```


<h2>Build with sbt</h2>


**Test:**

``` 
sbt test
```


**Demo:**

``` 
sbt "run data/time_series.txt"
```