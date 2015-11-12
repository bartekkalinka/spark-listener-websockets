package pl.bka.spark

import org.apache.spark.scheduler.{SparkListenerStageCompleted, SparkListener, SparkListenerJobStart}
import java.io.PrintWriter

class CustomSparkListener extends SparkListener {

  private def message(msg: String) = {
  	println(msg)
  	new PrintWriter("./test") { write(msg); close }
  }

  override def onJobStart(jobStart: SparkListenerJobStart) {
  	message(s"Job started with ${jobStart.stageInfos.size} stages: $jobStart")
  }

  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    message(s"Stage ${stageCompleted.stageInfo.stageId} completed with ${stageCompleted.stageInfo.numTasks} tasks.")
  }
}