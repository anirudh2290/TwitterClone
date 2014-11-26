/**
 * Created by Anirudh on 11/23/2014.
 */
import akka.actor._
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

//will add isFirst later
//case class init(numberOfUsers: Int, isFirstServer: Boolean)
case class sendTweetToRouter(tweet: String)
case class giveTweetFromRouter()
case class Init(numUsers:Int, isFirstServer: Boolean)
case class calculateStats()
case class receiveCount(noOfTweetRequest: Int, noOfTweetResponses: Int)
case class countTweetRequestsAndResponses()
case class initializeScheduler()
case class sendTweetAndResponsesServer(countIndex: Int, tweetRequest: Int, tweetResponse: Int)
case class cancelSchedulers()

object Server {
  def props(serverAC: ActorSystem, clientActorSystem: String, clientIpAddress: String, clientPort: String, noOfLBS: Int):Props =
    Props(classOf[Server], serverAC, clientActorSystem, clientIpAddress, clientPort, noOfLBS)
}

class Server(serverAC: ActorSystem, clientActorSystem: String, clientIpAddress: String, clientPort: String, noOfLBS: Int) extends Actor{
   
  /*keep number of server workers equal to nr of cores*/
  val nrOfCores: Int = Runtime.getRuntime().availableProcessors()
  var countOfTweetRequests: Int = 0
  var countOfTweetResponses: Int = 0
  var finalCountOfTweetRequests: Int = 0
  var finalCountOfTweetResponses: Int = 0
  var isFirstServer: Boolean = false
  var totalReceived: Int = 0
  var tweetRequestsList: ListBuffer[Int] = new ListBuffer[Int]
  var tweetResponseList: ListBuffer[Int] = new ListBuffer[Int]
  var tweetRequestSample: Int = 0
  var tweetResponseSample: Int = 0
  var countIndex: Int = 0
  var cancellable: Cancellable = new Cancellable {override def isCancelled: Boolean = false

    override def cancel(): Boolean = false
  }

  def receive = {
    //case init(numberOfUsers: Int, isFirstServer: Boolean) => InitializeServer(numberOfUsers, isFirstServer)
    //MUGDHA:: Changed the parameter passed to numUsers Please take a look
    case Init(numUsers, isFirstServer) => initScheduler(numUsers, isFirstServer)
  	case sendTweetToRouter(tweet: String) => sendTweetToRouter(tweet,sender())
    case giveTweetFromRouter() => giveTweetFromRouter(sender())
    case calculateStats() => calculateStatsServer(sender())
    case receiveCount(noOfTweetRequest: Int, noOfTweetResponses: Int) => receiveCountVal(noOfTweetRequest, noOfTweetResponses, sender())
    case countTweetRequestsAndResponses() => countTweetReqsAndResps()
    case sendTweetAndResponsesServer(countIndex: Int, tweetRequest: Int, tweetResponse: Int) => sendTweetAndResponses(countIndex, tweetRequest, tweetResponse)
    case cancelSchedulers() => {cancellable.cancel()}
  }

  private def initScheduler(numUsers: Int, isFstServer: Boolean): Unit ={
    if(isFstServer) {
      InitializeServer(numUsers)
    }
    import serverAC.dispatcher
    cancellable = serverAC.scheduler.schedule(0.5 seconds, 1 seconds, self , countTweetRequestsAndResponses())
  }

  private def countTweetReqsAndResps(): Unit = {
    context.actorSelection("../server" + 0 ) ! sendTweetAndResponsesServer(countIndex, tweetRequestSample, tweetResponseSample)
    tweetRequestSample = 0
    tweetResponseSample = 0
    countIndex = countIndex + 1
  }

  private def sendTweetAndResponses(countIndex: Int, tweetRequest: Int, tweetResponse: Int): Unit ={
    //println("Inside sendTweetAndResponses " + "countIndex is " +  countIndex + "tweetRequest is " +  tweetRequest + "tweetResponse is " + tweetResponse)
    if(tweetRequestsList.size <= countIndex) {
      tweetRequestsList += tweetRequest
    } else {
      tweetRequestsList(countIndex) = tweetRequestsList(countIndex) + tweetRequest
    }

    if(tweetResponseList.size <= countIndex) {
      tweetResponseList += tweetResponse
    } else {
      tweetResponseList(countIndex) = tweetResponseList(countIndex) + tweetResponse
    }
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
      for(i <- 1 to noOfLBS - 1) {
        println("Inside totalReceived == noOfLBS")
        println(self.path)
        context.actorSelection("../server" + i.toString()) ! cancelSchedulers()
      }

      println("="*30)
      println("The total countOfTweetRequests")
      println(countOfTweetRequests)
      println("The total countOfTweetResponses")
      println(countOfTweetResponses)
      println("Lists are tweetRequestsList ::::::::::::")
      println(tweetRequestsList)
      println("Lists are tweetResponsList :::::::::::::")
      println(tweetResponseList)
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
    tweetRequestSample = tweetRequestSample + 1
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! sendTweetToWorker(tweet, senderId)
  }

  private def giveTweetFromRouter(senderRef: ActorRef): Unit ={
    countOfTweetResponses = countOfTweetResponses + 1
    tweetResponseSample = tweetResponseSample + 1
    var senderNameString = senderRef.path.name
    var senderId = senderNameString.substring(1).toInt
    var sendTo: Int = senderId % nrOfCores
    //route to serverworker with id = sendTo
    var actr = context.actorSelection(sendTo.toString())
    actr ! giveTweetFromWorker(senderId, clientActorSystem, clientIpAddress, clientPort)
  }
}
