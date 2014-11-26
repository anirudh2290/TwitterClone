import java.io.PrintWriter
import java.security.MessageDigest
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import akka.actor.PoisonPill
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.actor.UntypedActor
import scala.util.Random
import akka.actor.Cancellable
import ClientMaster._


case class Work()
case class Tweet()
case class receiveTweet()
case class cancelAllSchedulers()
case class printQueue(msgQ: mutable.Queue[String])

object ClientWorker {
  def props(ac: ActorSystem, servers: Int, probabilityList: (Int, Int), hostName: String, portNo: String): Props ={
    //println("probabilityList is :::::")
    //println(probabilityList)
    Props(classOf[ClientWorker], ac, servers, probabilityList, hostName, portNo)
  }
}

class ClientWorker(ac: ActorSystem, servers: Int, probabilityList: (Int, Int), hostName: String, portNo: String) extends Actor {
  println(self.path)
  var count = 0
  var cancellable2: Cancellable = new Cancellable {override def isCancelled: Boolean = false

    override def cancel(): Boolean = false
  }

  var cancellable3: Cancellable = new Cancellable {override def isCancelled: Boolean = false

    override def cancel(): Boolean = false
  }

  var actorSysName = "twitter-server"
  var probabilityOfSelection: Double = ac.settings.config.getDouble("akka.remote.netty.tcp.stableProb")
  def receive = {
    case Tweet() => tweet()
    case receiveTweet() => receiveTweets()
    case printQueue(msgQ: mutable.Queue[String]) => {
      println("*"*20)
      println("worker name " + self.path.name)
      println(msgQ.toString())
      println("*"*20)
    }
    case Work() =>{
      //var cancellable: Cancellable = system.scheduler.schedule(0 milliseconds, 1 milliseconds, self, Tweet)
      import ac.dispatcher
      cancellable2 = ac.scheduler.schedule(0 seconds, 1 seconds, self, Tweet())
      cancellable3 = ac.scheduler.schedule(0 seconds, 10 seconds, self, receiveTweet())
    }

    case cancelAllSchedulers() => {
      cancellable2.cancel()
      cancellable3.cancel()
    }

  }
  //TODO allow for multiple superbosses
  //Passed servers because there can be multiple load balancers in the front
  def tweet(): Unit = {

    if(count >= probabilityList._1 && count <= probabilityList._2) {
      probabilityOfSelection = ac.settings.config.getDouble("akka.remote.netty.tcp.peakProb")
    } else {
      probabilityOfSelection = ac.settings.config.getDouble("akka.remote.netty.tcp.stableProb")
    }

    var random_index = Random.nextFloat()
    //   println(" This is the index   " + random_index + "Distribution   " + "  Worker name   " + self.path.name + "count  " + count)
    if (random_index < probabilityOfSelection) {
      val random_tweet = Random.alphanumeric.take(140).mkString
      //    val random_tweet = Random.nextString(10)
      // print(" This is the tweet   " + random_tweet)
      println("="*30)
      println("worker name " + self.path.name)
      println("random tweet sent is " + random_tweet + "count value is " + count )
      println("="*30)
      context.actorSelection("akka.tcp://" + actorSysName + "@" + hostName + ":" + portNo + "/user/server" + (count%servers) ) ! sendTweetToRouter(random_tweet)
      count = count + 1
     // println("called")
      //listener ! tweetPrint(random_tweet)
    } else {
      count = count + 1
    }

  }
  def receiveTweets():Unit ={
    //println("akka.tcp://" + actorSysName + "@" + hostName + ":" + portNo + "/user/server" + (count%servers))
    context.actorSelection("akka.tcp://" + actorSysName + "@" + hostName + ":" + portNo + "/user/server" + (count%servers) ) ! giveTweetFromRouter()
  }
}