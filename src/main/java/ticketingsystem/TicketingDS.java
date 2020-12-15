package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private int threadNum;
    private Train[] trains;
    private int coachNum;
    private int seatNum;
    
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

        currTrian.addSoldTicket(ticket);
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        Train currTrian = trains[route - 1];
        return currTrian.getRemainSeats(departure - 1, arrival - 1);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        Train currTrian = trains[ticket.route - 1];
        if(!currTrian.containAndRemove(ticket)) {
            return false;
        }
        int seat = (ticket.coach - 1) * seatNum + (ticket.seat - 1);
        currTrian.insertRefundList(seat, ticket.departure - 1, ticket.arrival - 1);
        return currTrian.unlockSeat(seat, ticket.departure - 1, ticket.arrival - 1);
    }

    public void clear() {
        ticketBeginID.remove();
        ticketEndID.remove();
    }
}
