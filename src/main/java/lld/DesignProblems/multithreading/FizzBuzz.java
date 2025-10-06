package lld.DesignProblems.multithreading;

class FizzBuzz {
    private final int n;
    private int current = 1;

    public FizzBuzz(int n) {
        this.n = n;
    }

    public synchronized void fizz(Runnable printFizz) throws InterruptedException {
        while (current <= n) {
            if (current % 3 == 0 && current % 5 != 0) {
                printFizz.run();
                current++;
                notifyAll();
            } else {
                wait();
            }
        }
    }

    public synchronized void buzz(Runnable printBuzz) throws InterruptedException {
        while (current <= n) {
            if (current % 5 == 0 && current % 3 != 0) {
                printBuzz.run();
                current++;
                notifyAll();
            } else {
                wait();
            }
        }
    }

    public synchronized void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        while (current <= n) {
            if (current % 3 == 0 && current % 5 == 0) {
                printFizzBuzz.run();
                current++;
                notifyAll();
            } else {
                wait();
            }
        }
    }

    public synchronized void number(Runnable printNumber) throws InterruptedException {
        while (current <= n) {
            if (current % 3 != 0 && current % 5 != 0) {
                printNumber.run();
                current++;
                notifyAll();
            } else {
                wait();
            }
        }
    }

    public static void main(String[] args) {
        FizzBuzz fizzBuzz = new FizzBuzz(15);

        Thread t1 = new Thread(() -> {
            try {
                fizzBuzz.fizz(() -> System.out.println("Fizz"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                fizzBuzz.buzz(() -> System.out.println("Buzz"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                fizzBuzz.fizzbuzz(() -> System.out.println("FizzBuzz"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread t4 = new Thread(() -> {
            try {
                fizzBuzz.number(() -> System.out.println(fizzBuzz.current));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
