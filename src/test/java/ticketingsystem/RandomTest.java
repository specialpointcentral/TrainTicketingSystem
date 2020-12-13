package ticketingsystem;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("RandomTest")
public class RandomTest {
    int threadnum = 8;
    int routenum = 20; // route is designed from 1 to 3
    int coachnum = 10; // coach is arranged from 1 to 5
    int seatnum = 100; // seat is allocated from 1 to 20
    int stationnum = 16; // station is designed from 1 to 5

    int testnum = 640000;
    final static int RETPC = 10; // return ticket operation is 10% percent
    final static int BUYPC = 30; // buy ticket operation is 30% percent
    final static int INQPC = 100; // inquiry ticket operation is 60% percent

    private String passengerName() {
        Random rand = new Random(System.currentTimeMillis());
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    @Test
    @DisplayName("RandomTest - beginTest")
    void beginTest() throws InterruptedException {

        final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

        Thread[] threads = new Thread[threadnum];
        final long startTime = System.nanoTime();
        
        for (int i = 0; i < threadnum; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    Random rand = new Random(System.currentTimeMillis());
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

                    for (int i = 0; i < testnum; i++) {
                        int sel = rand.nextInt(INQPC);
                        // refund ticket
                        if (0 <= sel && sel < RETPC && !soldTicket.isEmpty()) {
                            int select = rand.nextInt(soldTicket.size());
                            if ((ticket = soldTicket.remove(select)) != null) {
                                long preTime = System.nanoTime() - startTime;
                                if (tds.refundTicket(ticket)) {
                                    // long postTime = System.nanoTime() - startTime;
                                    // System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " "
                                    //         + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " "
                                    //         + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
                                    //         + ticket.arrival + " " + ticket.seat);
                                    // System.out.flush();
                                } else {
                                    System.err.println(preTime + " " + String.valueOf(System.nanoTime() - startTime)
                                            + " " + ThreadId.get() + " " + "ErrOfRefund");
                                    fail("Err: cannot refund ticket");
                                    assert false : "Err: cannot refund ticket";
                                }
                            } else {
                                long preTime = System.nanoTime() - startTime;
                                System.err.println(preTime + " " + String.valueOf(System.nanoTime() - startTime) + " "
                                        + ThreadId.get() + " " + "ErrOfRefund");
                                assertNotNull(ticket, "Err: soldTicket out of bounds");
                                assert false : "Err: soldTicket out of bounds";
                            }
                        } else
                        // buy ticket
                        if (RETPC <= sel && sel < BUYPC) {
                            String passenger = passengerName();
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
                                                                                                // greater than
                                                                                                // departure
                            // long preTime = System.nanoTime() - startTime;
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                                // long postTime = System.nanoTime() - startTime;
                                // System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " "
                                //         + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " "
                                //         + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
                                //         + ticket.arrival + " " + ticket.seat);
                                soldTicket.add(ticket);
                                // System.out.flush();
                            } else {
                                // System.out.println(preTime + " " + String.valueOf(System.nanoTime() - startTime) + " "
                                //         + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure + " "
                                //         + arrival);
                                // System.out.flush();
                            }
                        } else
                        // inquiry ticket
                        if (BUYPC <= sel && sel < INQPC) {

                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
                                                                                                // greater than
                                                                                                // departure
                            // long preTime = System.nanoTime() - startTime;
                            int leftTicket = tds.inquiry(route, departure, arrival);
                            // long postTime = System.nanoTime() - startTime;
                            // System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "RemainTicket"
                            //         + " " + leftTicket + " " + route + " " + departure + " " + arrival);
                            // System.out.flush();
                        }
                    }
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < threadnum; i++) {
            threads[i].join();
        }
    }
}
