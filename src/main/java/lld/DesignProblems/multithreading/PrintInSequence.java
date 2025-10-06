package lld.DesignProblems.multithreading;

/*
* Print Numbers in Sequence Using Three Threads
*/
class PrintInSequence {
    private final int limit;
    private int counter = 1;
    private int threadId = 1;

    public PrintInSequence(int limit) {
        this.limit = limit;
    }

    public void printNumber(int threadNum) {
        synchronized (this) {
            while (counter <= limit) {
                while (threadId != threadNum) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (counter <= limit) {
                    System.out.println("Thread " + threadNum + ": " + counter);
                    counter++;
                    threadId = threadNum % 3 + 1;
                    notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        PrintInSequence printInSequence = new PrintInSequence(10);

        Thread t1 = new Thread(() -> printInSequence.printNumber(1));
        Thread t2 = new Thread(() -> printInSequence.printNumber(2));
        Thread t3 = new Thread(() -> printInSequence.printNumber(3));

        t1.start();
        t2.start();
        t3.start();
    }
}

