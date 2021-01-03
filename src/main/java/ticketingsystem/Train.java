package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class RefundTicket {
    int seat;
    int departure; 
    int arrival;
    public RefundTicket(final int seat, final int departure, final int arrival) {
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
    }
}

public class Train {
    private AtomicInteger[] seats;
    private final int seatNum;
    private final int coachNum;
    private final int stationNum;
    private final int allSeatNum;
    private ConcurrentHashMap<Long, Ticket> soldTickets;

    private static final int BUFSIZE = 20;
    private ThreadLocal<RefundTicket[]> refundList = ThreadLocal.withInitial(()-> new RefundTicket[BUFSIZE]);
    private ThreadLocal<Integer> pointer = ThreadLocal.withInitial(()-> 0);
    // map for remain seats
    RemainSeatsTable remainSeats;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        this.seatNum = seatnum;
        this.allSeatNum = coachnum * seatnum;
        this.coachNum = coachnum;
        this.stationNum = stationnum;
        this.seats = new AtomicInteger[this.allSeatNum];
        for (int i = 0; i < this.allSeatNum; ++i) {
            this.seats[i] = new AtomicInteger(0);
        }
        remainSeats = new RemainSeatsTable(this.allSeatNum, this.stationNum);
        int initialCapacity = 128;
        float loadFactor = 0.5f;
        int concurrencyLevel = 2;
        soldTickets = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public final int getAndLockSeat(final int departure, final int arrival) {
        return getAndLockSeat(departure, arrival, 0);
    }

    public int getAndLockSeat(final int departure, final int arrival, final int beginSeats) {
        // check if has seats
        if (haveRemainSeats(departure, arrival)) {
            int beginSeat = beginSeats % allSeatNum;
            // find the seat
            for (int i = 0; i < allSeatNum; ++i) {
                int pos = (beginSeat + i) % allSeatNum;
                int tmp = seats[pos].get();
                // if seat is not occupied
                while (!isSeatOccupied(tmp, departure, arrival)) {
                    // CAS!
                    if (seats[pos].compareAndSet(tmp, setOccupied(tmp, departure, arrival))) {
                        remainSeats.decrementRemainSeats(departure, arrival, tmp);
                        return pos;
                    }
                    tmp = seats[pos].get();
                }
            }
        }
        return -1;
    }

    public final int getRemainSeats(final int departure, final int arrival) {
        return remainSeats.getRemainSeats(departure, arrival);
    }

    public boolean unlockSeat(final int seat, final int departure, final int arrival) {
        while (true) {
            int tmp = seats[seat].get();
            int cleanTmp = cleanOccupied(tmp, departure, arrival);
            if (seats[seat].compareAndSet(tmp, cleanTmp)) {
                remainSeats.incrementRemainSeats(departure, arrival, cleanTmp);
                return true;
            }
        }
    }

    private final boolean isSeatOccupied(final int block, final int departure, final int arrival) {
        int occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        // 00000|0000|000000 block
        // 00000|1111|000000 occupied
        return (occupied & block) != 0;
    }

    private final int setOccupied(final int block, final int departure, final int arrival) {
        int occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block | occupied;
    }

    private final int cleanOccupied(final int block, final int departure, final int arrival) {
        int occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block & ~occupied;
    }

    private final boolean haveRemainSeats(final int departure, final int arrival) {
        return remainSeats.getRemainSeats(departure, arrival) > 0;
    }

    public final void insertRefundList(final int seat, final int departure, final int arrival) {
        int p = pointer.get();
        refundList.get()[p] = new RefundTicket(seat, departure, arrival);
        pointer.set((p + 1) % BUFSIZE);
    }

    public final int getFindRefund(final int departure, final int arrival) {
        int usefulSeat = -1;
        for(int i = 0; i < BUFSIZE; ++i) {
            RefundTicket tick = refundList.get()[i];
            if(tick != null && tick.departure <= departure && tick.arrival >= arrival) {
                usefulSeat = tick.seat;
                refundList.get()[i] = null;
                break;
            }
        }
        return usefulSeat;
    }

    public void clear() {
        refundList.remove();
        pointer.remove();
        remainSeats.clear();
    }

    public final boolean containAndRemove(Ticket ticket) {
        Ticket containTicket = soldTickets.get(ticket.tid);
        if (containTicket == null || !ticketEquals(ticket, containTicket)) {
            return false;
        }
        return soldTickets.remove(ticket.tid, containTicket);
    }

    public final void addSoldTicket(Ticket ticket) {
        soldTickets.put(ticket.tid, ticket);
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
}
