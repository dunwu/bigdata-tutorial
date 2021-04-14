/**
  * 条件控制语句示例
  *
  * @author peng.zhang
  */
object IfDemo {
  def main(args: Array[String]) {
    var x = 30;

    if (x == 10) {
      println("X 的值为 10");
    } else if (x == 20) {
      println("X 的值为 20");
    } else if (x == 30) {
      println("X 的值为 30");
    } else {
      println("无法判断 X 的值");
    }
  }
}
