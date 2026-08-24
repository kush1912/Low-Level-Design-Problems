package lld.DesignProblems.bookmyshow;

import lld.DesignProblems.bookmyshow.enums.SeatStatus;
import lld.DesignProblems.bookmyshow.enums.Tier;
import lld.DesignProblems.bookmyshow.model.Booking;
import lld.DesignProblems.bookmyshow.model.City;
import lld.DesignProblems.bookmyshow.model.Customer;
import lld.DesignProblems.bookmyshow.model.Movie;
import lld.DesignProblems.bookmyshow.model.Screen;
import lld.DesignProblems.bookmyshow.model.Seat;
import lld.DesignProblems.bookmyshow.model.Show;
import lld.DesignProblems.bookmyshow.model.Theatre;
import lld.DesignProblems.bookmyshow.services.BookingOrchestrator;
import lld.DesignProblems.bookmyshow.services.BookingService;
import lld.DesignProblems.bookmyshow.services.InMemoryPaymentService;
import lld.DesignProblems.bookmyshow.services.PaymentService;
import lld.DesignProblems.bookmyshow.services.SeatLockProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

public class BookmyshowDriverClass {
    public static void main(String[] args) {
        Movie movie = new Movie(
                "Interstellar",
                "English",
                8.7,
                "Science-fiction adventure"
        );

        Screen screen = new Screen("Audi 1", 2, 3);
        Seat a1 = new Seat("A", 1, Tier.SILVER);
        Seat a2 = new Seat("A", 2, Tier.SILVER);
        Seat a3 = new Seat("A", 3, Tier.SILVER);
        Seat b1 = new Seat("B", 1, Tier.GOLDEN);
        Seat b2 = new Seat("B", 2, Tier.GOLDEN);
        Seat b3 = new Seat("B", 3, Tier.GOLDEN);

        for (Seat seat : Set.of(a1, a2, a3, b1, b2, b3)) {
            screen.addSeat(seat);
        }

        long startTime =
                System.currentTimeMillis() + 60 * 60 * 1000L;
        Show show = new Show(
                movie,
                startTime,
                startTime + 3 * 60 * 60 * 1000L,
                screen
        );
        screen.addShow(show);

        Theatre theatre = new Theatre("PVR");
        theatre.addScreen(screen);

        City city = new City("Bengaluru");
        city.addTheatres(theatre);

        SeatLockProvider seatLockProvider =
                new SeatLockProvider();
        BookingService bookingService =
                new BookingService(seatLockProvider);
        PaymentService paymentService =
                new InMemoryPaymentService();
        BookingOrchestrator orchestrator =
                new BookingOrchestrator(
                        bookingService,
                        paymentService
                );

        System.out.println("Movies in " + city.getCiyName() + ":");
        bookingService.getMovies(city).forEach(
                availableMovie ->
                        System.out.println(
                                "- " + availableMovie.getTitle()
                        )
        );

        System.out.println(
                "\nShows found: "
                        + bookingService.getShows(city, movie).size()
        );

        System.out.println("\nInitial availability:");
        printAvailability(
                bookingService.getSeatAvailability(show)
        );

        Customer customer1 = new Customer("C1");
        Optional<Booking> firstBooking =
                orchestrator.bookTickets(
                        customer1,
                        show,
                        Set.of(a1, a2)
                );

        System.out.println("\nC1 booking:");
        if (firstBooking.isPresent()) {
            Booking booking = firstBooking.get();
            System.out.println(
                    booking.getBookingId()
                            + " -> "
                            + booking.getStatus()
            );
        } else {
            System.out.println("Booking failed");
        }

        System.out.println("\nAvailability after C1 booking:");
        printAvailability(
                bookingService.getSeatAvailability(show)
        );

        Customer customer2 = new Customer("C2");
        Optional<Booking> competingBooking =
                orchestrator.bookTickets(
                        customer2,
                        show,
                        Set.of(a1)
                );

        System.out.println("\nC2 requests already-booked A1:");
        System.out.println(
                competingBooking.isEmpty()
                        ? "Rejected as expected"
                        : "Unexpectedly booked"
        );
    }

    private static void printAvailability(
            SortedMap<Seat, SeatStatus> availability
    ) {
        for (Map.Entry<Seat, SeatStatus> entry
                : availability.entrySet()) {
            Seat seat = entry.getKey();

            System.out.println(
                    seat.row()
                            + seat.column()
                            + " | "
                            + seat.tier()
                            + " | price="
                            + seat.tier().getPrice()
                            + " | "
                            + entry.getValue()
            );
        }
    }
}
