/**
  * 循环控制语句示例
  *
  * @author peng.zhang
  */
object WhileDemo {
  def main(args: Array[String]) {
    // 局部变量
    var a = 10;

    // while 循环执行
    while (a < 20) {
      println("Value of a: " + a);
      a = a + 1;
    }
  }
}
