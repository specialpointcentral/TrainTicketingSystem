package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private int threadNum;
    private Train[] trains;
    private int coachNum;
    private int seatNum;
    private ConcurrentHashMap<Long, Ticket> soldTickets;
    private AtomicLong buyTicketQueryID;
    private long hashMask;

    private ThreadLocal<Long> ticketBeginID = ThreadLocal.withInitial(()->0L);
    private ThreadLocal<Long> ticketEndID = ThreadLocal.withInitial(()->-1L);

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.threadNum = threadnum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        trains = new Train[routenum];
        buyTicketQueryID = new AtomicLong(0);
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train(coachnum, seatnum, stationnum);
        }
        int initialCapacity = 128;
        float loadFactor = 0.5f;
        int concurrencyLevel = (threadnum / 10 + 1) * 2;
        soldTickets = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);

        int coachBitNum = 32 - Integer.numberOfLeadingZeros(Math.max(this.coachNum - 1, 1));
        int threadNumBitNum = 32 - Integer.numberOfLeadingZeros(Math.max(this.threadNum - 1, 1));
        // hash mask for query
        if (this.threadNum > this.coachNum) {
            // all coach need used as hash
            this.hashMask = (0x1 << coachBitNum) - 1;
        } else {
            // will send to some coach
            this.hashMask = ((0x1 << threadNumBitNum) - 1) << (coachBitNum - threadNumBitNum);
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Ticket ticket = new Ticket();
        long threadID = Thread.currentThread().getId();
        // deliver to every train and coach
        int queryCoachID = (int)(threadID & hashMask);
        Train currTrian = trains[route - 1];
        int beginSeats = currTrian.getFindRefund(departure - 1, arrival - 1);
        if(beginSeats == -1) beginSeats = queryCoachID * seatNum;
        int seat = currTrian.getAndLockSeat(departure - 1, arrival - 1, beginSeats);
        if (seat < 0)
            return null;
        // find a tid
        long queryID = ticketBeginID.get();
        if(queryID > ticketEndID.get()) {
            queryID = buyTicketQueryID.getAndAdd(512);
            ticketEndID.set(queryID + 511);
        }
        ticketBeginID.set(queryID + 1);
        // build a ticket
        ticket.tid = queryID;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = (seat / seatNum) + 1;
        ticket.seat = (seat % seatNum) + 1;

        soldTickets.put(ticket.tid, ticket);
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        Train currTrian = trains[route - 1];
        return currTrian.getRemainSeats(departure - 1, arrival - 1);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (!soldTickets.containsKey(ticket.tid) || 
            !ticketEquals(ticket, soldTickets.get(ticket.tid))) {
            return false;
        }
        soldTickets.remove(ticket.tid);
        Train currTrian = trains[ticket.route - 1];
        int seat = (ticket.coach - 1) * seatNum + (ticket.seat - 1);
        currTrian.insertRefundList(seat, ticket.departure - 1, ticket.arrival - 1);
        return currTrian.unlockSeat(seat, ticket.departure - 1, ticket.arrival - 1);
    }

    private final boolean ticketEquals(Ticket x, Ticket y) {
        if(x == y) return true;
        if(x == null || y == null) return false;
        
        return( 
            (x.tid == y.tid)                    &&
            (x.passenger.equals(y.passenger))   &&
            (x.route == y.route)                &&
            (x.coach == y.coach)                &&
            (x.seat == y.seat)                  &&
            (x.departure == y.departure)        &&
            (x.arrival == y.arrival)
        );
    }

    public void clear() {
        ticketBeginID.remove();
        ticketEndID.remove();
    }
}
