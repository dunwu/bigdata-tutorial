/**
  * 捕获异常示例
  *
  * @author peng.zhang
  */
import java.io.{FileNotFoundException, FileReader, IOException}

object Test {
  def main(args: Array[String]) {
    try {
      val f = new FileReader("input.txt")
    } catch {
      case ex: FileNotFoundException => {
        println("Missing file exception")
      }
      case ex: IOException => {
        println("IO Exception")
      }
    }
  }
}
