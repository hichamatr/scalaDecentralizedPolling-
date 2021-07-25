import scala.collection.mutable.ListBuffer

object Main {

  def main(args: Array[String]): Unit = {


    // We are going to use the class Network to generate our network
    val network = new Network(4,4,1)

    network.describeNetwork()
    // network.details()


    // For verification purposes, we can check: (Master authority) using the function network.masterView()


    // Voting round
    println("\n\nVoting round started!")
    network.vote()
    println("Voting round Done!\n\n")


    // broadcasting localTally inside each cluster apart

    println("Local broadcasting of individual tallies started in each group, Started !!")
    network.broadcastIndividualTally()
    println("Local broadcasting of individual tallies started in each group, Done !!\n\n")


    // increment localTally by individualTally <==>  line 3 of the polling algorithm
    network.incrementLocalTally()
    println("Local Tally incremented by individualTally, line 3 of algorithm\n\n")


    // Normally, each local tally of a participant should reflect the voting sum of the precedent group.
    // We can verify this using the function: network.showLocalTallies() :

    // println("Local Tallies of Each Participant")
    // network.showLocalTallies()  >> this one to show the localTallies of each participant
    // network.masterView()  >> this one to see the actual results, by the "master authority", mainly used for verifications purposes




    // Forwarding local tally alongside the ring
    var round = 0

    while(!network.isLocalTallyForwardingDone()){
      println("Forwarding Number: ",round)

      // Forward the current last
      network.forwardFinalLocalTallyToProxies()

      // Select local tally from buffer,
      // since we assumed all participants to be honest,
      // we are just choosing the first value from the buffer
      // and then free the buffer
      network.fillLocalTalliesListFromBuffer()

      round = round + 1
    }

    println("\n\nLocal Tallies List: ")
    network.showLocalTalliesList()

    println("\n\nVoting results of the distributed system:")
    network.showVotingResults()

    println("\n\nMaster View Results: ")
    network.masterView()

  }
}