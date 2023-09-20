import java.util.Scanner;

import com.josephmate.DemoServiceGrpc;
import com.josephmate.DemoServiceOuterClass.AsyncPartialRequest;
import com.josephmate.DemoServiceOuterClass.AsyncPartialResponse;
import com.josephmate.DemoServiceOuterClass.SynchronousCompleteRequest;
import com.josephmate.DemoServiceOuterClass.SynchronousCompleteResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        // port=0 means dynamically allocate the port.
        Server server = ServerBuilder.forPort(0)
            .addService(new ServerImpl())
            .build();
        server.start();
        System.out.println("Server running on port: " + server.getPort());
        System.out.println("Waiting for next request.");
        // keep running until killed
        server.awaitTermination();
    }
}

class ServerImpl extends DemoServiceGrpc.DemoServiceImplBase {

    @Override
    public StreamObserver<AsyncPartialRequest> bidirectionalAsync(StreamObserver<AsyncPartialResponse> responseObserver) {
        return new StreamObserver<AsyncPartialRequest>() {
            @Override
            public void onNext(AsyncPartialRequest asyncPartialRequest) {
                System.out.println("Received from client:");
                System.out.println(asyncPartialRequest.getCounter());
                System.out.println("Send something back to the client. -1 to stop");
                responseObserver.onNext(AsyncPartialResponse.newBuilder()
                    .setCounter(Util.readOneInt())
                    .build());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Client wants to stop");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void onlyResponseAsync(SynchronousCompleteRequest request, StreamObserver<AsyncPartialResponse> responseObserver) {
        System.out.println("Received the following data from the client:");
        for(int val : request.getCounterList()) {
            System.out.println(val);
        }

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'respond', 'end' or 'stop' to send the request.");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (
                input.equalsIgnoreCase("send")
                    || input.equalsIgnoreCase("respond")
                    || input.equalsIgnoreCase("stop")
                    || input.equalsIgnoreCase("end")
            ) {
                break;
            }

            try {
                int number = Integer.parseInt(input);

                responseObserver.onNext(AsyncPartialResponse.newBuilder()
                    .setCounter(number)
                    .build());

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number, stop, or end");
            }
        }
        scanner.close();

        responseObserver.onCompleted();
        System.out.println("Waiting for next request.");
    }

    @Override
    public StreamObserver<AsyncPartialRequest> onlyRequestAsync(StreamObserver<SynchronousCompleteResponse> responseObserver) {
        return super.onlyRequestAsync(responseObserver);
    }

    @Override
    public void allSynchronous(SynchronousCompleteRequest request, StreamObserver<SynchronousCompleteResponse> responseObserver) {
        System.out.println("Received the following data from the client:");
        for(int val : request.getCounterList()) {
            System.out.println(val);
        }

        System.out.println("Please enter numbers to send separated by newline.");
        System.out.println("enter 'send', 'respond', 'end' or 'stop' to send the request.");
        SynchronousCompleteResponse.Builder builder = SynchronousCompleteResponse.newBuilder();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (
                input.equalsIgnoreCase("send")
                    || input.equalsIgnoreCase("respond")
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

        responseObserver.onNext(builder.build());
        // cannot call onNext again since it's a sync response
        // not sure what will happen if you call it multiple times
        responseObserver.onCompleted();
        System.out.println("Waiting for next request.");
    }
}