package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class Train {
    private AtomicLong[] seats;
    private final int seatNum;
    private final int coachNum;
    private final int stationNum;
    private final int allSeatNum;

    // map for remain seats
    RemainSeatsTable remainSeats;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        this.seatNum = seatnum;
        this.allSeatNum = coachnum * seatnum;
        this.coachNum = coachnum;
        this.stationNum = stationnum;
        this.seats = new AtomicLong[this.allSeatNum];
        for (int i = 0; i < this.allSeatNum; ++i) {
            this.seats[i] = new AtomicLong(0);
        }
        remainSeats = new RemainSeatsTable(this.allSeatNum, this.stationNum);
    }

    public int getAndLockSeat(final int departure, final int arrival) {
        return getAndLockSeat(departure, arrival, 0);
    }

    public int getAndLockSeat(final int departure, final int arrival, final int beginCoach) {
        // check if has seats
        while (haveRemainSeats(departure, arrival)) {
            int beginSeat = (beginCoach % coachNum) * seatNum;
            // find the seat
            for (int i = 0; i < allSeatNum; ++i) {
                int pos = (beginSeat + i) % allSeatNum;
                long tmp = seats[pos].get();
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

    public int getRemainSeats(final int departure, final int arrival) {
        return remainSeats.getRemainSeats(departure, arrival);
    }

    public boolean unlockSeat(final int seat, final int departure, final int arrival) {
        while (true) {
            long tmp = seats[seat].get();
            long cleanTmp = cleanOccupied(tmp, departure, arrival);
            if (seats[seat].compareAndSet(tmp, cleanTmp)) {
                remainSeats.incrementRemainSeats(departure, arrival, cleanTmp);
                return true;
            }
        }
    }

    private final boolean isSeatOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        // 00000|0000|000000 block
        // 00000|1111|000000 occupied
        return (occupied & block) != 0;
    }

    private final long setOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block | occupied;
    }

    private final long cleanOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block & ~occupied;
    }

    private final boolean haveRemainSeats(final int departure, final int arrival) {
        return remainSeats.getRemainSeats(departure, arrival) > 0;
    }
}
