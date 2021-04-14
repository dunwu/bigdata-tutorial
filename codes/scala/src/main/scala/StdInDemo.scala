import scala.io._

/**
  * StdIn 示例
  *
  * @author peng.zhang
  */
object StdInDemo {
  def main(args: Array[String]) {
    print("请输入内容: ")
    val line = StdIn.readLine()

    println("你输入的是: " + line)
  }
}
