package assignment4;

public final class Main {
    private Main() {}
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("test")) {
            Tests.run();
        } else if (args[0].equals("benchmark")) {
            Tests.run();
            Benchmark.run();
        } else if (args[0].equals("demo")) {
            System.out.println("LCS(ABCBDAB, BDCABA): " + Algorithms.lcs("ABCBDAB", "BDCABA"));
            System.out.println("EditDistance(kitten, sitting): " + Algorithms.editDistance("kitten", "sitting"));
            String text = "ababa", pattern = "aba";
            System.out.println("Naive matches: " + Algorithms.naiveMatch(text, pattern).indices());
            System.out.println("KMP matches: " + Algorithms.kmpMatch(text, pattern).indices());
            System.out.println("Rabin-Karp matches: " + Algorithms.rabinKarpMatch(text, pattern).indices());
            System.out.println("Kasai banana LCP: " + java.util.Arrays.toString(
                    Algorithms.kasai("banana", new int[]{5, 3, 1, 0, 4, 2})));
        } else {
            System.err.println("Usage: java -cp out assignment4.Main [test|benchmark|demo]");
            System.exit(2);
        }
    }
}
