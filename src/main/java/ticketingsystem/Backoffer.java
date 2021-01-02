package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;

public class Backoffer {
    ThreadLocal<Integer> retryTime = ThreadLocal.withInitial(()->1);
    private ThreadLocalRandom rand = ThreadLocalRandom.current();

    public void setBackoffTime(int time) {
        if(time > 1)
            retryTime.set(time);
        else
            retryTime.set(1);
    }

    public void backoff() {
        int bound = retryTime.get();
        int j = rand.nextInt(bound);
        for(int i = 0; i < j; ++i);
        if(bound < 0xfff)
            retryTime.set(bound << 1);
    }
}
