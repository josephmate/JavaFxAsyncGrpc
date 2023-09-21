# What is async?

Instead of blocking until a result is ready, you provide a callback function that is called when
it completes. This frees up the thread to do something else. UI developers are really good at
writing and understanding async code to prevent rendering from blocking on long API operations.

A typical async example could look something like
```shell
API Request Worker Thread               Database Worker Thread
1. request XYZ comes in                 1. working on handling the result from a API ABC
2. put callback into database           |
   worker queue                         |
3. thread works on next API request     2. result from database for XYZ is ready but worker
                                            thread still working on callback for ABC  
                                        3. XYZ finished, moving on to ABC
                                        4. use callback from ABC to finish off the API request
                                           using result from database         
```

# Async vs. Synchronous

| Dimension       | Asynchronous              | Synchronous                      |
|-----------------|---------------------------|----------------------------------|
| Throughput      | High                      | Low                              |
| Efficiency      | High                      | Low                              |
| Simplicity      | Low                       | High                             |

# Examples of async

1. gRPC stream observer (this experiment)
2. Vert.x
3. Netty
4. Spring Webflux
5. R2DBC
2. JDK 21 Project Loom


# gRPC Async
Async callbacks are done thru the StreamObserver interface.
The two most important methods are onNext and onComplete.
onNext is when the next message from the other side is ready.
onComplete is when the other side is done sending stuff.

# Gotchyas
Mixing async and synchronous code is challenging.
If async is handled properly, a response can be returned when the server is still processing the request!
You might have an HTTP Server like spring web that responses to requests asynchronously, but invoking
async operations against gRPC services or databases.

This experiment demonstrates this problem.


# Running gRPC Experiments

Terminal 1:
```shell
java -cp .\target\java-fx-async-grpc-1-shaded.jar ServerMain
> Server running on port: 64610
> 
```

Terminal 2:
```shell
java -cp .\target\java-fx-async-grpc-1-shaded.jar ClientMain --method AllSynchronous --port 64610

```

```shell
java -cp .\target\java-fx-async-grpc-1-shaded.jar ClientMain --method OnlyResponseAsync --port 64610

```

```shell
java -cp .\target\java-fx-async-grpc-1-shaded.jar ClientMain --method OnlyResponseAsyncBroken --port 64610

```

```shell
java -cp .\target\java-fx-async-grpc-1-shaded.jar ClientMain --method BidirectionalAsync --port 64610

```


# Homework questions

How can you send gigantic response that consumes too much memory using a stream response?

How would you use bi-direction gRPC to implement a chat server?
