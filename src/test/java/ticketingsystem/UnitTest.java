package ticketingsystem;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * unit test This test is only run in single thread.
 */
public class UnitTest {
    int routenum = 3; // route is designed from 1 to 3
    int coachnum = 5; // coach is arranged from 1 to 5
    int seatnum = 10; // seat is allocated from 1 to 20
    int stationnum = 8; // station is designed from 1 to 5

    int testnum = 1000;

    long startTime;

    private String passengerName() {
        Random rand = new Random(System.currentTimeMillis());
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    public UnitTest() {
        this(3, 5, 10, 8, 1000);
    }

    public UnitTest(int routenum, int coachnum, int seatnum, int stationnum, int testnum) {
        this.routenum = Math.max(routenum, 3);
        this.coachnum = Math.max(coachnum, 2);
        this.seatnum = Math.max(seatnum, 5);
        this.stationnum = stationnum;
        this.testnum = testnum;
    }

    public boolean beginTest() throws InterruptedException {
        startTime = System.nanoTime();
        boolean result = true;
        if (testBuyTicket()) {
            System.out.println("Test BuyTicket Pass!");
            System.out.flush();
        } else {
            System.err.println("Test BuyTicket Failure!");
            result = false;
        }

        if (testRefundTicket()) {
            System.out.println("Test RefundTicket Pass!");
            System.out.flush();
        } else {
            System.err.println("Test RefundTicket Failure!");
            result = false;
        }

        if (testInquiryTicket()) {
            System.out.println("Test InquiryTicket Pass!");
            System.out.flush();
        } else {
            System.err.println("Test InquiryTicket Failure!");
            result = false;
        }

        return result;
    }

    private boolean testBuyTicket() throws InterruptedException {
        final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, 1);
        int route = routenum;
        int departure = 1;
        int arrival = stationnum;
        int beginTickets = tds.inquiry(route, departure, arrival);
        long preTime = System.nanoTime() - startTime;
        if (beginTickets != seatnum * coachnum) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(
                    preTime + " " + postTime + " " + "testBuyTicket Test0: beginTickets=" + beginTickets + ".");
            fail("Err: Inquiry wrong seats!");
            assert false : "Err: Inquiry wrong seats!";
            return false;
        }

