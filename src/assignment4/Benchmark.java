package assignment4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public final class Benchmark {
    private static final int RUNS = 5;
    private static final String ALPHABET = "ABCD";
    private static volatile long blackhole;
    private static final long SEED = 20261108L;
    private Benchmark() {}

    private static String randomString(Random random, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        return new String(chars);
    }

    private static double avgMillis(Supplier<Long> task) {
        // The supplier runs only the algorithm, with no input generation, file I/O or printing.
        blackhole = task.get(); // Untimed JIT warmup.
        long totalNs = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            blackhole = task.get();
            totalNs += System.nanoTime() - start;
        }
        return totalNs / (RUNS * 1_000_000.0);
    }

    public static void run() throws IOException {
        Path results = Path.of("results");
        Files.createDirectories(results);
        Random random = new Random(SEED);
        List<String> dp = new ArrayList<>();
        dp.add("algorithm,input_size,avg_ms,runs,cell_updates");
        for (int n : new int[]{100, 500, 1000}) {
            String a = randomString(random, n), b = randomString(random, n);
            double lcs = avgMillis(() -> (long) Algorithms.lcs(a, b));
            double edit = avgMillis(() -> (long) Algorithms.editDistance(a, b));
            dp.add("LCS," + n + "," + lcs + "," + RUNS + "," + (long)n * n);
            dp.add("Edit Distance," + n + "," + edit + "," + RUNS + "," + (long)n * n);
        }
        Files.write(results.resolve("dp.csv"), dp);

        List<String> match = new ArrayList<>();
        match.add("algorithm,text_length,pattern_case,pattern_length,avg_ms,runs,character_comparisons,occurrences");
        for (int n : new int[]{10000, 50000, 100000}) {
            String text = randomString(random, n);
            String present = "ABCDABCDABCDABCD";
            // Guarantee at least one present occurrence by insertion before benchmarking.
            int position = n / 2;
            text = text.substring(0, position) + present + text.substring(position + present.length());
            String absent = "ZZZZZZZZZZZZZZZZ";
            for (String kind : new String[]{"present", "absent"}) {
                String pattern = kind.equals("present") ? present : absent;
                final String workload = text;
                var baseline = Algorithms.naiveMatch(workload, pattern);
                var kmp = Algorithms.kmpMatch(workload, pattern);
                var rk = Algorithms.rabinKarpMatch(workload, pattern);
                if (!baseline.indices().equals(kmp.indices()) || !baseline.indices().equals(rk.indices()))
                    throw new IllegalStateException("Matching algorithms disagree for " + n + " / " + kind);
                double naiveMs = avgMillis(() -> (long) Algorithms.naiveMatch(workload, pattern).indices().size());
                double kmpMs = avgMillis(() -> (long) Algorithms.kmpMatch(workload, pattern).indices().size());
                double rkMs = avgMillis(() -> (long) Algorithms.rabinKarpMatch(workload, pattern).indices().size());
                match.add("Naive," + n + "," + kind + "," + pattern.length() + "," + naiveMs + "," + RUNS + "," + baseline.characterComparisons() + "," + baseline.indices().size());
                match.add("KMP," + n + "," + kind + "," + pattern.length() + "," + kmpMs + "," + RUNS + "," + kmp.characterComparisons() + "," + kmp.indices().size());
                match.add("Rabin-Karp," + n + "," + kind + "," + pattern.length() + "," + rkMs + "," + RUNS + "," + rk.characterComparisons() + "," + rk.indices().size());
            }
        }
        Files.write(results.resolve("matching.csv"), match);

        List<String> kasai = new ArrayList<>();
        kasai.add("algorithm,input_size,avg_ms,runs,lcp_length");
        for (int n : new int[]{1000, 5000, 10000}) {
            String text = randomString(random, n);
            int[] suffixArray = Algorithms.suffixArrayForTesting(text); // deliberately NOT timed
            double ms = avgMillis(() -> (long) Algorithms.kasai(text, suffixArray).length);
            kasai.add("Kasai," + n + "," + ms + "," + RUNS + "," + suffixArray.length);
        }
        Files.write(results.resolve("kasai.csv"), kasai);
        System.out.println("Generated results/dp.csv, results/matching.csv, results/kasai.csv");
        System.out.println("Machine/JVM: " + System.getProperty("os.name") + ", " + System.getProperty("java.version"));
    }
}
