package ticketingsystem;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicStampedReference;

public class RemainSeatsTable {
    private int stationNum;
    private int seatNum;
    private AtomicStampedReference<int[][]> remainSeats;

    private ThreadLocal<int[][]> localTable;
    private ThreadLocal<int[][]> localTableSwither;

    Backoffer backoff = new Backoffer();

    public RemainSeatsTable(final int seatnum, final int stationnum) {
        this.stationNum = stationnum;
        this.seatNum = seatnum;

        this.localTable = ThreadLocal.withInitial(() -> {
            int[][] remainSeat = new int[stationNum][];
            for (int i = 0; i < stationNum; ++i) {
                // remainSeat[from][to]
                // NOTE: 'from' is real, but 'to' is (to+from)
                // if 'to' is 0, result is always 0
                // Example: remainSeat[1][2] = (1)->(3)
                remainSeat[i] = new int[stationNum - i];
                remainSeat[i][0] = 0;
                for (int j = 1; j < stationNum - i; ++j) {
                    remainSeat[i][j] = seatNum;
                }
            }
            return remainSeat;
        });

        this.localTableSwither = ThreadLocal.withInitial(() -> {
            int[][] remainSeat = new int[stationNum][];
            for (int i = 0; i < stationNum; ++i) {
                // remainSeat[from][to]
                // NOTE: 'from' is real, but 'to' is (to+from)
                // if 'to' is 0, result is always 0
                // Example: remainSeat[1][2] = (1)->(3)
                remainSeat[i] = new int[stationNum - i];
                remainSeat[i][0] = 0;
                for (int j = 1; j < stationNum - i; ++j) {
                    remainSeat[i][j] = seatNum;
                }
            }
            return remainSeat;
        });

        this.remainSeats = new AtomicStampedReference<>(localTable.get(), 0);
    }

    public final int getRemainSeats(final int departure, final int arrival) {
        int currTimestap = remainSeats.getStamp();
        int[][] currTable = remainSeats.getReference();
        int remain = currTable[departure][arrival - departure];
        int twiceTimestap = remainSeats.getStamp();
        while (currTimestap != twiceTimestap) {
            currTimestap = remainSeats.getStamp();
            currTable = remainSeats.getReference();
            remain = currTable[departure][arrival - departure];
            twiceTimestap = remainSeats.getStamp();
        }
        return remain;
    }

    public final void setRemainSeats(final int departure, final int arrival, final long origin, final int num) {
        while (true) {
            int[][] oldTable = remainSeats.getReference();
            int[][] newTable = localTable.get();
            if (Arrays.equals(oldTable, newTable)) {
                // using my local table as global table,
                // so we need create a new one to modify
                newTable = localTableSwither.get();
            }
            int stamp = remainSeats.getStamp();
            // decrease/increase the table

            // if origin is 0, mark we need do all task
            boolean currIsNotClean = (origin != 0);
            // 0 1 2 3 4 5
            // Example: 2->4
            // 0-3 -> 3-5
            // departure station
            for (int i = 0; i < arrival; ++i) {
                // copy
                for (int j = i + 1; j < stationNum; ++j) {
                    newTable[i][j - i] = oldTable[i][j - i];
                }
                // arrival station
                for (int j = Math.max(departure, i) + 1; j < stationNum; ++j) {
                    if (currIsNotClean && isOverlapping(i, j, origin)) {
                        newTable[i][j - i] = oldTable[i][j - i];
                    } else {
                        newTable[i][j - i] = oldTable[i][j - i] + num;
                    }
                }
            }
            // copy
            for (int i = arrival; i < stationNum; ++i) {
                for (int j = i + 1; j < stationNum; ++j) {
                    newTable[i][j - i] = oldTable[i][j - i];
                }
            }

            if (remainSeats.compareAndSet(oldTable, newTable, stamp, stamp + 1)) {
                return;
            }
            backoff.backoff();
        }
    }

    public void decrementRemainSeats(final int departure, final int arrival, final long origin) {
        setRemainSeats(departure, arrival, origin, -1);
    }

    public void incrementRemainSeats(final int departure, final int arrival, final long origin) {
        setRemainSeats(departure, arrival, origin, 1);
    }

    private final boolean isOverlapping(final int departure, final int arrival, final long origin) {
        // departure and arrival not overlapping the origin data
        int mask = ((0x01 << (arrival - departure)) - 1) << departure;
        return ((mask & origin) > 0);
    }

    public void clear() {
        localTable.remove();
        localTableSwither.remove();
    }
}
