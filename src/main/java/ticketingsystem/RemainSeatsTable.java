package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class RemainSeatsTable {
    private int stationNum;
    private AtomicInteger[][][] remainSeats;
    private AtomicStampedReference<Integer> tag;

    public RemainSeatsTable(final int seatnum, final int stationnum) {
        this.stationNum = stationnum;
        this.tag = new AtomicStampedReference<>(0,0);
        this.remainSeats = new AtomicInteger[2][][];
        for (int tags = 0; tags < 2; ++tags) {
            this.remainSeats[tags] = new AtomicInteger[stationnum][];
            for (int i = 0; i < stationnum; ++i) {
                // remainSeats[from][to]
                // NOTE: 'from' is real, but 'to' is (to+from)
                // if 'to' is 0, result is always 0
                // Example: remainSeats[1][2] = (1)->(3)
                this.remainSeats[tags][i] = new AtomicInteger[stationnum - i];
                this.remainSeats[tags][i][0] = new AtomicInteger(0);
                for (int j = 1; j < stationnum - i; ++j) {
                    remainSeats[tags][i][j] = new AtomicInteger(seatnum);
                }
            }
        }
    }

    public int getRemainSeats(final int departure, final int arrival) {
        return remainSeats[tag.get() % 2][departure][arrival - departure].get();
    }

    public void decrementRemainSeats(final int departure, final int arrival) {
        int currTag = (tag.get() + 1) % 2;
        // 0 1 2 3 4 5
        // Example: 2->4
        // 0-3 -> 3-5
        // departure station
        for (int i = 0; i < arrival; ++i) {
            // arrival station
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[currTag][i][j - i].getAndDecrement();
            }
        }
        // finish modify
        tag.getAndIncrement();
    }

    public void incrementRemainSeats(final int departure, final int arrival) {
        int currTag = (tag.get() + 1) % 2;
        // departure station
        for (int i = 0; i < arrival; ++i) {
            // arrival station
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[currTag][i][j - i].getAndIncrement();
            }
        }
        // finish modify
        tag.getAndIncrement();
    }
}
