package ticketingsystem;

public class Train {
    public boolean[][] seats;
    public final int seatNum;
    public final int coachNum;
    public final int stationNum;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        seatNum = coachnum * seatnum;
        coachNum = coachnum;
        stationNum = stationnum;
        seats = new boolean[seatNum][stationnum];
        for (int i = 0; i < seatNum; ++i) {
            for (int j = 0; j < stationnum; ++j) {
                seats[i][j] = false;
            }
        }
    }

    public int getAndLockSeat(final int departure, final int arrival) {
        int seat = -1;
        for(int i = 0; i < seatNum; ++i) {
            if(!isSeatOccupied(seats[i], departure, arrival)) {
                seat = i;
                setOccupied(seats[i], departure, arrival);
                break;
            }
        }
        return seat;
    }

    public int getRemainSeats(final int departure, final int arrival) {
        int seatsNum = 0;
        for(int i = 0; i < seatNum; ++i) {
            if(!isSeatOccupied(seats[i], departure, arrival)) {
                ++seatsNum;
            }
        }
        return seatsNum;
    }

    public boolean unlockSeat(final int seat ,final int departure, final int arrival) {
        cleanOccupied(seats[seat], departure, arrival);
        return true;
    }

    private boolean isSeatOccupied(final boolean[]block, final int departure, final int arrival){
        boolean findOne = false;
        for(int i = departure; i < arrival; ++i) {
            if(block[i]) {
                // seats are occpied
                findOne = true;
                break;
            }
        }
        return findOne;
    }

    private void setOccupied(final boolean[]block, final int departure, final int arrival){
        for(int i = departure; i < arrival; ++i) {
            block[i] = true;
        }
    }

    private void cleanOccupied(final boolean[]block, final int departure, final int arrival){
        for(int i = departure; i < arrival; ++i) {
            block[i] = false;
        }
    }
}
