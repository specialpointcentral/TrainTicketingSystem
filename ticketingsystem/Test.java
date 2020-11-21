package ticketingsystem;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== Begin Unit Test =====");
        System.out.flush();
        UnitTest utest = new UnitTest();
        try {
            if(utest.beginTest()) {
                System.out.println("===== Unit Test Pass! =====");
                System.out.flush();
            } else {
                System.err.println("===== Unit Test Failure! =====");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("===== Unit Test End =====");
            System.out.flush();
        }

        System.out.println("===== Begin Random Test =====");
        System.out.flush();
        RandomTest rtest = new RandomTest();
        try {
            rtest.beginTest();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("===== Random Test End =====");
            System.out.flush();
        }
    }
}
