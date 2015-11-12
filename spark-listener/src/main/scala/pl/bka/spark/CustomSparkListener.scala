package pl.bka.spark

import org.apache.spark.scheduler.{SparkListenerStageCompleted, SparkListener, SparkListenerJobStart}
import java.io.File

class CustomSparkListener extends SparkListener {

  private def createTestFile = new File("./test").createNewFile

  override def onJobStart(jobStart: SparkListenerJobStart) {
    println(s"Job started with ${jobStart.stageInfos.size} stages: $jobStart")
    createTestFile
  }

  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    println(s"Stage ${stageCompleted.stageInfo.stageId} completed with ${stageCompleted.stageInfo.numTasks} tasks.")
    createTestFile
  }
}