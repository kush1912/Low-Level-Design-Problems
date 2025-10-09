package lld.DesignProblems;

//import lld.DesignProblems.movieBookingSystem.MovieDriver;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class DesignApplication {




	public static void main(String[] args) {


//		SpringApplication.run(DesignApplication.class, args);
	}



	/*
	1. Make Util class
		- Id generator
	2. Enums, Models, Services, constants
	2. In the driver code always start from the smallest Difficulty class
	3. Add logs after Each Action
	4. Focus on Problem Action by Action and keep checking after each action.
	5. Make Shared Resources Static
	6. Comparable and Comparator
	7. Make Constructor
		- Getter & Setter
		- Constructor Methods
		- Put Services in try Catch Blocks

	8. Implementation of Stack, Queue, Priority Queue, Map, Set
		- Stack
			- pop = seek
		- Queue
			- Queue<String> str_queue = new LinkedList<> ();
			- Queue<String> priority_queue = new PriorityQueue<> ();
			- PriorityQueue<Person> pqByName = new PriorityQueue<>(Comparator.comparing(Person::getName));
			- Queue<Integer> queue1 = new ArrayDeque<> ();
			- push() = add() = offer()
				- offer returns true on adding the element and false if queue is full while add throws exception.
			- front() = peek()
			- pop() = poll() = remove()
			    - poll throws null if there are no elements and remove throws exception.
			- isEmpty()
	9. if you add Object in any data structure, you need to implement Comparable Interface and override compareTo() Method.
	10. int comparison = date1.compareTo(date2);

	* */

}
