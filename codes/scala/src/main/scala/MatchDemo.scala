/**
  * @author peng.zhang
  */
object MatchDemo {
  def main(args: Array[String]) {
    println(matchTest("two"))
    println(matchTest("test"))
    println(matchTest(1))
    println(matchTest(6))

  }

  def matchTest(x: Any): Any = x match {
    case 1      => "one"
    case "two"  => 2
    case y: Int => "scala.Int"
    case _      => "many"
  }
}
