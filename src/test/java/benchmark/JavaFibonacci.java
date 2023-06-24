package benchmark;

public class JavaFibonacci {
    private static int fib(int n) {
        if (n == 0 || n == 1)
            return n;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int result = fib(35) + fib(35) + fib(35);
        long end = System.currentTimeMillis();
        System.out.println("Result: " + result);
        System.out.println("Execution took " + (end - start) + "ms");
    }
}
