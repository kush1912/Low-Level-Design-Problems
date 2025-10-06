package lld.DesignProblems.multithreading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TicketBookingSystem {
    private final BlockingQueue<Integer> availableTicketsQueue; // Queue representing available tickets

    public TicketBookingSystem(int numTickets) {
        availableTicketsQueue = new LinkedBlockingQueue<>(numTickets);
        for (int i = 0; i < numTickets; i++) {
            availableTicketsQueue.add(i); // Add tickets to the queue
        }
    }

    public boolean bookTicket() {
        Integer ticketNumber = availableTicketsQueue.poll(); // Attempt to remove a ticket from the queue
        if (ticketNumber != null) {
            // Simulate some processing time
            try {
                Thread.sleep(100); // Sleep for 100 milliseconds to simulate processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // If we reach here, a ticket was successfully booked
            return true;
        } else {
            // No available tickets in the queue
            return false;
        }
    }

    public int getAvailableTickets() {
        return availableTicketsQueue.size();
    }

    public static void main(String[] args) {
        int numTickets = 10;
        TicketBookingSystem bookingSystem = new TicketBookingSystem(numTickets);

        // Simulate multiple booking attempts
        Runnable bookingTask = () -> {
            for (int i = 0; i < 5; i++) {
                if (bookingSystem.bookTicket()) {
                    System.out.println(Thread.currentThread().getName() + ": Ticket booked successfully. Available tickets: " + bookingSystem.getAvailableTickets());
                } else {
                    System.out.println(Thread.currentThread().getName() + ": Failed to book ticket. No available tickets.");
                }
            }
        };

        // Create multiple threads to simulate concurrent booking attempts
        for (int i = 0; i < 3; i++) {
            new Thread(bookingTask).start();
        }
    }
}
