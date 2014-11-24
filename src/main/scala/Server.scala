/**
 * Created by Anirudh on 11/23/2014.
 */
import akka.actor.{ActorRef, ActorSystem, Props, Actor, Inbox}
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer


case class init(numberOfUsers: Int, isFirstServer: Boolean)
case class sendTweetToRouter(tweet: String)
case class giveTweetFromRouter()

object Server {
  def props(clientActorSystem: String, clientIpAddress: String, clientPort: String):Props =
    Props(classOf[Server], clientActorSystem, clientIpAddress, clientPort)
}

class Server(clientActorSystem: String, clientIpAddress: String, clientPort: String) extends Actor{
  /*data structure to store pie chart*/
  val pieChart: ListBuffer[(Int,Int , Int)] = new ListBuffer[(Int, Int, Int)]
  /*keep number of server workers equal to nr of cores*/
  val nrOfCores: Int = Runtime.getRuntime().availableProcessors()


  def receive = {
    case init(numberOfUsers: Int, isFirstServer: Boolean) => InitializeServer(numberOfUsers, isFirstServer)
    case sendTweetToRouter(tweet: String) => sendTweetToRouter(tweet,sender())
    case giveTweetFromRouter() => giveTweetFromRouter(sender())
  }
  //isFirstServer is used so that we can additional instances to the routing level
  private def InitializeServer(numberOfUsers: Int, isFirstServer: Boolean): Unit ={

    if(isFirstServer) {
      /*initialize here*/

      /* create actors here*/

      /*initialize pie chart. Is essentially a list of tuples*/
      println("Initializing the server here")

      /*send init to actors*/
      /*actr ! initWorker()*/
    }
  }

  private def sendTweetToRouter(tweet: String, senderRef: ActorRef): Unit = {
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! sendTweetToWorker(tweet, senderId)
  }

  private def giveTweetFromRouter(senderRef: ActorRef): Unit ={
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! giveTweetFromWorker(senderId, clientActorSystem, clientIpAddress, clientPort)
  }
}
