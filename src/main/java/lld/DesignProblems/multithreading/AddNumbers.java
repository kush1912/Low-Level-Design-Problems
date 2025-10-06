package lld.DesignProblems.multithreading;

class AddNumbers implements Runnable {
    private int num1, num2;
    private int result;

    public AddNumbers(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
    }

    @Override
    public void run() {
        result = num1 + num2;
        System.out.println("The sum is: " + result);
    }

    public static void main(String[] args) {
        int num1 = 5;
        int num2 = 10;

        AddNumbers addNumbersTask = new AddNumbers(num1, num2);
        Thread addNumbersThread = new Thread(addNumbersTask);
        addNumbersThread.start();
    }
}
