Spark listener broadcasting messages through websockets.

# ws-broadcast

websockets server:

cd ws-broadcast

sbt run

http://localhost:8008

# spark-listener

spark listener writing messages to file on disk:

cd spark-listener

sbt package

spark-shell --conf spark.logConf=true --conf spark.extraListeners=pl.bka.spark.CustomSparkListener --driver-class-path ./target/scala-2.11/spark-listener_2.11-1.0.jar
