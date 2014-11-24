import akka.actor.{Props, Actor}

import scala.collection.mutable.ListBuffer

/**
 * Created by Anirudh on 11/23/2014.
 */

case class InitWorker(numUsers:Int, numUserPerWorker:Int, startID:Int, endID:Int)
//case class initWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)])
case class sendTweetToWorker(tweet: String, senderId: Int)
case class giveTweetFromWorker(senderId: Int, clientActorSystem: String, clientIpAddress: String, clientPort: String)
case class tweetFromSibling(tweet: String, senderId: Int)

object ServerWorker {
  def props(nrOfWorkers: Int): Props =
    Props(classOf[ServerWorker], nrOfWorkers)
}

class ServerWorker(nrOfWorkers: Int) extends Actor {

  //Added by Mugdha
  var map = Map[Int, User]()
  var connectionString: String = ""

  def receive = {
  	case InitWorker(numUsers:Int, numUserPerWorker:Int, start:Int, end:Int) => {
      initialize(numUsers, numUserPerWorker, start, end-1);
    }   
	case giveTweetFromWorker(senderId: Int, clientActorSystem: String, clientIpAddress: String, clientPort: String) => giveTweet(senderId,clientActorSystem,clientIpAddress, clientPort)
    case sendTweetToWorker(tweet: String, senderId: Int) => sendTweet(tweet, senderId)
    case tweetFromSibling(tweet: String, senderId: Int) => tweetFromSiblingWorker(tweet, senderId)
  }

  def initialize(numUsers:Int, numUserPerWorker:Int, start:Int, end:Int) = {
    val ch:Chart = new Chart(numUsers:Int, numUserPerWorker:Int)
    var IDs:Int = 0
    for (IDs <- start to end){
      val list:List[Int] = ch.getFollowersList(IDs);
      val usr:User = new User()
      usr.followers = list
      map += (IDs -> usr)
    }
    printMap()
  }
  
  def printMap() = {
    println("Map size: " + map.size)
    map.foreach { case (key, value) => 
    println(">>> key=" + key + ", value=" + value.followers ) }
/*    map.foreach(p => 
      println(">>> key=" + p._1 + ", value=" + p._2))
*/ }


  private def tweetFromSiblingWorker(tweet: String, senderId: Int): Unit ={
    var user: User = map.getOrElse(senderId, null)
    if(user != null){
      user.msgQ.enqueue(tweet)
    } else {
      user = new User()
      user.msgQ.enqueue(tweet)
      map += (senderId -> user)
    }
  }

  private def initializeWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)]): Unit ={
    //initializeWorker here
  }

  private def sendTweet(tweet: String, senderId: Int): Unit ={
    var user: User = map.getOrElse(senderId, null)
    var workerContaintFollower:Int = -1

    if(user != null) {
      // if user not null, enqueue tweet to message queue and followers message queue
      user.msgQ.enqueue(tweet)
       for(i<-0 to user.followers.length - 1){
         //TODO evaluate performance of actorSelection and decide if it is better to store ActorSelections. Memory vs cpu processing tradeoff involved
         var workerContainingFollower = user.followers(i) % nrOfWorkers
         context.actorSelection("../" + workerContainingFollower.toString()) ! tweetFromSibling(tweet, senderId)
       }

    } else {
      // if user is null then create new user and enqueue to message queue
      user = new User()
      user.msgQ.enqueue(tweet)
      map += (senderId -> user)
    }
  }

  private def giveTweet(senderId: Int, clientActorSystem: String, clientIpAddress: String, clientPort: String): Unit = {
    //TODO same tradeoff made as above. Have to evaluate
    var u: User = map.getOrElse(senderId, null)
    connectionString = "akka.tcp://" + clientActorSystem + "@" + clientIpAddress + clientPort + "/user/" + senderId.toString()
    //change this according to the method implemented in the client
    context.actorSelection(connectionString) ! receive(u.msgQ)
  }
}
