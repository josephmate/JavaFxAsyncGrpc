import java.util.Scanner;

import com.josephmate.DemoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
        OnlyRequestAsync,
        AllSynchronous
    }

    public static void main(String[] args) throws Exception {
        ClientMain clientMain = new ClientMain();
        CmdLineParser parser = new CmdLineParser(clientMain);
        parser.parseArgument(args);
        clientMain.run();
    }

    private void run() {
        switch (method) {
            case AllSynchronous:
                allSynchronous();
                break;
            default:
                throw new UnsupportedOperationException(method + " not implemented yet");
        }
    }

    private void allSynchronous() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
            .usePlaintext() // insecure
            .build();

        DemoServiceGrpc.DemoServiceBlockingStub blockingStub = DemoServiceGrpc.newBlockingStub(channel);

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'end' or 'stop' to send the request.");
        SynchronousCompleteRequest.Builder builder = SynchronousCompleteRequest.newBuilder();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("send") || input.equalsIgnoreCase("stop") || input.equalsIgnoreCase("end")) {
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

        channel.shutdownNow();
    }
}
