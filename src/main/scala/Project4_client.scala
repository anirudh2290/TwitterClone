import java.io.PrintWriter
import java.security.MessageDigest
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import akka.actor.PoisonPill
import scala.collection.mutable.ArrayBuffer
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.actor.UntypedActor
import scala.util.Random
import akka.actor.Cancellable

object NormalizedDistribution {
  val list = List(.1, .2, .3, .4, .5, .6, .7, .8)

}
/*
class ClientActor extends Actor {
 def receive ={
   case init =>
     println("start")
 }
}
* 
*/
object Project4_client extends App {

  println("Client ready")
  sealed trait twitterClient
  case object CreateClients extends twitterClient
  case object Tick extends twitterClient
  case class Work() extends twitterClient
  case class tweetPrint(random_tweet: String) extends twitterClient
  val system = ActorSystem("TwitterCient")
  // val clientActor = system.actorOf(Props[ClientActor], name = "clientActor")
  val listener = system.actorOf(Props[Listener], name = "listener")
  initialize(nrOfWorkers = 1)
  def initialize(nrOfWorkers: Int) {
    // create the result listener, which will print the result and shutdown the system
    // val listener = system.actorOf(Props[Listener], name = "listener")
    // create the master
    val master = system.actorOf(Props(new Master(nrOfWorkers, listener)), name = "master")
    master ! CreateClients

  }
  class Master(nrOfWorkers: Int, listener: ActorRef) extends Actor {

    def receive = {
      case CreateClients =>
        for (i <- 0 until nrOfWorkers) {
          val w = context.actorOf(Props[Worker], "w" + i)
          w ! Work()
        }
    }

  }
}