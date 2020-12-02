package ticketingsystem;

import java.util.HashMap;

public class TicketingDS implements TicketingSystem {

    private Train[] trains;
    private long ticketID;
    private HashMap<Long, Ticket> soldTickets;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        trains = new Train[routenum];
        ticketID = 0;
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train(coachnum, seatnum, stationnum);
        }
        soldTickets = new HashMap<Long, Ticket>();
    }

    @Override
    synchronized public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Train currTrian = trains[route - 1];
        int seat = currTrian.getAndLockSeat(departure - 1, arrival - 1);
        if(seat < 0) return null;
        Ticket ticket = new Ticket();
        ticket.tid = ticketID++;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = (seat / (trains[route - 1].seatNum / trains[route - 1].coachNum)) + 1;
        ticket.seat = (seat % (trains[route - 1].seatNum / trains[route - 1].coachNum)) + 1;
        if (ticket.seat == 0) {
            System.out.println(seat + "\t" + trains[route - 1].seatNum);
        }
        soldTickets.put(ticket.tid, ticket);
        return ticket;
    }

    @Override
    synchronized public int inquiry(int route, int departure, int arrival) {
        Train currTrian = trains[route - 1];
        return currTrian.getRemainSeats(departure - 1, arrival - 1);
    }

    @Override
    synchronized public boolean refundTicket(Ticket ticket) {
        if (!soldTickets.containsKey(ticket.tid) || !ticket.equals(soldTickets.get(ticket.tid))) {
            return false;
        }
        Train currTrian = trains[ticket.route - 1];
        int seat = (ticket.coach - 1) * (trains[ticket.route - 1].seatNum / trains[ticket.route - 1].coachNum)
                + (ticket.seat - 1);
        if (currTrian.unlockSeat(seat, ticket.departure - 1, ticket.arrival - 1)) {
            return soldTickets.remove(ticket.tid, ticket);
        }
        return false;
    }

}
