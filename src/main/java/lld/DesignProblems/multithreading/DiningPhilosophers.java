package lld.DesignProblems.multithreading;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DiningPhilosophers extends Thread {
    private final int id;
    private final Lock leftFork, rightFork;

    public DiningPhilosophers(int id, Lock leftFork, Lock rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    @Override
    public void run() {
        try {
            while (true) {
                think();
                eat();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        System.out.println("Philosopher " + id + " is thinking.");
        Thread.sleep(1000);
    }

    private void eat() throws InterruptedException {
        leftFork.lock();
        rightFork.lock();
        try {
            System.out.println("Philosopher " + id + " is eating.");
            Thread.sleep(1000);
        } finally {
            leftFork.unlock();
            rightFork.unlock();
        }
    }

    public static void main(String[] args) {
        Lock[] forks = new ReentrantLock[5];
        for (int i = 0; i < 5; i++) {
            forks[i] = new ReentrantLock();
        }

        for (int i = 0; i < 5; i++) {
            new DiningPhilosophers(i, forks[i], forks[(i + 1) % 5]).start();
        }
    }
}
