package ticketingsystem;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        RandomTest rtest = new RandomTest();
        try {
            rtest.beginTest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
