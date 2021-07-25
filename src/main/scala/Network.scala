import scala.collection.mutable.ListBuffer

/**
 *
 * authors : ( BEN AYAD, Mohamed Ayoub   -   EL HALOUMI, Hicham  )
 * Description : the actual class describes the Network structure, which models the actual life community
 * Date : 07/09/2019 - 11/09/2019
 * source : The Decentralized Polling algorithm with respectable participants.
 *
 * @param numOfGroups : the number of groups in the network (clusters) -- R parameter
 * @param lengthOfEachGroup :  the number of nodes per cluster -- |g_i| * R = N
 * @param K : it is the parameter K (privacy parameter).
 */

class Network(var numOfGroups : Int, var lengthOfEachGroup: Int,var K: Int) {

  //initialize the network (setup)
  private var (realVotes, listOfGroups ) = generateNetwork(numOfGroups,lengthOfEachGroup)

  def vote(): Unit ={
    for (participant <- this.listOfGroups){
      participant.sendVotes()
    }
  }

  def broadcastIndividualTally(): Unit ={
    for (participant <- this.listOfGroups){
      participant.broadcastIndividualTally()
    }
  }

  def incrementLocalTally(): Unit ={
    for (participant <- this.listOfGroups){
      participant.addToLocalTally(participant.getIndividualTally())
      participant.getLocalTalliesList() += participant.getLocalTally()
    }
  }

  def showLocalTallies(): Unit ={
    for (participant <- this.listOfGroups){
      println("For participant "+ participant.getNodeID() +" "+participant.getLocalTally())
    }
  }

  def showLocalTalliesList(): Unit ={
    for (participant <- this.listOfGroups){
      println(participant.getLocalTalliesList())
    }
  }


  // this function shows the results from a mater authority point of view, not based on the polling algorithm
  def masterView(): Unit ={
    var globalSum = 0.0
    for (i <- 0 until numOfGroups){
      var sum = 0.0
      for(j <- 0 until lengthOfEachGroup){
        sum += this.listOfGroups(i*lengthOfEachGroup+j).getNodeVote()
      }
      println("Sum of votes of GROUP "+i+" is ",sum)
      globalSum += sum
    }
    println("Sum of votes "+globalSum)
  }

  // this function describes each participant in the network
  def describeNetwork(): Unit ={
    for (participant <- this.listOfGroups){
      print(participant.describeNode())
    }
  }

  // this function shows the list of proxies and clients for each participant
  def details(): Unit ={
    for (participant <- this.listOfGroups){
      print(participant.details())
    }
  }

  // this function checks is the forwardintg is done, by testing the length of localTallyList for each participant
  def isLocalTallyForwardingDone(): Boolean ={
    for (participant <- this.listOfGroups){
      if  (participant.getLocalTalliesList().length < numOfGroups) return false
    }
    true
  }

  // this function forwards the last received localTally of a participant to its proxies, and stock them in the proxies buffer of local tallies
  def forwardFinalLocalTallyToProxies(): Unit ={
    for (participant <- this.listOfGroups){
      val lengthOfParticipantLocalTalliesList = participant.getLocalTalliesList().length
      for (proxy <- participant.getNodeProxies()){
        var lastLocalTally = participant.getLocalTalliesList()(lengthOfParticipantLocalTalliesList-1)
        proxy.getLocalTalliesBuffer() += lastLocalTally
      }
    }
  }

  // We choose the most dominant local tally from Buffer and add it to the localTallyList
  def fillLocalTalliesListFromBuffer(): Unit ={
    for (participant <- this.listOfGroups){
      // choose local tally from buffer
      participant.addToLocalTalliesList(participant.getLocalTalliesBuffer()(0)) // we assume all participants are honest, no need to choose the most representative one
      // we empty the buffer list for future broadcast.
      participant.clearBuffer()
    }
  }

  def showVotingResults(): Unit ={
    for (participant <- this.listOfGroups){
      participant.getVotingResults()
    }
  }

  // this funtion generates the network, and guarantees the randomness of the distribution and especially that each participant has at least one client
  private def generateNetwork(numOfGroups : Int,lengthOfEachGroup: Int): (Array[Float],ListBuffer[Node]) ={

    // generate N random votes that will e assigned to the network nodes
    val networkVotes  = new Array[Float](numOfGroups*lengthOfEachGroup)

    for (i<-0 to numOfGroups*lengthOfEachGroup - 1) {
      if ( scala.util.Random.nextDouble < 0.6 )
        networkVotes(i) = -1
      else
        networkVotes(i) = 1
    }

    // create a list of n * p nodes
    var arrayOfNodes =  ListBuffer[Node]()

    // filling the nodes details and assigning their votes:
    for (i <- 0 until numOfGroups ;  j <- 0 until lengthOfEachGroup){
      arrayOfNodes += new Node(networkVotes(i*lengthOfEachGroup + j),i,j)
    }

    // filling the proxies list
    for (i <- 0 until numOfGroups){
      val randomPermutation = scala.util.Random.shuffle(List.range(0,lengthOfEachGroup))
      var bufferOfRandomOIndices = randomPermutation.toBuffer

      // first round to guarentee that each node in the next group has at least 1 client.
        for (elt <-0 until lengthOfEachGroup){
          //   i,elt is the participant we are dealing with in the current group
          //   (i+1),randomPermutation(elt)  its his first proxy
          val validProxy = arrayOfNodes(((i+1)%numOfGroups)*lengthOfEachGroup +randomPermutation(elt))
          arrayOfNodes(i*lengthOfEachGroup +elt).addToNodeProxies(validProxy)
          validProxy.numberOfClients += 1

        }
        // know we add the rest of the proxies ( 2*K proxy randomly )
        for (participantInd <- 0 until lengthOfEachGroup){
          //   i , participantInd is the participant we are dealing with in the current group
          //   (participantInd+1),randomPermutation(elt)  its his first proxy
          var firstProxyIndice = arrayOfNodes(i*lengthOfEachGroup+participantInd).getNodeProxies()(0).getNodeOrderInsideGroup()
          // we subtract the first indice so it doesnt show up again in the proxies list
          bufferOfRandomOIndices -= firstProxyIndice
          for (proxyId <- 0 until 2*K ){
            val validProxy = arrayOfNodes(((i+1)%numOfGroups)*lengthOfEachGroup +bufferOfRandomOIndices(proxyId))
            arrayOfNodes(i*lengthOfEachGroup+participantInd).addToNodeProxies(validProxy)
            validProxy.numberOfClients += 1
          }
          // we add it again for later use
          // this way we guarenteed randomness and also that the ring would never get stucck, all participant would have at least one client
          bufferOfRandomOIndices += firstProxyIndice
        }
    }


    // assigning officeMates of Each Node
    for (i <- 0 until numOfGroups ;  j <- 0 until lengthOfEachGroup){
      var officeMates = new ListBuffer[Node]()
      for (k <- 0 until lengthOfEachGroup) {
        if (k != j) {
          officeMates += arrayOfNodes(i*lengthOfEachGroup +k)
        }
      }
      arrayOfNodes(i*lengthOfEachGroup +j).setNodeOfficeMates(officeMates)
    }
    ( networkVotes, arrayOfNodes )
  }

}
