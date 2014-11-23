import akka.actor.ActorSystem

/**
 * Created by Ankit on 11/23/2014.
 */
object project4 {
  def main(args: Array[String]): Unit = {
    val numberOfUsers = (args(0).toInt)
    //get the superboss instances here
    val system = ActorSystem("twitter-super-boss")
    //keep two superbosses
    //val server = system.actorOf(Props[Server], "t1")
    //val server2 = system.actorOf(Props[Server], "t2")
    // server ! init(numberOfUsers, true)
    // server2 ! init(numberOfUsers, false)
    // client ! init()


  }
}
