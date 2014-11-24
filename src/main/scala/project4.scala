import akka.actor.ActorSystem
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

/**
 * Created by Ankit on 11/23/2014.
 */
object project4 {
  def main(args: Array[String]): Unit = {
    if(args.length != 2) {
      println("Correct Usage is ::: sbt runMain project4 <numberOfNodes> <isServer>")
    }
    val numberOfUsers = (args(0).toInt)
    val isServer      = (args(1).toBoolean)
    //get the superboss instances here
    val root = ConfigFactory.load()
    val serverProps  = root.getConfig("serverSystem")
    val clientProps  = root.getConfig("clientSystem")

    if(isServer){
      val system = ActorSystem("twitter-server", serverProps)
      //val server = system.actorOf(Props[Server], "t1")
      //server ! init(numberOfUsers, true)
    } else {

    }


    //keep two superbosses
    //val server = system.actorOf(Props[Server], "t1")
    //val server2 = system.actorOf(Props[Server], "t2")
    // server ! init(numberOfUsers, true)
    // server2 ! init(numberOfUsers, false)
    // client ! init()


  }
}
