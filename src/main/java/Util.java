import java.util.Scanner;

import com.josephmate.DemoServiceOuterClass;

public class Util {
    public static int readOneInt() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(input);
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        return -1; // dead code
    }
}
