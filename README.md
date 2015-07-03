# akka-distributed-quicksort-demo
Application that demonstrates how to use akka to solve distributed recursive problems utilizing quicksort as an example.

Classes using the work queue approach are located in com.tsykul.forkjoin.distributed.queue package.

To run the workers start com.tsykul.forkjoin.distributed.queue.runner.BootWorker specifying -Dakka.remote.netty.tcp.port=<port>
The first two nodes should be started on ports 2551 and 2552.
Client is just an actor that creates a single task and sends it for execution.
It is started on the same node as work queue in this example.
(In real app that would most likely be a separate node using Akka cluster client)
To run the client and the queue start com.tsykul.forkjoin.distributed.queue.runner.BootQueueAndClient and specify the port for remoting in the same way it was specified for workers.

Classes using the actor hierarcy approach are located in com.tsykul.forkjoin.distributed.hierarchy package.
To run the workers start com.tsykul.forkjoin.distributed.hierarchy.runner.BootWorker specifying -Dakka.remote.netty.tcp.port=<port>
To run the client start com.tsykul.forkjoin.distributed.queue.runner.BootClient and specify the port for remoting in the same way it was specified for workers.
