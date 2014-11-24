/**
 * Created by Mugdha on 11/24/2014.
 */

import scala.collection.mutable.ListBuffer
import scala.util.Random

class StatNode(per:Float, start:Int, end:Int){
  var percent:Float = per;
  var rangeStart:Int = start;
  var rangeEnd:Int = end;
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
