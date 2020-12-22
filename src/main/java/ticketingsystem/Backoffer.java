package ticketingsystem;

import java.security.SecureRandom;
import java.util.Random;

public class Backoffer {
    ThreadLocal<Integer> retryTime = ThreadLocal.withInitial(()->1);
    private Random rand = new SecureRandom();

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
        if(bound < 0xfffff)
            retryTime.set(bound << 1);
    }
}
