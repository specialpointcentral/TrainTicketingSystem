package ticketingsystem.jmh.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.*;
import ticketingsystem.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 5)
@Fork(2)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerformanceBenchmark {
    @Param({ "1", "2", "4", "8", "16", "32", "64" })
    static int nThreads;

    private ExecutorService pool;

    final static int routenum = 20; // route is designed from 1 to 3
    final static int coachnum = 10; // coach is arranged from 1 to 5
    final static int seatnum = 100; // seat is allocated from 1 to 20
    final static int stationnum = 16; // station is designed from 1 to 5

    final static int testnum = 6400000;
    final static int retpc = 10; // return ticket operation is 10% percent
    final static int buypc = 30; // buy ticket operation is 30% percent
    final static int inqpc = 100; // inquiry ticket operation is 60% percent

    TicketingDS tds;
    ArrayList<Callable<Object>> list;

    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    @Setup
    public void init() {
        this.pool = Executors.newFixedThreadPool(nThreads);
        tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, nThreads);
        list = new ArrayList<Callable<Object>>();
        for (int i = 0; i < nThreads; i++) {
            list.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    singleTrace();
                    return null;
                }
            });
        }
    }

    @Benchmark
    public int performTrace() throws InterruptedException {
        pool.invokeAll(list);
        return nThreads;
    }

    @TearDown
    public void finish() {
        pool.shutdown();
    }

    private final void singleTrace() {
        Random rand = new Random();
        MyTicket ticket = new MyTicket();
        ArrayList<MyTicket> soldTicket = new ArrayList<MyTicket>();

        for (int i = 0; i < testnum; i++) {
            int sel = rand.nextInt(inqpc);
            // return ticket
            if (0 <= sel && sel < retpc && soldTicket.size() > 0) {
                int select = rand.nextInt(soldTicket.size());
                if ((ticket = soldTicket.remove(select)) != null) {
                    if (!tds.refundTicket(ticket)) {
                        System.out.println("ErrOfRefund");
                        System.out.flush();
                    }
                } else {
                    System.out.println("ErrOfRefund");
                    System.out.flush();
                }
            } else
            // buy ticket
            if (retpc <= sel && sel < buypc) {
                String passenger = passengerName();
                int route = rand.nextInt(routenum) + 1;
                int departure = rand.nextInt(stationnum - 1) + 1;
                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
                                                                                    // greater than
                                                                                    // departure
                if ((ticket = (MyTicket)tds.buyTicket(passenger, route, departure, arrival)) != null) {
                    soldTicket.add(ticket);
                }
            } else
            // inquiry ticket
            if (buypc <= sel && sel < inqpc) {

                int route = rand.nextInt(routenum) + 1;
                int departure = rand.nextInt(stationnum - 1) + 1;
                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
                                                                                    // greater than
                                                                                    // departure
                int leftTicket = tds.inquiry(route, departure, arrival);
            }
        }
    }
}