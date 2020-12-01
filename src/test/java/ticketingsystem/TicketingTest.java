package ticketingsystem;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TicketingTest {

    @Test
    public void mainTest() throws InterruptedException {
        System.out.println("===== Begin Random Test =====");
        System.out.flush();
        RandomTest rtest = new RandomTest();
        try {
            rtest.beginTest();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("===== Random Test End =====");
            System.out.flush();
        }
    }
}
