import scala.collection.mutable.ListBuffer

/**
 * authors : (  BEN AYAD, Mohamed Ayoub   -    EL HALOUMI, Hicham )
 * Description : the actual class describes the Node, which is the structure that model a single participant
 * Date : 07/09/2019 - 11/09/2019
 * source : The Decentralized Polling algorithm with respectable participants.
 *
 *
 * @param nodeRealVote :indicates the vote associated to the node
 * @param groupId : the cluster identifier to which the node is belonging to it
 * @param groupInOrder : indicates the order of the node in the group
 */

class Node(var nodeRealVote: Float,  var groupId: Int, groupInOrder: Int) {

  // each node is identified by its unique couple of groupId and its order within its group
  private var nodeId  = " " + groupId+groupInOrder


  var numberOfClients = 0

  // individual tally
  private var individualTally : Float = 0

  // local tally
  private var localTally : Float = 0


  // list of proxies and office mates
  private var proxies  = new ListBuffer[Node]()
  private var officeMates = new ListBuffer[Node]()

  // each node have to keep the list of all local tallies coming from the previous group -- S set of the algorithm
  private var localTalliesBuffer = new ListBuffer[Float]()

  // these variables are used to compute the global votes -- T set of the algorithm
  private var localTalliesList  = new ListBuffer[Float]()

  /** methods*/
  def getNodeVote(): Float =  this.nodeRealVote
  def setNodeVote( nodeRealVote : Float ) : Unit = {this.nodeRealVote = nodeRealVote}

  def getNodeOrderInsideGroup(): Int = this.groupInOrder

  def getNodeProxies(): ListBuffer[Node] =  this.proxies

  def addToNodeProxies(proxy :  Node): Unit =  this.proxies += proxy

  def setNodeProxies(proxies :  ListBuffer[Node]) : Unit = {this.proxies = proxies}

  def getNodeOfficeMates(): ListBuffer[Node] =  this.officeMates
  def setNodeOfficeMates(officeMates : ListBuffer[Node]) : Unit = {this.officeMates = officeMates}

  def getLocalTalliesBuffer() : ListBuffer[Float] = this.localTalliesBuffer
  def setLocalTalliesBuffer(localTalliesBuffer : ListBuffer[Float]) : Unit = {this.localTalliesBuffer = localTalliesBuffer}

  def getLocalTalliesList() : ListBuffer[Float] = localTalliesList
  def setLocalTalliesList(localTalliesList : ListBuffer[Float]) : Unit = {this.localTalliesList = localTalliesList}

  def getNodeID() : String = this.nodeId
  def setNodeId( nodeId : String) = {this.nodeId = nodeId}

  def getNumberOfClients() : Int = this.numberOfClients

  def getIndividualTally() : Float = this.individualTally
  def setIndividualTally(individualTally: Float)  = this.individualTally = individualTally

  def getLocalTally() : Float = this.localTally
  def setLocalTally(localTally: Float)  = this.localTally = localTally


  def addToIndividualTally(value: Float): Unit ={
    this.individualTally += value
  }

  def addToLocalTalliesList(localTally: Float): Unit ={
    this.localTalliesList += localTally
  }

  def sendVotes(): Unit ={
    var b = this.nodeRealVote
    for (proxy <- this.getNodeProxies()){
      proxy.addToIndividualTally(b)
      b = -b
    }
  }

  def broadcastIndividualTally(): Unit ={
    for (mate <- this.officeMates){
      mate.localTally += this.individualTally
    }
  }

  def broadcastLocalTally(): Unit ={
    for (proxy <- this.getNodeProxies()){
      proxy.addToLocalTalliesList(this.localTally)
    }
  }

  def addToLocalTally(value: Float): Unit ={
    this.localTally += value
  }


  def addToLocalTalliesBuffer(sentLocalTally: Float): Unit ={
    this.localTalliesBuffer += sentLocalTally
  }


  def forwardLocalTally(): Unit ={
    for (proxy <- this.getNodeProxies()){
      proxy.addToLocalTalliesBuffer(this.localTally)
    }
  }

  def clearBuffer(): Unit = this.localTalliesBuffer.clear()


  def describeNode(): String = "Im Node: " +nodeId+ ", I voted for "+ nodeRealVote+" I have "+this.proxies.length+" proxies and "+this.numberOfClients+" Clients \n"


  def details(): Unit ={
    println("Im "+nodeId)
    println("My proxies",this.proxies.length)
    for (proxy <- this.proxies) println(proxy.nodeId)
    println("My Office Mates",this.officeMates.length)
    for (mate <- this.officeMates) println(mate.nodeId)

  }


  def getVotingResults(): Unit = {
    println("Im participant "+this.nodeId+" The voting results are "+this.localTalliesList.sum)
  }




}
