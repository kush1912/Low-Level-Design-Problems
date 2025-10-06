package lld.DesignProblems.multithreading;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketBookingReenterantLock {
    private final Lock[] ticketLocks; // Array of locks for each ticket
    private int availableTickets; // Initialize with the total number of tickets available

    public TicketBookingReenterantLock(int numTickets) {
        this.availableTickets = numTickets;
        ticketLocks = new ReentrantLock[numTickets];
        for (int i = 0; i < numTickets; i++) {
            ticketLocks[i] = new ReentrantLock();
        }
    }
    public boolean bookTicket(int ticketNumber) {
        if (ticketNumber < 0 || ticketNumber >= ticketLocks.length) {
            return false; // Invalid ticket number
        }

        Lock ticketLock = ticketLocks[ticketNumber];
        ticketLock.lock(); // Acquire the lock for the specified ticket
        try {
            if (availableTickets > 0) {
                // Simulate some processing time
                try {
                    Thread.sleep(100); // Sleep for 100 milliseconds to simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // If we reach here, there are enough tickets available, proceed with booking
                availableTickets--;
                return true;
            } else {
                return false; // No available tickets
            }
        } finally {
            ticketLock.unlock(); // Release the lock for the specified ticket
        }
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public static void main(String[] args) {
        int numTickets = 10;
        TicketBookingReenterantLock bookingSystem = new TicketBookingReenterantLock(numTickets);

        // Simulate multiple booking attempts
        Runnable bookingTask = () -> {
            for (int i = 0; i < 5; i++) {
                int ticketToBook = (int) (Math.random() * numTickets); // Randomly choose a ticket
                if (bookingSystem.bookTicket(ticketToBook)) {
                    System.out.println(Thread.currentThread().getName() + ": Ticket " + ticketToBook + " booked successfully. Available tickets: " + bookingSystem.getAvailableTickets());
                } else {
                    System.out.println(Thread.currentThread().getName() + ": Failed to book ticket " + ticketToBook + ". No available tickets.");
                }
            }
        };

        // Create multiple threads to simulate concurrent booking attempts
        for (int i = 0; i < 3; i++) {
            new Thread(bookingTask).start();
        }
    }
}
