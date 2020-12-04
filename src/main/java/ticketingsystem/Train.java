package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Train {
    private AtomicLong[] seats;
    private final int seatNum;
    private final int coachNum;
    private final int stationNum;
    private final int allSeatNum;

    // map for remain seats
    private AtomicInteger[][] remainSeats;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        this.seatNum = seatnum;
        this.allSeatNum = coachnum * seatnum;
        this.coachNum = coachnum;
        this.stationNum = stationnum;
        this.seats = new AtomicLong[this.allSeatNum];
        for (int i = 0; i < this.allSeatNum; ++i) {
            this.seats[i] = new AtomicLong(0);
        }
        this.remainSeats = new AtomicInteger[stationnum][];
        for (int i = 0; i < stationnum; ++i) {
            // remainSeats[from][to]
            // NOTE: 'from' is real, but 'to' is (to+from)
            // if 'to' is 0, result is always 0
            // Example: remainSeats[1][2] = (1)->(3)
            this.remainSeats[i] = new AtomicInteger[stationnum - i];
            this.remainSeats[i][0] = new AtomicInteger(0);
            for (int j = 1; j < stationnum - i; ++j) {
                remainSeats[i][j] = new AtomicInteger(this.allSeatNum);
            }
        }
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
                        occupiedRemainSeats(departure, arrival);
                        return pos;
                    }
                    tmp = seats[pos].get();
                }
            }
        }
        return -1;
    }

    public int getRemainSeats(final int departure, final int arrival) {
        return remainSeats[departure][arrival - departure].get();
    }

    public boolean unlockSeat(final int seat, final int departure, final int arrival) {
        while (true) {
            long tmp = seats[seat].get();
            if (seats[seat].compareAndSet(tmp, cleanOccupied(seats[seat].get(), departure, arrival))) {
                releasedRemainSeats(departure, arrival);
                return true;
            }
        }
    }

    private boolean isSeatOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        // 00000|0000|000000 block
        // 00000|1111|000000 occupied
        return (occupied & block) != 0;
    }

    private long setOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block | occupied;
    }

    private long cleanOccupied(final long block, final int departure, final int arrival) {
        long occupied = ((0x01 << (arrival - departure)) - 1) << departure;
        return block & ~occupied;
    }

    private boolean haveRemainSeats(final int departure, final int arrival) {
        return remainSeats[departure][arrival - departure].get() > 0;
    }

    private void occupiedRemainSeats(final int departure, final int arrival) {
        // 0 1 2 3 4 5
        // Example: 2->4
        // 0-3 -> 3-5
        // departure station
        for (int i = 0; i < arrival; ++i) {
            // arrival station
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[i][j - i].getAndDecrement();
            }
        }
    }

    private void releasedRemainSeats(final int departure, final int arrival) {
        // departure station
        for (int i = 0; i < arrival; ++i) {
            // arrival station
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[i][j - i].getAndIncrement();
            }
        }
    }
}
