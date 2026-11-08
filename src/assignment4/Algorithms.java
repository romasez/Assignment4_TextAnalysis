package assignment4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Algorithms {
    private Algorithms() {}

    public record MatchResult(List<Integer> indices, long characterComparisons) {}

    public static int lcs(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1]; // Zero row and column are base cases.
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static int editDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int substitutionCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                        Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + substitutionCost));
            }
        }
        return dp[m][n];
    }

    private static List<Integer> emptyPatternMatches(int textLength) {
        List<Integer> found = new ArrayList<>(textLength + 1);
        for (int i = 0; i <= textLength; i++) found.add(i);
        return found;
    }

    public static MatchResult naiveMatch(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return new MatchResult(emptyPatternMatches(n), 0);
        List<Integer> found = new ArrayList<>();
        long comparisons = 0;
        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m) {
                comparisons++;
                if (text.charAt(start + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) found.add(start);
        }
        return new MatchResult(found, comparisons);
    }

    public static int[] lps(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    public static MatchResult kmpMatch(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return new MatchResult(emptyPatternMatches(n), 0);
        int[] lps = lps(pattern);
        List<Integer> found = new ArrayList<>();
        long comparisons = 0;
        int i = 0, j = 0;
        while (i < n) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    found.add(i - m);
                    j = lps[j - 1]; // Allow overlapping matches.
                }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return new MatchResult(found, comparisons);
    }

    public static MatchResult rabinKarpMatch(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return new MatchResult(emptyPatternMatches(n), 0);
        if (m > n) return new MatchResult(List.of(), 0);
        final long base = 256, prime = 1_000_000_007L;
        long multiplier = 1, patternHash = 0, windowHash = 0;
        for (int i = 1; i < m; i++) multiplier = (multiplier * base) % prime;
        for (int i = 0; i < m; i++) {
            patternHash = (base * patternHash + pattern.charAt(i)) % prime;
            windowHash = (base * windowHash + text.charAt(i)) % prime;
        }
        List<Integer> found = new ArrayList<>();
        long comparisons = 0;
        for (int start = 0; start <= n - m; start++) {
            if (patternHash == windowHash) {
                // A matching hash is only a candidate: verify characters on collision.
                int j = 0;
                while (j < m) {
                    comparisons++;
                    if (text.charAt(start + j) != pattern.charAt(j)) break;
                    j++;
                }
                if (j == m) found.add(start);
            }
            if (start < n - m) {
                long removed = (text.charAt(start) * multiplier) % prime;
                windowHash = (windowHash - removed + prime) % prime;
                windowHash = (windowHash * base + text.charAt(start + m)) % prime;
            }
        }
        return new MatchResult(found, comparisons);
    }

    public static int[] kasai(String text, int[] suffixArray) {
        int n = text.length();
        if (suffixArray.length != n) throw new IllegalArgumentException("Suffix array length must equal text length");
        int[] rank = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int pos = suffixArray[i];
            if (pos < 0 || pos >= n || seen[pos])
                throw new IllegalArgumentException("Suffix array must be a permutation of 0..n-1");
            seen[pos] = true;
            rank[pos] = i;
        }
        int[] lcp = new int[n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int r = rank[i];
            if (r == 0) { k = 0; continue; }
            int j = suffixArray[r - 1];
            while (i + k < n && j + k < n && text.charAt(i + k) == text.charAt(j + k)) k++;
            lcp[r] = k;
            if (k > 0) k--; // Reuse at least k-1 characters for the next suffix.
        }
        return lcp;
    }

    public static int[] suffixArrayForTesting(String text) {
        Integer[] offsets = new Integer[text.length()];
        for (int i = 0; i < offsets.length; i++) offsets[i] = i;
        Arrays.sort(offsets, (a, b) -> {
            int i = a, j = b, n = text.length();
            while (i < n && j < n) {
                int cmp = Character.compare(text.charAt(i++), text.charAt(j++));
                if (cmp != 0) return cmp;
            }
            return Integer.compare(n - a, n - b);
        });
        int[] result = new int[offsets.length];
        for (int i = 0; i < result.length; i++) result[i] = offsets[i];
        return result;
    }
}
