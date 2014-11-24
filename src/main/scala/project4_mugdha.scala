import scala.util.Random
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Scheduler
import akka.routing.RoundRobinRouter
import scala.util.control.Breaks._
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.collection.immutable._
import com.sun.xml.internal.bind.v2.model.core.ID
import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.math

case class Init(numUsers:Int)
case class InitWorker(numUsers:Int, numUserPerWorker:Int, startID:Int, endID:Int)

object project4 {

  def main(args: Array[String]): Unit = {
    //val numUsers = args(0)
    val numUsers = 1000;
    implicit val system = ActorSystem("Twitter")
    var Server:ActorRef = system.actorOf(Props[Server])
    Server ! Init(numUsers) 
  }
}

class StatNode(per:Float, start:Int, end:Int){
  var percent:Float = per;
  var rangeStart:Int = start;
  var rangeEnd:Int = end;
}

class Server extends Actor{
  val numWorkers:Int = 64
        
  def receive = {
    case Init(numUsers) => {
      val numUserPerWorker:Int = Math.ceil(numUsers.toDouble/numWorkers.toDouble).toInt;
      println("Each worker will keep track of: " + numUserPerWorker)
      
      var ServerWorkers:List[ActorRef] = Nil
      var i:Int = 0
      while(i<numWorkers){
        //when context when system?
        ServerWorkers ::= context.actorOf(Props[ServerWorker])
        i += 1
      }
      i = 0;
      
      while (i<Math.min(numWorkers,numUsers )) {
        //when context when system?
        val start:Int = i*numUserPerWorker;
      	//val end:Int = if (i*num+num < numUsers)  i*num+num else numUsers
        val end:Int = Math.min(i*numUserPerWorker+numUserPerWorker, numUsers)  
         
        ServerWorkers(i) ! InitWorker(numUsers, numUserPerWorker, start, end)
        i += 1
      }
    }
  }
}

class ServerWorker extends Actor{
  
	  var map = Map[Int, User]()

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
  
  def receive = {
    case InitWorker(numUsers:Int, numUserPerWorker:Int, start:Int, end:Int) => {
      initialize(numUsers, numUserPerWorker, start, end-1);
    }
  }
	}

class User {
  var followers:List[Int] = List[Int]() 
  var msgQ = new scala.collection.mutable.Queue[String]
}

class Chart (totnumUsers:Int, numUserPerWorker:Int) {
  
  val totalnumUsers:Int = totnumUsers
  val numUsers = numUserPerWorker;
  var usersCounter:Int = 0;
  val chart:ListBuffer[StatNode] = new ListBuffer [StatNode]
  chart += new StatNode(0.811f, 0, 50) += 
  new StatNode(0.092f, 101, 500) += new StatNode(0.016f, 501, 1000) += 
  new StatNode(0.063f, 51, 100) += new StatNode(0.016f, 1001, 5000) += 
  new StatNode(0.002f, 0, 50) 
  
  def getFollowersList(ID:Int) : List[Int] = {
    //println("ratio: " + usersCounter + " " + numUsers + " per val: " + chart(0).percent)
    if(usersCounter/numUsers > chart(0).percent){
      chart remove 0
      usersCounter = 0;
    }
    
    val start:Int = chart(0).rangeStart 
    val end:Int = chart(0).rangeEnd 
   
    val numFollowers = getRandomNumber(end, start)
    var index:Int = 0
    var setFollowers:Set[Int] = Set()

    val maxFollowers:Int = Math.min(numFollowers, numUsers);

    while(index < maxFollowers){
	  var n = getRandomNumber(totalnumUsers);
	  while (setFollowers contains(n)){
       n = getRandomNumber(totalnumUsers);
      }
	//  println("hwewerwe" + setFollowers)
      setFollowers += n
      index += 1
    }   
    //println("ID: " + ID + " has followers " + maxFollowers + " followers: " + setFollowers)
    usersCounter += 1;   
    return setFollowers.toList;
  }
  
  def getRandomNumber(numUsers:Int) : Int = {
  /*  var i:Int = 0
    //while(i != node.ID ){
      i = Random.nextInt(numUsers)
    //}
    return i
  */
    return Random.nextInt(numUsers) 
   }
  
  def getRandomNumber(max:Int, min:Int) : Int = {
    val rand:Random = new Random();
    val randomNum:Int = rand.nextInt((max - min) + 1) + min;
    //println("random: " + max + " " + min + " random no " +randomNum);
    return randomNum;    
  }
}
