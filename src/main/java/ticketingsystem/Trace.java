package ticketingsystem;

import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;

class ThreadId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }
}

public class Trace {
	final static int threadnum = 1;
	final static int routenum = 1; // route is designed from 1 to 3
	final static int coachnum = 3; // coach is arranged from 1 to 5
	final static int seatnum = 3; // seat is allocated from 1 to 20
	final static int stationnum = 3; // station is designed from 1 to 5

	final static int testnum = 3000;
	final static int retpc = 30; // return ticket operation is 10% percent
	final static int buypc = 60; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	public static void main(String[] args) throws InterruptedException {
        
		 
		Thread[] threads = new Thread[threadnum];
		
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		
		final long startTime = System.nanoTime();
		//long preTime = startTime;
	    
	for (int i = 0; i< threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
            		Random rand = new Random();
                	Ticket ticket = new Ticket();
            		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
            		
            		//System.out.println(ThreadId.get());
            		for (int i = 0; i < testnum; i++) {
            			int sel = rand.nextInt(inqpc);
            			if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
            				int select = rand.nextInt(soldTicket.size());
						long preTime = System.nanoTime() - startTime;
           				if ((ticket = soldTicket.remove(select)) != null) {
								preTime = System.nanoTime() - startTime;
            					if (tds.refundTicket(ticket)) {
    								long postTime = System.nanoTime() - startTime;
            						System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
            						//System.out.println(preTime + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
            						//System.out.flush();
            					} else {
    								long postTime = System.nanoTime() - startTime;
            						System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "ErrOfRefund");
            						//System.out.flush();
            					}
            				} else {
								long postTime = System.nanoTime() - startTime;
            					System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "ErrOfRefund");
        						//System.out.flush();
            				}
            			} else if (retpc <= sel && sel < buypc) { // buy ticket
            				String passenger = passengerName();
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							long preTime = System.nanoTime() - startTime;
            				if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								long postTime = System.nanoTime() - startTime;
            					System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
            					//System.out.println(preTime + " " + ThreadId.get() + " " + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
            					soldTicket.add(ticket);
        						//System.out.flush();
            				} else {
								long postTime = System.nanoTime() - startTime;
            					System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
            					//System.out.println(preTime + " " + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
        						//System.out.flush();
            				}
            			} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
            				
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							long preTime = System.nanoTime() - startTime;
            				int leftTicket = tds.inquiry(route, departure, arrival);
							long postTime = System.nanoTime() - startTime;
            				System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
            				//System.out.println(preTime + " " + ThreadId.get() + " " + "RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
    						//System.out.flush();  
    						         			
            			}
            		}

                }
            });
              threads[i].start();
 	    }
	
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i].join();
	    }		
	}
}
