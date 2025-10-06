package lld.DesignProblems.multithreading;

class PrintEvenOdd {
    private final int limit;
    private int counter = 1;

    public PrintEvenOdd(int limit) {
        this.limit = limit;
    }

    private void printNumber(boolean isEven) {
        synchronized (this) {
            while (counter < limit) {
                if ((counter % 2 == 0) == isEven) {
                    System.out.println(Thread.currentThread().getName() + " - " + counter);
                    counter++;
                    notify();
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            notify(); // Ensure the other thread can exit if waiting
        }
    }

    public static void main(String[] args) {
        PrintEvenOdd printEvenOdd = new PrintEvenOdd(10);

        Runnable printOdd = () -> printEvenOdd.printNumber(false);
        Runnable printEven = () -> printEvenOdd.printNumber(true);

        Thread t1 = new Thread(printOdd, "OddThread");
        Thread t2 = new Thread(printEven, "EvenThread");

        t1.start();
        t2.start();
    }
}
