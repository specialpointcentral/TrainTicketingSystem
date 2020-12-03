package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

    private Train[] trains;
    private AtomicLong ticketsID;
    private int coachnum;
    private int seatnum;
    private ConcurrentHashMap<Long, Ticket> soldTickets;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        trains = new Train[routenum];
        ticketsID = new AtomicLong(0);
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train(coachnum, seatnum, stationnum);
        }
        soldTickets = new ConcurrentHashMap<>();
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Train currTrian = trains[route - 1];
        int seat = currTrian.getAndLockSeat(departure - 1, arrival - 1);
        if(seat < 0) return null;
        Ticket ticket = new Ticket();
        ticket.tid = ticketsID.incrementAndGet();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = (seat / (seatnum / coachnum)) + 1;
        ticket.seat = (seat % (seatnum / coachnum)) + 1;
        if (ticket.seat == 0) {
            System.err.println("Error!");
        }
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
        int seat = (ticket.coach - 1) * (seatnum / coachnum)
                + (ticket.seat - 1);
        return currTrian.unlockSeat(seat, ticket.departure - 1, ticket.arrival - 1);
    }

}
