import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import com.josephmate.DemoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import com.josephmate.DemoServiceOuterClass.AsyncPartialRequest;
import com.josephmate.DemoServiceOuterClass.AsyncPartialResponse;
import com.josephmate.DemoServiceOuterClass.SynchronousCompleteRequest;
import com.josephmate.DemoServiceOuterClass.SynchronousCompleteResponse;

public class ClientMain {
    @Option(
        name = "--port",
        usage = "the port to connect to (assumes localhost)",
        required = true
    )
    private int port;
    @Option(
        name = "--method",
        usage = "the grpc method to call",
        required = true
    )
    private Method method;

    enum Method {
        BidirectionalAsync,
        OnlyResponseAsync,
        OnlyResponseAsyncBroken,
        AllSynchronous
    }

    public static void main(String[] args) throws Exception {
        ClientMain clientMain = new ClientMain();
        CmdLineParser parser = new CmdLineParser(clientMain);
        parser.parseArgument(args);
        clientMain.run();
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

        switch (method) {
            case AllSynchronous:
                allSynchronous(channel);
                break;
            case OnlyResponseAsync:
                onlyResponseAsync(channel);
                break;
            case OnlyResponseAsyncBroken:
                onlyResponseAsyncBroken(channel);
                break;
            case BidirectionalAsync:
                bidirectionalAsync(channel);
                break;
            default:
                throw new UnsupportedOperationException(method + " not implemented yet");
        }
    }

    private void allSynchronous(ManagedChannel channel) {
        DemoServiceGrpc.DemoServiceBlockingStub blockingStub = DemoServiceGrpc.newBlockingStub(channel);

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'end' or 'stop' to send the request.");
        SynchronousCompleteRequest.Builder builder = SynchronousCompleteRequest.newBuilder();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (
                input.equalsIgnoreCase("send")
                    || input.equalsIgnoreCase("stop")
                    || input.equalsIgnoreCase("end")
            ) {
                break;
            }

            try {
                int number = Integer.parseInt(input);
                builder.addCounter(number);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number, stop, or end");
            }
        }
        scanner.close();

        SynchronousCompleteResponse response = blockingStub.allSynchronous(builder.build());
        System.out.println("Got response back from server:");
        for(int val : response.getCounterList()) {
            System.out.println(val);
        }
    }

    private void onlyResponseAsync(ManagedChannel channel) throws Exception {
        DemoServiceGrpc.DemoServiceStub stub = DemoServiceGrpc.newStub(channel);

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'end' or 'stop' to send the request.");
        SynchronousCompleteRequest.Builder builder = SynchronousCompleteRequest.newBuilder();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (
                input.equalsIgnoreCase("send")
                    || input.equalsIgnoreCase("stop")
                    || input.equalsIgnoreCase("end")
            ) {
                break;
            }

            try {
                int number = Integer.parseInt(input);
                builder.addCounter(number);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number, stop, or end");
            }
        }
        scanner.close();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("Receiving numbers from server:");
        stub.onlyResponseAsync(builder.build(), new StreamObserver<AsyncPartialResponse>() {
            @Override
            public void onNext(AsyncPartialResponse asyncPartialResponse) {
                System.out.println(asyncPartialResponse.getCounter());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        System.out.println("Server completed response");
    }

    /**
     * For some reason this one only works on a fresh server.
     * Not going to spend time debugging it.
     *
     * @param channel
     * @throws Exception
     */
    private void onlyResponseAsyncBroken(ManagedChannel channel) throws Exception {
        DemoServiceGrpc.DemoServiceStub stub = DemoServiceGrpc.newStub(channel);

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'end' or 'stop' to send the request.");
        SynchronousCompleteRequest.Builder builder = SynchronousCompleteRequest.newBuilder();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (
                input.equalsIgnoreCase("send")
                    || input.equalsIgnoreCase("stop")
                    || input.equalsIgnoreCase("end")
            ) {
                break;
            }

            try {
                int number = Integer.parseInt(input);
                builder.addCounter(number);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number, stop, or end");
            }
        }
        scanner.close();

        System.out.println("Receiving numbers from server:");
        stub.onlyResponseAsync(builder.build(), new StreamObserver<AsyncPartialResponse>() {
            @Override
            public void onNext(AsyncPartialResponse asyncPartialResponse) {
                System.out.println(asyncPartialResponse.getCounter());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
            }
        });

        System.out.println("Server completed response");
    }

    private void bidirectionalAsync(ManagedChannel channel) throws Exception {
        DemoServiceGrpc.DemoServiceStub stub = DemoServiceGrpc.newStub(channel);

        final CountDownLatch allDoneLatch = new CountDownLatch(1);
        System.out.println("Receiving numbers from server:");

        BiDirectionalObserver biDirectionalObserver = new BiDirectionalObserver();
        StreamObserver<AsyncPartialRequest> startPingPong = stub.bidirectionalAsync(biDirectionalObserver);
        biDirectionalObserver.talkToServer = startPingPong;
        biDirectionalObserver.allDoneLatch = allDoneLatch;


        // this message begins the back and forth
        System.out.println("Give a number to send to the server:");
        int number = Util.readOneInt();
        startPingPong.onNext(AsyncPartialRequest.newBuilder()
            .setCounter(number)
            .build());


        allDoneLatch.await();
        System.out.println("Server stopped. Conversation complete.");
    }
}

class BiDirectionalObserver implements StreamObserver<AsyncPartialResponse> {

    StreamObserver<AsyncPartialRequest> talkToServer;
    CountDownLatch allDoneLatch;

    @Override
    public void onNext(AsyncPartialResponse asyncPartialResponse) {
        int responseNum = asyncPartialResponse.getCounter();
        System.out.println("Server sent: ");
        System.out.println(responseNum);
        if (responseNum == -1) {
            System.out.println("Telling server to we're done.");
            talkToServer.onCompleted();
        } else {
            System.out.println("Send back a number:");
            int number = Util.readOneInt();
            talkToServer.onNext(AsyncPartialRequest.newBuilder()
                .setCounter(number)
                .build());
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        allDoneLatch.countDown();
    }
}