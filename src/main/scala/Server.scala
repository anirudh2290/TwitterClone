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
case class Init(numUsers:Int, isFirstServer: Boolean)
case class calculateStats()
case class receiveCount(noOfTweetRequest: Int, noOfTweetResponses: Int)

object Server {
  def props(clientActorSystem: String, clientIpAddress: String, clientPort: String, noOfLBS: Int):Props =
    Props(classOf[Server], clientActorSystem, clientIpAddress, clientPort, noOfLBS)
}

class Server(clientActorSystem: String, clientIpAddress: String, clientPort: String, noOfLBS: Int) extends Actor{
   
  /*keep number of server workers equal to nr of cores*/
  val nrOfCores: Int = Runtime.getRuntime().availableProcessors()
  var countOfTweetRequests: Int = 0
  var countOfTweetResponses: Int = 0
  var finalCountOfTweetRequests: Int = 0
  var finalCountOfTweetResponses: Int = 0
  var isFirstServer: Boolean = false
  var totalReceived: Int = 0

  def receive = {
    //case init(numberOfUsers: Int, isFirstServer: Boolean) => InitializeServer(numberOfUsers, isFirstServer)
    //MUGDHA:: Changed the parameter passed to numUsers Please take a look
    case Init(numUsers, isFirstServer) => {
      if(isFirstServer) {
        InitializeServer(numUsers)
      }
    }
  	case sendTweetToRouter(tweet: String) => sendTweetToRouter(tweet,sender())
    case giveTweetFromRouter() => giveTweetFromRouter(sender())
    case calculateStats() => calculateStatsServer(sender())
    case receiveCount(noOfTweetRequest: Int, noOfTweetResponses: Int) => receiveCountVal(noOfTweetRequest, noOfTweetResponses, sender())
  }

  private def receiveCountVal(noOfTweetRequest: Int, noOfTweetResponses: Int, sender: ActorRef): Unit ={
    println("pathname is " + self.path.name + "noOfTweetRequests is " + noOfTweetRequest + "noOfTweetResponses is " + noOfTweetResponses )

    if(sender.path.name == "server0") {
      countOfTweetRequests = countOfTweetRequests - noOfTweetRequest
      countOfTweetResponses = countOfTweetResponses - noOfTweetResponses
    }

    if(isFirstServer) {
      countOfTweetRequests = countOfTweetRequests + noOfTweetRequest
      countOfTweetResponses = countOfTweetResponses + noOfTweetResponses
      totalReceived = totalReceived + 1
    }

    println("totalReceived is " + totalReceived)


    if(totalReceived == (noOfLBS)) {
      println("="*30)
      println("The total countOfTweetRequests")
      println(countOfTweetRequests)
      println("The total countOfTweetResponses")
      println(countOfTweetResponses)
      println("="*30)
    }
  }

  private def calculateStatsServer(sender: ActorRef): Unit ={
    println("Inside calculateStatsServer " + self.path.name)
    if(isFirstServer) {
         for(i <- 1 to noOfLBS - 1) {
           println(self.path)
           context.actorSelection("../server" + i.toString()) ! calculateStats()
         }
    }
    context.actorSelection("../server" + 0.toString()) ! receiveCount(countOfTweetRequests, countOfTweetResponses)


  }

  def InitializeServer(numUsers: Int) {
    isFirstServer = true
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
    countOfTweetRequests = countOfTweetRequests + 1
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! sendTweetToWorker(tweet, senderId)
  }

  private def giveTweetFromRouter(senderRef: ActorRef): Unit ={
    countOfTweetResponses = countOfTweetResponses + 1
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! giveTweetFromWorker(senderId, clientActorSystem, clientIpAddress, clientPort)
  }
}
