import java.util.Scanner;

import com.josephmate.ChatServiceGrpc;
import com.josephmate.ChatServiceOuterClass.SendMessageToServer;
import com.josephmate.ChatServiceOuterClass.ServerBroadcastsToEveryone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class ChatClientMain {
    @Option(
        name = "--port",
        usage = "the port to connect to (assumes localhost)",
        required = true)
    private int port;

    public static void main(String[] args) throws Exception {
        ChatClientMain chatClientMain = new ChatClientMain();
        CmdLineParser parser = new CmdLineParser(chatClientMain);
        parser.parseArgument(args);
        chatClientMain.run();
    }

    private void run() throws Exception {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
            .usePlaintext() // insecure
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // allow any left over connections to finish what their doing.
            while (!channel.isTerminated()) {
                channel.shutdown();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        ChatServiceGrpc.ChatServiceStub stub = ChatServiceGrpc.newStub(channel);
        chat(stub);
    }

    private void chat(ChatServiceGrpc.ChatServiceStub stub) {
        StreamObserver<SendMessageToServer> sendToServerObs = stub.chat(new StreamObserver<ServerBroadcastsToEveryone>() {
            @Override
            public void onNext(ServerBroadcastsToEveryone serverBroadcastsToEveryone) {
                System.out.println("From Server: " + serverBroadcastsToEveryone.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            sendToServerObs.onNext(SendMessageToServer.newBuilder()
                .setMessage(input)
                .build());
        }
    }
}
