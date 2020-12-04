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

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.threadNum = threadnum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        trains = new Train[routenum];
        buyTicketQueryID = new AtomicLong(0);
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train(coachnum, seatnum, stationnum);
        }
        soldTickets = new ConcurrentHashMap<>();

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
        long queryID = buyTicketQueryID.incrementAndGet();
        int queryCoachID = (int) ((queryID ^ threadID) & hashMask);
        // System.err.println(queryCoachID);
        Train currTrian = trains[route - 1];
        int seat = currTrian.getAndLockSeat(departure - 1, arrival - 1, queryCoachID);
        if (seat < 0)
            return null;
        ticket.tid = queryID;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = (seat / (seatNum / coachNum)) + 1;
        ticket.seat = (seat % (seatNum / coachNum)) + 1;

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
        if (!soldTickets.containsKey(ticket.tid) || !ticket.equals(soldTickets.get(ticket.tid))) {
            return false;
        }
        soldTickets.remove(ticket.tid, ticket);
        Train currTrian = trains[ticket.route - 1];
        int seat = (ticket.coach - 1) * (seatNum / coachNum) + (ticket.seat - 1);
        return currTrian.unlockSeat(seat, ticket.departure - 1, ticket.arrival - 1);
    }

}
