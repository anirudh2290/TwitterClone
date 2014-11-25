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

class ClientListener extends Actor {
  def receive = {
    case tweetPrint(random_tweet: String) =>
      println(" This is the tweet from listener " + random_tweet)
  }
}