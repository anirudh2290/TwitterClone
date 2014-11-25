/**
 * Created by Anirudh on 11/23/2014.
 */
import akka.actor.{ActorRef, ActorSystem, Props, Actor, Inbox}
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

//will add isFirst later
//case class init(numberOfUsers: Int, isFirstServer: Boolean)
case class sendTweetToRouter(tweet: String)
case class giveTweetFromRouter()
case class Init(numUsers:Int)

object Server {
  def props(clientActorSystem: String, clientIpAddress: String, clientPort: String):Props =
    Props(classOf[Server], clientActorSystem, clientIpAddress, clientPort)
}

class Server(clientActorSystem: String, clientIpAddress: String, clientPort: String) extends Actor{
   
  /*keep number of server workers equal to nr of cores*/
  val nrOfCores: Int = Runtime.getRuntime().availableProcessors()
  
  def receive = {
    //case init(numberOfUsers: Int, isFirstServer: Boolean) => InitializeServer(numberOfUsers, isFirstServer)
    //MUGDHA:: Changed the parameter passed to numUsers Please take a look
    case Init(numUsers) => { InitializeServer(numUsers) }
  	case sendTweetToRouter(tweet: String) => sendTweetToRouter(tweet,sender())
    case giveTweetFromRouter() => giveTweetFromRouter(sender())
  }

  def InitializeServer(numUsers: Int) {
    val numUserPerWorker:Int = Math.ceil(numUsers.toDouble/nrOfCores.toDouble).toInt;

    var ServerWorkers:ListBuffer[ActorRef] = ListBuffer[ActorRef]()
    var i:Int = 0
    while(i<nrOfCores){
      //when context when system?
      //modified name for ServerWorkers
      ServerWorkers += context.actorOf(ServerWorker.props(nrOfCores), i.toString())
      i += 1
    }
    i = 0;

    while (i<Math.min(nrOfCores, numUsers )) {
      //when context when system?
      val start:Int = i
      //val end:Int = if (i*num+num < numUsers)  i*num+num else numUsers
      //val end:Int = Math.min(i*numUserPerWorker+numUserPerWorker, numUsers)
      val end:Int = numUsers
      ServerWorkers(i) ! InitWorker(numUsers, numUserPerWorker, start, end)
      i += 1
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
