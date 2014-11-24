//Following import added by Anirudh for testing
//import _root_.Project4_client.tweetTestForServer
import akka.actor.{ActorRef, ActorSystem, Props}
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
    println("="*20)
    println("numberOfUsers are " + numberOfUsers)
    println("isServer value is " + isServer)
    //get the superboss instances here
    val root = ConfigFactory.load()
    val serverProps  = root.getConfig("serverSystem")
    val clientProps  = root.getConfig("clientSystem")

    if(isServer){
      val system = ActorSystem("twitter-server", serverProps)
      val server = system.actorOf(Server.props("twitter-server",clientProps.getString("akka.remote.netty.tcp.hostname"),clientProps.getString("akka.remote.netty.tcp.port") ), "superboss1")
      server ! Init(numberOfUsers)
      //Following added for testing

      Thread.sleep(1000)
      val w = system.actorOf(Props[Worker], "w" + 0)
      w ! tweetTestForServer(server, "timepass test 1")
      w ! tweetTestForServer(server, "timepass test 2")
      w ! tweetTestForServer(server, "timepass test 3")
      w ! tweetTestForServer(server, "timepass test 4")
      w ! receiveTweetForServer(server)

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
