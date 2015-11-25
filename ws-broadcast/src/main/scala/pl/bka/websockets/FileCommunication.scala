package pl.bka.websockets

import java.io.File

object FileCommunication {
  val filePath = "../spark-listener/test"

  def formatHtml(text: String): String = text.split('\n').mkString("<br>")

  def getMessage: Option[String] = {
    val file = new File(filePath)
    if(file.exists) {
      val testTxtSource = scala.io.Source.fromFile(filePath)
      val str = testTxtSource.mkString
      testTxtSource.close
      Some(str)
    } else None
  }

  def rmFile = {
    val file = new File(filePath)
    if (file.exists) file.delete()
  }
}


