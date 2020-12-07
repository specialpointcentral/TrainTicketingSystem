package ticketingsystem;

import java.util.concurrent.atomic.AtomicStampedReference;

public class RemainSeatsTable {
    private int stationNum;
    private int[][][] remainSeats;
    private AtomicStampedReference<Integer> tag;

    public RemainSeatsTable(final int seatnum, final int stationnum) {
        this.stationNum = stationnum;
        this.tag = new AtomicStampedReference<>(0, 0);
        this.remainSeats = new int[2][][];
        for (int tags = 0; tags < 2; ++tags) {
            this.remainSeats[tags] = new int[stationnum][];
            for (int i = 0; i < stationnum; ++i) {
                // remainSeats[from][to]
                // NOTE: 'from' is real, but 'to' is (to+from)
                // if 'to' is 0, result is always 0
                // Example: remainSeats[1][2] = (1)->(3)
                this.remainSeats[tags][i] = new int[stationnum - i];
                this.remainSeats[tags][i][0] = 0;
                for (int j = 1; j < stationnum - i; ++j) {
                    remainSeats[tags][i][j] = seatnum;
                }
            }
        }
    }

    public int getRemainSeats(final int departure, final int arrival) {
        int currTimestap = tag.getStamp();
        int currTag = tag.getReference();
        int remain = remainSeats[currTag][departure][arrival - departure];
        while (!tag.compareAndSet(currTag, currTag, currTimestap, currTimestap)) {
            currTimestap = tag.getStamp();
            currTag = tag.getReference();
            remain = remainSeats[currTag][departure][arrival - departure];
        }
        return remain;
    }

    public synchronized void decrementRemainSeats(final int departure, final int arrival) {
        int currTag = 1 - tag.getReference();
        // 0 1 2 3 4 5
        // Example: 2->4
        // 0-3 -> 3-5
        // departure station
        for (int i = 0; i < arrival; ++i) {
            // arrival station
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[1 - currTag][i][j - i]--;
                remainSeats[currTag][i][j - i] = remainSeats[1 - currTag][i][j - i];
            }
        }
        // finish modify
        tag.set(currTag, tag.getStamp() + 1);
    }

    public synchronized void incrementRemainSeats(final int departure, final int arrival) {
        int currTag = 1 - tag.getReference();
        // departure station
        for (int i = 0; i < arrival; ++i) {
            for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                remainSeats[1 - currTag][i][j - i]++;
                remainSeats[currTag][i][j - i] = remainSeats[1 - currTag][i][j - i];
            }
        }
        // finish modify
        tag.set(currTag, tag.getStamp() + 1);
    }
}
