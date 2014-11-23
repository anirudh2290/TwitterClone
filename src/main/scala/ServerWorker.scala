import scala.collection.mutable.ListBuffer

/**
 * Created by Ankit on 11/23/2014.
 */

case class initWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)])
case class sendTweetToWorker(tweet: String)
case class giveTweetFromWorker()

object ServerWorker {

}

class ServerWorker {

  def receive = {
    case initWorker(startRange: Int, endRange: Int,pieValues: ListBuffer[(Int, Int, Int)]) => initializeWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)])
    case giveTweetFromWorker() => giveTweet()
    case sendTweetToWorker(tweet: String) => sendTweet(tweet)
  }



  private def initializeWorker(startRange: Int, endRange: Int, pieValues: ListBuffer[(Int, Int, Int)]): Unit ={
    //initializeWorker here
  }

  private def sendTweet(tweet: String): Unit ={

  }

  private def giveTweet(): Unit = {

  }
}
