package ticketingsystem;

import org.openjdk.jmh.annotations.*;

import java.text.DecimalFormat;
import java.util.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 5)
@Fork(2)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
public class PerformanceBenchmark {
    @Param({ "1", "2", "4", "8", "16", "32", "64", "128" })
    static int nThreads;

    private ExecutorService pool;

    final static int routenum = 20; // route is designed from 1 to 3
    final static int coachnum = 10; // coach is arranged from 1 to 5
    final static int seatnum = 100; // seat is allocated from 1 to 20
    final static int stationnum = 16; // station is designed from 1 to 5

    final static int testnum = 64000;
    final static int retpc = 10; // return ticket operation is 10% percent
    final static int buypc = 30; // buy ticket operation is 30% percent
    final static int inqpc = 100; // inquiry ticket operation is 60% percent

    TicketingDS tds;
    ArrayList<Callable<Object>> list;

    static ThreadLocalRandom rand = ThreadLocalRandom.current();

    // perform record
    class PerformRecord {
        int buySuccessTimes;
        int buyFailTimes;
        int refundTimes;
        int inqueryTimes;
    }

    ThreadLocal<PerformRecord> perform = ThreadLocal.withInitial(() -> new PerformRecord());
    List<PerformRecord> singlePerform = Collections.synchronizedList(new ArrayList<PerformRecord>());
    ArrayList<PerformRecord> performList = new ArrayList<>();

    Logger logger = Logger.getLogger("PerformLog");

    static String passengerName() {
        long uid = rand.nextLong();
        return "passenger" + uid;
    }

    @Setup(Level.Trial)
    public void initPerform() {
        performList.clear();
    }

    @Setup(Level.Iteration)
    public void init() {
        this.pool = Executors.newFixedThreadPool(nThreads);
        tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, nThreads);
        list = new ArrayList<>();
        singlePerform.clear();
        for (int i = 0; i < nThreads; i++) {
            list.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    perform.get().buySuccessTimes = 0;
                    perform.get().buyFailTimes = 0;
                    perform.get().refundTimes = 0;
                    perform.get().inqueryTimes = 0;

                    singleTrace();
                    
                    singlePerform.add(perform.get());
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

    @TearDown(Level.Iteration)
    public void finish() {
        performCalc();
        pool.shutdown();
    }

    public void performCalc() {
        PerformRecord p = new PerformRecord();
        for (int i = 0; i < singlePerform.size(); ++i) {
            p.buySuccessTimes += singlePerform.get(i).buySuccessTimes;
            p.buyFailTimes += singlePerform.get(i).buyFailTimes;
            p.refundTimes += singlePerform.get(i).refundTimes;
            p.inqueryTimes += singlePerform.get(i).inqueryTimes;
        }
        performList.add(p);
    }

    @TearDown(Level.Trial)
    public void performRes() {
        for (int i = 0; i < performList.size(); ++i) {
            PerformRecord p = performList.get(i);
            int all = p.inqueryTimes + p.refundTimes + p.buySuccessTimes + p.buyFailTimes;
            logger.info(String.format(
                    "[Turn: %02d] Inquery: %08d(%s%%), Refund: %08d(%s%%), BuySuccess: %08d(%s%%), BuyFailed: %08d(%s%%) %n",
                    i + 1, p.inqueryTimes, new DecimalFormat("0.00").format(p.inqueryTimes * 100.0 / all),
                    p.refundTimes, new DecimalFormat("0.00").format(p.refundTimes * 100.0 / all), p.buySuccessTimes,
                    new DecimalFormat("0.00").format(p.buySuccessTimes * 100.0 / all), p.buyFailTimes,
                    new DecimalFormat("0.00").format(p.buyFailTimes * 100.0 / all)));
        }
    }

    private final void singleTrace() {
        Ticket ticket = null;
        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

        for (int i = 0; i < testnum / nThreads; i++) {
            int sel = rand.nextInt(inqpc);
            // return ticket
            if (0 <= sel && sel < retpc && !soldTicket.isEmpty()) {
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
                perform.get().refundTimes++;
            } else
            // buy ticket
            if (retpc <= sel && sel < buypc) {
                String passenger = passengerName();
                int route = rand.nextInt(routenum) + 1;
                int departure = rand.nextInt(stationnum - 1) + 1;
                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
                                                                                    // greater than
                                                                                    // departure
                if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                    soldTicket.add(ticket);
                    perform.get().buySuccessTimes++;
                } else {
                    perform.get().buyFailTimes++;
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

                perform.get().inqueryTimes++;
            }
        }
    }
}