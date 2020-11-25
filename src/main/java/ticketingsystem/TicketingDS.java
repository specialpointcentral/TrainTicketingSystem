package ticketingsystem;

public class TicketingDS implements TicketingSystem {

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {

    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        // TODO Auto-generated method stub
        return false;
    }

}
