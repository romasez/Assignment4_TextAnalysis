package assignment4;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Tests {
    private Tests() {}
    private static int assertions = 0;

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }

    private static void verifyMatches(String text, String pattern, List<Integer> expected) {
        var naive = Algorithms.naiveMatch(text, pattern).indices();
        var kmp = Algorithms.kmpMatch(text, pattern).indices();
        var rk = Algorithms.rabinKarpMatch(text, pattern).indices();
        check(naive.equals(expected), "Naive: " + text + " / " + pattern + " => " + naive);
        check(kmp.equals(expected), "KMP: " + text + " / " + pattern + " => " + kmp);
        check(rk.equals(expected), "Rabin-Karp: " + text + " / " + pattern + " => " + rk);
    }

    public static void run() {
        check(Algorithms.lcs("ABC", "ABC") == 3, "LCS identical");
        check(Algorithms.lcs("AAA", "BBB") == 0, "LCS different");
        check(Algorithms.lcs("", "ABC") == 0, "LCS empty");
        check(Algorithms.lcs("ABCBDAB", "BDCABA") == 4, "LCS classic");
        check(Algorithms.editDistance("abc", "abc") == 0, "Edit identical");
        check(Algorithms.editDistance("", "abc") == 3, "Edit empty");
        check(Algorithms.editDistance("abc", "") == 3, "Edit reverse empty");
        check(Algorithms.editDistance("cat", "cut") == 1, "Edit substitution");
        check(Algorithms.editDistance("cat", "cart") == 1, "Edit insertion");
        check(Algorithms.editDistance("cart", "cat") == 1, "Edit deletion");
        check(Algorithms.editDistance("kitten", "sitting") == 3, "Edit classic");

        verifyMatches("abcde", "ab", List.of(0));                // beginning
        verifyMatches("abcde", "bc", List.of(1));                // middle
        verifyMatches("abcde", "de", List.of(3));                // end
        verifyMatches("ababa", "aba", List.of(0, 2));           // overlap
        verifyMatches("aaaaa", "aa", List.of(0, 1, 2, 3));      // many
        verifyMatches("abcde", "xy", List.of());                 // absent
        verifyMatches("ab", "abc", List.of());                   // longer
        verifyMatches("abc", "", List.of(0, 1, 2, 3));          // empty pattern
        verifyMatches("", "", List.of(0));                      // both empty
        verifyMatches("", "a", List.of());                      // empty text
        verifyMatches("aaabaaab", "aaab", List.of(0, 4));       // repeated prefixes
        verifyMatches("ABABABAC", "ABABAC", List.of(2));        // KMP fallback

        String banana = "banana";
        int[] sa = {5, 3, 1, 0, 4, 2};
        int[] expectedLcp = {0, 1, 3, 0, 0, 2};
        check(Arrays.equals(Algorithms.suffixArrayForTesting(banana), sa), "banana suffix array");
        check(Arrays.equals(Algorithms.kasai(banana, sa), expectedLcp), "banana LCP");
        check(Arrays.equals(Algorithms.kasai("", new int[0]), new int[0]), "empty Kasai");
        check(Arrays.equals(Algorithms.kasai("a", new int[]{0}), new int[]{0}), "one-char Kasai");
        check(Arrays.equals(Algorithms.kasai("aaaa", new int[]{3, 2, 1, 0}), new int[]{0, 1, 2, 3}), "repeats Kasai");
        boolean rejected = false;
        try { Algorithms.kasai("abc", new int[]{0, 0, 2}); }
        catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "Kasai should reject invalid suffix-array permutation");

        Random random = new Random(12345);
        String alphabet = "ABCD";
        for (int test = 0; test < 200; test++) {
            StringBuilder t = new StringBuilder(), p = new StringBuilder();
            for (int i = 0, size = random.nextInt(18); i < size; i++) t.append(alphabet.charAt(random.nextInt(4)));
            for (int i = 0, size = random.nextInt(7); i < size; i++) p.append(alphabet.charAt(random.nextInt(4)));
            String text = t.toString(), pattern = p.toString();
            verifyMatches(text, pattern, Algorithms.naiveMatch(text, pattern).indices());
            int[] suffix = Algorithms.suffixArrayForTesting(text);
            int[] lcp = Algorithms.kasai(text, suffix);
            for (int i = 1; i < suffix.length; i++) {
                int a = suffix[i - 1], b = suffix[i], length = 0;
                while (a + length < text.length() && b + length < text.length()
                        && text.charAt(a + length) == text.charAt(b + length)) length++;
                check(lcp[i] == length, "Kasai vs direct prefix scan");
            }
        }
        System.out.println("PASS: " + assertions + " assertions (including 200 randomized test cases)");
    }
}
