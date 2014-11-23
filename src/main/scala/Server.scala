/**
 * Created by Anirudh on 11/23/2014.
 */
import akka.actor.{ActorRef, ActorSystem, Props, Actor, Inbox}
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer


case class init(numberOfUsers: Int, isFirstServer: Boolean)

object Server {

}

class Server extends Actor{

  /*data structure to store pie chart*/
  val pieChart: ListBuffer[(Int, Int, Int)] = new ListBuffer[(Int, Int, Int)]

  def receive = {
    case init(numberOfUsers: Int, isFirstServer: Boolean) => InitializeServer(numberOfUsers: Int, isFirstServer: Boolean)
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
}