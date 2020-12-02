package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

/**
 * unit test This test is only run in single thread.
 */
@DisplayName("MultiThreadTest")
public class MultiThreadTest {
    protected static final ExecutorService pool = Executors.newCachedThreadPool();
    // barrier for making all threads begin at same time
    protected CyclicBarrier barrier;
    // buy tickest
    List<Ticket> soldTicket = Collections.synchronizedList(new ArrayList<Ticket>());
    List<int[]> cannotSoldTicket = Collections.synchronizedList(new ArrayList<int[]>());
    AtomicInteger soldTicketNum = new AtomicInteger(0);
    AtomicInteger refundTicketNum = new AtomicInteger(0);
    AtomicInteger realRefundTicketNum = new AtomicInteger(0);

    int opForRoute;
    // ticketing date struct
    int threadnum = 16;
    int routenum = 6; // route is designed from 1 to 6
    int coachnum = 8; // coach is arranged from 1 to 8
    int seatnum = 100; // seat is allocated from 1 to 100
    int stationnum = 8; // station is designed from 1 to 8
    final static int TESTNUM = 10;
    private byte[] lock = new byte[0];
    protected TicketingDS tds;

    private String passengerName() {
        Random rand = new Random(System.currentTimeMillis());
        long uid = rand.nextLong();
        return "passenger" + uid;
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        int currentRepetition = repetitionInfo.getCurrentRepetition();
        int totalRepetitions = repetitionInfo.getTotalRepetitions();
        String methodName = testInfo.getTestMethod().get().getName();
        System.out.println(
                String.format("Execute repetition %d of %d for %s", currentRepetition, totalRepetitions, methodName));
        System.out.flush();

        initData();
    }

    private void initData() {
        Random rand = new Random(System.currentTimeMillis());
        opForRoute = rand.nextInt(routenum) + 1;
        tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum * 2);
        soldTicket.clear();
        cannotSoldTicket.clear();
        soldTicketNum.set(0);
        refundTicketNum.set(0);
        realRefundTicketNum.set(0);
    }

    @RepeatedTest(value = TESTNUM, name = "{displayName} {currentRepetition}/{totalRepetitions}")
    void buyAndRefundTickets() {
        barrier = new CyclicBarrier(threadnum * 2 + 1);
        try {
            for (int i = 0; i < threadnum; i++) {
                pool.execute(new BuyTickets());
                pool.execute(new RefundTickets());
            }
            barrier.await(); // waiting for ready
            barrier.await(); // waiting for finish
            // sold tickets number must bigger than all seats number
            assertTrue(soldTicketNum.get() >= coachnum * seatnum,
                    "Sold tickets number must bigger than all seats number");
            // refundTicketNum must equal to realRefundTicketNum
            assertEquals(refundTicketNum.get(), realRefundTicketNum.get(),
                    "Value refundTicketNum must equal to realRefundTicketNum");
            // remainTicketNum need equal to sold - refund
            int remainTicketNum = tds.inquiry(opForRoute, stationnum / 2, stationnum / 2 + 1);
            int cannotSoldTicketNum = cannotSoldTicket.size();
            assertEquals(remainTicketNum, coachnum * seatnum - soldTicketNum.get() + refundTicketNum.get(),
                    "Value remainTicketNum need equal to all - soldTicketNum + refundTicketNum");

        } catch (Exception e) {
            fail("RuntimeException: " + e.getMessage());
        }
    }

    class BuyTickets implements Runnable {

        @Override
        public void run() {
            Random rand = new Random(System.currentTimeMillis());
            try {
                barrier.await();
                int buyTicketNum = rand.nextInt(coachnum * seatnum / threadnum) + (coachnum * seatnum / threadnum);
                for (int i = 0; i < buyTicketNum; ++i) {
                    // we make all of them pass station stationnum / 2
                    int departure = rand.nextInt(stationnum / 2) + 1;
                    int arrival = stationnum / 2 + rand.nextInt(stationnum - stationnum / 2) + 1;
                    String passenger = passengerName();
                    Ticket ticket = tds.buyTicket(passenger, opForRoute, departure, arrival);
                    Thread.sleep(rand.nextInt(100));
                    if (ticket != null) {
                        System.err.printf("B: %d-%d\t(%d)->(%d)\n", ticket.coach, ticket.seat, ticket.departure,
                                ticket.arrival);
                        soldTicketNum.getAndIncrement();
                        soldTicket.add(ticket);
                    } else {
                        int[] t = new int[2];
                        t[0] = departure;
                        t[1] = arrival;
                        cannotSoldTicket.add(t);
                    }
                }
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class RefundTickets implements Runnable {

        @Override
        public void run() {
            try {
                barrier.await();
                Ticket tic = null;
                boolean refundOK = false;
                Random rand = new Random(System.currentTimeMillis());
                Thread.sleep(rand.nextInt(100));
                while (!soldTicket.isEmpty()) {
                    synchronized (lock) {
                        tic = null;
                        if (!soldTicket.isEmpty()) {
                            tic = soldTicket.get(rand.nextInt(soldTicket.size()));
                            soldTicket.remove(tic);
                        }
                    }
                    Thread.sleep(rand.nextInt(200));
                    if (tic != null) {
                        System.err.printf("R: %d-%d\t(%d)->(%d)\n", tic.coach, tic.seat, tic.departure, tic.arrival);
                        refundTicketNum.getAndIncrement();
                        refundOK = tds.refundTicket(tic);
                        if (refundOK) {
                            realRefundTicketNum.getAndIncrement();
                        }
                    }
                }
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);

            }
        }

    }
}
