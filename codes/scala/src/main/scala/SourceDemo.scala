import scala.io.Source

/**
  * Source 示例
  *
  * @author peng.zhang
  */
object SourceDemo {
  def main(args: Array[String]) {
    println("文件内容为:")

    Source.fromFile("test.txt").foreach {
      print
    }
  }
}
