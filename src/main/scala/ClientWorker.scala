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
import ClientMaster._
import system.dispatcher

class ClientWorker extends Actor {
  var count = 0
  def receive = {
    case Work() =>
      self ! Tick
    case Tick =>
      var cancellable: Cancellable = system.scheduler.schedule(0 milliseconds, 1 milliseconds, self, Tweet)
    case Tweet =>
      tweet
  }

  def tweet: Unit = {
    if (count <= NormalizedDistribution.list.length - 1) {
      var random_index = Random.nextFloat()
      //   println(" This is the index   " + random_index + "Distribution   " + "  Worker name   " + self.path.name + "count  " + count)
      if (random_index < NormalizedDistribution.list(count)) {
        val random_tweet = Random.alphanumeric.take(140).mkString
        //    val random_tweet = Random.nextString(10)
        // print(" This is the tweet   " + random_tweet)
        count = count + 1
        println("called")
        listener ! tweetPrint(random_tweet)
      } else {
        count = count + 1

      }
    } else
      context.stop(self)
  }
}