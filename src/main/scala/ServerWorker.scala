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
//Added for testing
case class printMessageQueue()

object ServerWorker {
  def props(nrOfWorkers: Int): Props =
    Props(classOf[ServerWorker], nrOfWorkers)
}

class ServerWorker(nrOfWorkers: Int) extends Actor {

  //Added by Mugdha
  var map = Map[Int, User]()
  var connectionString: String = ""
  var limit: Int = 3

  def receive = {
  	case InitWorker(numUsers:Int, numUserPerWorker:Int, start:Int, end:Int) => {
      initialize(numUsers, numUserPerWorker, start, end-1);
    }   
	case giveTweetFromWorker(senderId: Int, clientActorSystem: String, clientIpAddress: String, clientPort: String) => giveTweet(senderId,clientActorSystem,clientIpAddress, clientPort)
    case sendTweetToWorker(tweet: String, senderId: Int) => sendTweet(tweet, senderId)
    case tweetFromSibling(tweet: String, senderId: Int) => tweetFromSiblingWorker(tweet, senderId)
    case printMessageQueue() => printMessageQueueVal()
  }

  def initialize(numUsers:Int, numUserPerWorker:Int, start:Int, end:Int) = {
    var numUsersPerWorkerAct:Int = 0
    numUsersPerWorkerAct = (end - start)/4 + 1

    val ch:Chart = new Chart(numUsers:Int, numUserPerWorker:Int)
    var IDs:Int = 0
    var beginVal = start
    while(beginVal < end){
      val list:List[Int] = ch.getFollowersList(beginVal);
      val usr:User = new User()
      usr.followers = list
      map += (beginVal -> usr)
      beginVal += nrOfWorkers
    }
    println("Inside id " + self.path.name + " numUserPerWorker is " + numUserPerWorker)
    printMap()
  }
  
  def printMap() = {
    println("Map size for worker id " + self.path.name + ": " + map.size)
    map.foreach { case (key, value) => 
    println(">>> key=" + key + ", value=" + value.followers ) }
/*    map.foreach(p => 
      println(">>> key=" + p._1 + ", value=" + p._2))
*/ }

  private def printMessageQueueVal(): Unit ={
    println("Inside printMessageQueue for " + self.path.name)
    map.foreach { case (key, value) =>
      println(">>>key=" + key + "msg queue=" + value.msgQ.toString() ) }
  }

  private def tweetFromSiblingWorker(tweet: String, senderId: Int): Unit ={
    var user: User = map.getOrElse(senderId, null)
    println("Inside tweetFromSiblingWorker senderId is " + senderId)

    if(user != null){
      user.msgQ.enqueue(tweet)
      map += (senderId -> user)
    } else {
      user = new User()
      user.msgQ.enqueue(tweet)
      map += (senderId -> user)
    }

    if(user.msgQ.size > limit){
      user.msgQ.dequeue()
    }
  }

  private def initializeWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)]): Unit ={
    //initializeWorker here
  }

  private def sendTweet(tweet: String, senderId: Int): Unit ={
    println("senderId is " + senderId)
    println("Inside worker " + self.path.name)
    println("size of map is " + map.size)
    println("does map contain the index senderId " + map.contains(senderId))
    println("||"*10)
    printMap()
    println("||"*10)
    var user: User = map.getOrElse(senderId, null)

    var workerContainingFollower:Int = -1

    if(user != null) {
      // if user not null, enqueue tweet to message queue and followers message queue
      user.msgQ.enqueue(tweet)
      println("Inside sendTweet if")
       for(i<-0 to user.followers.length - 1){
         //TODO evaluate performance of actorSelection and decide if it is better to store ActorSelections. Memory vs cpu processing tradeoff involved
         println("Inside followers send")
         if(senderId != user.followers(i)) {
           workerContainingFollower = user.followers(i) % nrOfWorkers
           context.actorSelection("../" + workerContainingFollower.toString()) ! tweetFromSibling(tweet, user.followers(i))
         }
       }

    } else {
      println("Inside sendTweet else")
      // if user is null then create new user and enqueue to message queue
      user = new User()
      user.msgQ.enqueue(tweet)
      map += (senderId -> user)
    }

    if(user.msgQ.size > limit){
      user.msgQ.dequeue()
    }
  }

  private def giveTweet(senderId: Int, clientActorSystem: String, clientIpAddress: String, clientPort: String): Unit = {
    //TODO same tradeoff made as above. Have to evaluate
    println("Inside giveTweet")
    var u: User = map.getOrElse(senderId, null)
    connectionString = "akka.tcp://" + clientActorSystem + "@" + clientIpAddress + clientPort + "/user/" + senderId.toString()
    //change this according to the method implemented in the client
    //context.actorSelection(connectionString) ! receive(u.msgQ)
    println("="*20)
    println(u.msgQ.toString())
    for(i <-0 to u.followers.length - 1) {
      println("Inside followers")
      var workerContainingFollower = u.followers(i) % nrOfWorkers
      println("workerContainingFollower is " + workerContainingFollower)
      context.actorSelection("../" + workerContainingFollower.toString()) ! printMessageQueue()
    }
    println("="*20)
  }
}
