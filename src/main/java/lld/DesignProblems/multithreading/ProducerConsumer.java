package lld.DesignProblems.multithreading;

import java.util.LinkedList;
import java.util.Queue;

class ProducerConsumer {
    private final int MAX_CAPACITY;
    private final Queue<Integer> buffer = new LinkedList<>();

    public ProducerConsumer(int capacity) {
        this.MAX_CAPACITY = capacity;
    }

    public void produce() throws InterruptedException {
        int value = 0;
        while (true) {
            synchronized (this) {
                while (buffer.size() == MAX_CAPACITY) {
                    wait(); // Wait until there is space in the buffer
                }

                System.out.println("Produced: " + value);
                buffer.add(value++);
                notifyAll(); // Notify consumers that there is new data available
            }
            Thread.sleep(100); // Simulate time taken to produce an item
        }
    }

    public void consume() throws InterruptedException {
        while (true) {
            synchronized (this) {
                while (buffer.isEmpty()) {
                    wait(); // Wait until there is data to consume
                }

                int value = buffer.poll();
                System.out.println("Consumed: " + value);
                notifyAll(); // Notify producers that there is space in the buffer
            }
            Thread.sleep(150); // Simulate time taken to consume an item
        }
    }

    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer(5);

        Thread producerThread = new Thread(() -> {
            try {
                pc.produce();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                pc.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producerThread.start();
        consumerThread.start();
    }
}
