//Following import added by Anirudh for testing
//import _root_.Project4_client.tweetTestForServer
import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ListBuffer

/**
 * Created by Ankit on 11/23/2014.
 */
object project4 {
  def main(args: Array[String]): Unit = {
    if(args.length != 3) {
      println("Correct Usage is ::: sbt runMain project4 <numberOfNodes> <isServer> <numberOfLoadbalancers>")
    }
    val numberOfUsers = (args(0).toInt)
    val isServer      = (args(1).toBoolean)
    val numberOfLBs   = (args(2).toInt)
    println("="*20)
    println("numberOfUsers are " + numberOfUsers)
    println("isServer value is " + isServer)
    //get the superboss instances here
    val root = ConfigFactory.load()
    val serverProps  = root.getConfig("serverSystem")
    val clientProps  = root.getConfig("clientSystem")

    if(isServer){
      val system = ActorSystem("twitter-server", serverProps)

      for(i <- 0 to numberOfLBs - 1) {
        var server = system.actorOf(Server.props(system, "twitter-client", clientProps.getString("akka.remote.netty.tcp.hostname"), clientProps.getString("akka.remote.netty.tcp.port"), numberOfLBs), "server" + i)
        if(i == 0) {
          server ! Init(numberOfUsers, true)
        } else{
          server ! Init(numberOfUsers, false)
        }

      }
    } else {
      val system = ActorSystem("twitter-client", clientProps)
      var statistics: ListBuffer[Int] = ListBuffer[Int]()
      var timeLimit: Int = system.settings.config.getInt("akka.remote.netty.tcp.timeLimit")
      var end: Int = timeLimit*60*1000 - 1
      var hostname: String = serverProps.getString("akka.remote.netty.tcp.hostname")
      var portNo: String   = serverProps.getString("akka.remote.netty.tcp.port")

      /* TODO statistics calculation has to be changed
      * */

      /*
         var c: Int = (numberOfUsers*21)/365
       var percent: Int = 90
      if(numberOfUsers <= 9){
        percent = 100
      }
       for(i <- 0 to end) {

        if(i >= (end/2 - c) && i < (end/2 + c)){
          //println("value is " + (end/2 - i))
          statistics += (percent*numberOfUsers).toInt/100
          //println(statistics(i))
        } else {
          statistics += ((100 - percent)*numberOfUsers).toInt/100
          //println(statistics(i))
        }
      }
      */

      var client = system.actorOf(ClientMaster.props(statistics, numberOfUsers, system, numberOfLBs, hostname, portNo),"clientmaster1")
      client ! initProbabilityList()
      println("here")
      client ! createClients()
      client ! startWork()
    }
  }
}
