import java.io.PrintWriter
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import akka.actor._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.util.Random


case class initProbabilityList()
case class createClients()
case class startWork()
case class stopWatchBegin()
case class stop()

object NormalizedDistribution {
  val list = List(.1, .2, .3, .4, .5, .6, .7, .8)

}
/*
class ClientActor extends Actor {
 def receive ={
   case init =>
     println("start")
     //Ping urs from here
 }
}
* 
*/

object ClientMaster {
  def props(statistics: ListBuffer[Int], nrOfWorkers: Int, ac: ActorSystem, servers: Int, hostNameServer: String, portNo: String): Props ={
    var timeLimit: Int = ac.settings.config.getInt("akka.remote.netty.tcp.timeLimit")
    Props(classOf[ClientMaster], statistics, timeLimit, nrOfWorkers, ac, servers, hostNameServer, portNo)
  }
}

class ClientMaster(statistics: ListBuffer[Int], timeLimit: Int, nrOfWorkers: Int, ac: ActorSystem, servers: Int, hostNameServer: String,portNo: String) extends Actor{
  var probabilityList:(Int, Int) = (0, 0)
  var cancellable:Cancellable = new Cancellable {override def isCancelled: Boolean = false

    override def cancel(): Boolean = false
  }
  def receive = {
    case initProbabilityList() => initProbList()
    case createClients() => createClientWorkers()
    case startWork()  => startWorking()
    case stopWatchBegin() => beginStopWatch()
    case stop() => stopAll()
  }

  private def initProbList(): Unit = {
    val rand:Random = new Random();
    val randomNum: Int = rand.nextInt(60*5) + 200
    probabilityList = (randomNum, randomNum + 2*60)
    println(probabilityList.toString())
    /*
    for(i <- 0 to statistics.size - 1){
      probabilityList += (statistics(i).toFloat/ nrOfWorkers.toFloat)

      if(i == statistics.size/2)
        println(probabilityList(i))
      else
        println("inelse")
    }
    */

  }

  private def startWorking(): Unit ={
    for(i <- 0 until nrOfWorkers) {
      context.actorSelection("w" + i) ! Work()
    }

    self ! stopWatchBegin()
  }

  private def createClientWorkers(): Unit ={
    for (i <- 0 until nrOfWorkers) {
      val w = context.actorOf(ClientWorker.props(ac, servers, probabilityList, hostNameServer, portNo), "w" + i)
      //Workers spawned here add a line to ping from here
      w ! Work()
    }

    //self ! stopWatchBegin()
  }

  private def beginStopWatch(): Unit ={
    import ac.dispatcher
    cancellable = ac.scheduler.scheduleOnce(Duration.create(timeLimit, TimeUnit.MINUTES), self, stop())
  }

  private def stopAll(): Unit ={
    for (i <- 0 until nrOfWorkers) {

      context.actorSelection("w" + i) ! cancelAllSchedulers()
      //Workers spawned here add a line to ping from here
    }
    ac.shutdown()
  }
}

/*
object ClientMaster extends App {

  println("Client ready")
  sealed trait twitterClient
  case object CreateClients extends twitterClient
  case object Tick extends twitterClient
  case object Tweet extends twitterClient
  case class Work() extends twitterClient
  case class PrintTweets() extends twitterClient
  case class tweetPrint(random_tweet: String) extends twitterClient
  val system = ActorSystem("TwitterCient")
  val listener = system.actorOf(Props[ClientListener], name = "listener")
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
          val w = context.actorOf(Props[ClientWorker], "w" + i)
          w ! Work()
          //Workers spawned here add a line to ping from here
        }
    }

  }
}
*/