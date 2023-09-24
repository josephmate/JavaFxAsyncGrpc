import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.josephmate.ChatServiceGrpc;
import com.josephmate.ChatServiceOuterClass.SendMessageToServer;
import com.josephmate.ChatServiceOuterClass.ServerBroadcastsToEveryone;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ChatServerMain {
    public static void main(String[] args) throws Exception {
        // port=0 means dynamically allocate the port.
        Server server = ServerBuilder.forPort(0)
            .addService(new ChatServerImpl())
            .build();
        server.start();
        System.out.println("Server running on port: " + server.getPort());
        System.out.println("Waiting for next request.");
        // keep running until killed
        server.awaitTermination();
    }
}

class ChatServerImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private List<StreamObserver<ServerBroadcastsToEveryone>> clientsToBroadcast = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger numClients = new AtomicInteger();

    @Override
    public StreamObserver<SendMessageToServer> chat(StreamObserver<ServerBroadcastsToEveryone> responseObserver) {
        int currentNum = numClients.getAndIncrement() + 1;
        System.out.println(currentNum + " clients connected.");
        clientsToBroadcast.add(responseObserver);
        return new StreamObserver<SendMessageToServer>() {
            @Override
            public void onNext(SendMessageToServer sendMessageToServer) {
                for(StreamObserver<ServerBroadcastsToEveryone> clientToBroadcast : clientsToBroadcast) {
                    String msgFromClient = sendMessageToServer.getMessage();
                    System.out.println("Message from client: " + msgFromClient);
                    ServerBroadcastsToEveryone msg = ServerBroadcastsToEveryone.newBuilder()
                        .setMessage(msgFromClient)
                        .build();
                    clientToBroadcast.onNext(msg);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
                // TODO: handle disconnects by keeping a map instead
            }
        };
    }

}