        /**
         * 1. Test only buy a ticket
         */
        preTime = System.nanoTime() - startTime;
        String passenger = passengerName();
        // rand a ticket
        Random rand = new Random(System.currentTimeMillis());
        departure = rand.nextInt(stationnum - 2) + 2; // 1 to 2 is reverse
        arrival = departure + rand.nextInt(stationnum - departure) + 1;
        Ticket ticket = tds.buyTicket(passenger, route, departure, arrival);
        if (ticket == null) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testBuyTicket Test1: Cannot buy a ticket!");
            fail("Err: Cannot buy a ticket!");
            assert false : "Err: Cannot buy a ticket!";
            return false;
        }
        int remainTickets = tds.inquiry(route, departure, arrival);
        if (remainTickets != (beginTickets - 1)) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testBuyTicket Test1: Return error ticket number "
                    + remainTickets + ".");
            fail("Err: Return error ticket number!");
            assert false : "Err: Return error ticket number!";
            return false;
        }
        /**
         * 2. Test buy all tickets
         */
        preTime = System.nanoTime() - startTime;
        // first we need buy all tickets - 1
        for (int i = 0; i < remainTickets; ++i) {
            if ((ticket = tds.buyTicket(passenger, route, 1, stationnum)) == null) {
                long postTime = System.nanoTime() - startTime;
                System.err.println(preTime + " " + postTime + " "
                        + "testBuyTicket Test2.1: Cannot buy a ticket in turn " + i + ".");
                fail("Err: Cannot buy a ticket!");
                assert false : "Err: Cannot buy a ticket!";
                return false;
            }
        }
        // second we find what we can buy
        remainTickets = tds.inquiry(route, 1, stationnum);
        if (remainTickets != 0) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testBuyTicket Test2.2: Return error ticket number "
                    + remainTickets + ".");
            fail("Err: Return error ticket number!");
            assert false : "Err: Return error ticket number!";
            return false;
        }
        // third, we try to buy 1 to 2, 1 tickets remain
        if ((ticket = tds.buyTicket(passenger, route, 1, 2)) == null) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testBuyTicket Test2.3: Cannot buy a ticket!");
            fail("Err: Cannot buy a ticket!");
            assert false : "Err: Cannot buy a ticket!";
            return false;
        }
        remainTickets = tds.inquiry(route, 1, 2);
        if (remainTickets != 0) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testBuyTicket Test2.3: Return error ticket number "
                    + remainTickets + ".");
            fail("Err: Return error ticket number!");
            assert false : "Err: Return error ticket number!";
            return false;
        }
        /**
         * 3. Test overbound situation
         */
        if ((ticket = tds.buyTicket(passenger, route, 1, 2)) != null) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(
                    preTime + " " + postTime + " " + "testBuyTicket Test3: Can buy a ticket when 0 ticket remain???");
            fail("Err: Can buy a ticket when 0 ticket remain!");
            assert false : "Err: Can buy a ticket when 0 ticket remain!";
            return false;
        }
        return true;
    }

    private boolean testRefundTicket() throws InterruptedException {
        final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, 1);
        int route = routenum - 1;
        int departure = 1;
        int arrival = stationnum;
        int beginTickets = tds.inquiry(route, departure, arrival);
        long preTime = System.nanoTime() - startTime;
        if (beginTickets != seatnum * coachnum) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(
                    preTime + " " + postTime + " " + "testRefundTicket Test0: beginTickets=" + beginTickets + ".");
            fail("Err: Inquiry wrong seats!");
            assert false : "Err: Inquiry wrong seats!";
            return false;
        }
        // rand a ticket
        Random rand = new Random(System.currentTimeMillis());
        departure = rand.nextInt(stationnum - 1) + 1;
        arrival = departure + rand.nextInt(stationnum - departure) + 1;
        /**
         * 1. Test only buy a ticket then refund a ticket
         */
        preTime = System.nanoTime() - startTime;
        String passenger = passengerName();

        Ticket ticket = tds.buyTicket(passenger, route, departure, arrival);
        if (ticket == null || !tds.refundTicket(ticket)) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testRefundTicket Test1: Cannot buy/refund a ticket!");
            fail("Err: Cannot buy a ticket!");
            assert false : "Err: Cannot buy a ticket!";
            return false;
        }
        int remainTickets = tds.inquiry(route, departure, arrival);
        if (remainTickets != beginTickets) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testRefundTicket Test1: Return error ticket number "
                    + remainTickets + ".");
            fail("Err: Return error ticket number!");
            assert false : "Err: Return error ticket number!";
            return false;
        }
        /**
         * 2. Test refund a ticket with wrong ticket info
         */
        if (tds.refundTicket(ticket)) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testRefundTicket Test2: Cannot refund a ticket!");
            fail("Err: Cannot buy a ticket!");
            assert false : "Err: Cannot buy a ticket!";
            return false;
        }
        remainTickets = tds.inquiry(route, departure, arrival);
        if (remainTickets != beginTickets) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(preTime + " " + postTime + " " + "testRefundTicket Test2: Return error ticket number "
                    + remainTickets + ".");
            fail("Err: Return error ticket number!");
            assert false : "Err: Return error ticket number!";
            return false;
        }
        return true;
    }

    private boolean testInquiryTicket() throws InterruptedException {
        final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, 1);
        int route = routenum - 2;
        int departure = 1;
        int arrival = stationnum;
        int beginTickets = tds.inquiry(route, departure, arrival);
        long preTime = System.nanoTime() - startTime;
        String passenger = passengerName();
        Random rand = new Random(System.currentTimeMillis());

        if (beginTickets != seatnum * coachnum) {
            long postTime = System.nanoTime() - startTime;
            System.err.println(
                    preTime + " " + postTime + " " + "testInquiryTicket Test0: beginTickets=" + beginTickets + ".");
            fail("Err: Inquiry wrong seats!");
            assert false : "Err: Inquiry wrong seats!";
            return false;
        }
        /**
         * 1. Test inquiry by sold some tickets
         */
        preTime = System.nanoTime() - startTime;
        for (int i = 1; i < beginTickets / 2; ++i) {
            if (tds.buyTicket(passenger, route, departure, arrival) == null
                    || tds.inquiry(route, departure, arrival) != beginTickets - i) {
                long postTime = System.nanoTime() - startTime;
                System.err.println(
                        preTime + " " + postTime + " " + "testInquiryTicket Test1: beginTickets=" + beginTickets + ".");
                fail("Err: Inquiry wrong seats!");
                assert false : "Err: Inquiry wrong seats!";
                return false;
            }
        }
        /**
         * 2. Test inquiry by sold/refunded some tickets
         */
        beginTickets = tds.inquiry(route, departure, arrival);
        int sell = rand.nextInt(beginTickets / 2 - 2) + 1;
        int buy = rand.nextInt(beginTickets / 2 - 2) + 1;
        sell = Math.max(sell, buy);
        preTime = System.nanoTime() - startTime;
        ArrayList<Ticket> tks = new ArrayList<Ticket>();
        Ticket tic = null;
        for (int i = 1; i < buy; ++i) {
            if ((tic = tds.buyTicket(passenger, route, departure, arrival)) == null
                    || tds.inquiry(route, departure, arrival) != beginTickets - i) {
                long postTime = System.nanoTime() - startTime;
                System.err.println(
                        preTime + " " + postTime + " " + "testInquiryTicket Test2: beginTickets=" + beginTickets + ".");
                fail("Err: Inquiry wrong seats!");
                assert false : "Err: Inquiry wrong seats!";
                return false;
            }
            tks.add(tic);
        }

        for (int i = 1; i < sell; ++i) {
            tic = tks.get(rand.nextInt(tks.size()));
            if (!tds.refundTicket(tic) || tds.inquiry(route, departure, arrival) != beginTickets - i) {
                long postTime = System.nanoTime() - startTime;
                System.err.println(
                        preTime + " " + postTime + " " + "testInquiryTicket Test2: beginTickets=" + beginTickets + ".");
                fail("Err: Inquiry wrong seats!");
                assert false : "Err: Inquiry wrong seats!";
                return false;
            }
            tks.remove(tic);
        }
        return true;
    }
}
