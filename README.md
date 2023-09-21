# JavaFxAsyncGrpc
Combining JavaFx with AsyncGrpc as a visual education tool

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

