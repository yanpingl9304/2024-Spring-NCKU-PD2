import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class TFIDFSearch {
    static Map<Integer, Indexer> indexerCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        String corpusFile = args[0].concat(".txt");
        String tc = args[1];
        List<String> queryList = new ArrayList<>();
        
        try {
            Path myPath = Paths.get(tc);
            queryList = Files.readAllLines(myPath);
            
            // 加載主索引
            FileInputStream fis = new FileInputStream(corpusFile + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Indexer deserializedIdx = (Indexer) ois.readObject();
            ois.close();
            fis.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            ExecutorService executor = Executors.newFixedThreadPool(1);

            int n = Integer.parseInt(queryList.get(0));
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 1; i < queryList.size(); i++) {
                String query = queryList.get(i);
                futures.add(executor.submit(() -> processQuery(deserializedIdx, query, n, corpusFile)));
            }

            for (Future<String> future : futures) {
                writer.write(future.get());
                writer.newLine();
            }

            executor.shutdown();
            writer.close();
        } catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    static String processQuery(Indexer deserializedIdx, String query, int n, String corpusFile) {
        List<String> queryWords = new ArrayList<>();
        if (query.contains("AND") || query.contains("OR")) {
            queryWords = processQueryTerms(query);
        } else {
            queryWords.add(query);
        }

        Set<Integer> section = new HashSet<>();
        if (query.contains("AND")) {
            section = Intersection(deserializedIdx, queryWords);
        } else if (query.contains("OR")) {
            section = Union(deserializedIdx, queryWords);
        } else {
            section.addAll(deserializedIdx.getTerm(queryWords.get(0)));
        }

        if (section.isEmpty()) {
            return String.join(" ", Collections.nCopies(n, "-1"));
        } else {
            Map<Integer, Double> store = new HashMap<>();
            for (Integer docID : section) {
                store.put(docID, computeTfIdfSum(docID, queryWords, corpusFile));
            }

            return getTopNResults(store, n);
        }
    }

    static List<String> processQueryTerms(String query) {
        List<String> queryWords = new ArrayList<>();
        if (query.contains("AND")) {
            for (String word : query.replaceAll("AND", " ").split("\\s+")) queryWords.add(word);
        } else if (query.contains("OR")) {
            for (String word : query.replaceAll("OR", " ").split("\\s+")) queryWords.add(word);
        }
        return queryWords;
    }

    static double computeTfIdfSum(Integer docID, List<String> queryWords, String corpusFile) {
        double sum = 0;
        String wantOpen = 1 + corpusFile + docID + ".ser";
        Indexer deserializedIdx1 = indexerCache.computeIfAbsent(docID, k -> {
            try (FileInputStream fis1 = new FileInputStream(wantOpen);
                    ObjectInputStream ois1 = new ObjectInputStream(fis1)) {
                return (Indexer) ois1.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });

        if (deserializedIdx1 != null) {
            for (String queryWord : queryWords) {
                sum += deserializedIdx1.getTfIdf(queryWord);
            }
        }
        return sum;
    }

    static String getTopNResults(Map<Integer, Double> store, int n) {
        PriorityQueue<Map.Entry<Integer, Double>> maxHeap = new PriorityQueue<>(
                n, (a, b) -> {
                    int tfidfComparison = Double.compare(b.getValue(), a.getValue());
                    if (tfidfComparison != 0) {
                        return tfidfComparison;
                    } else {
                        return Integer.compare(a.getKey(), b.getKey());
                    }
                }
        );

        maxHeap.addAll(store.entrySet());

        List<Map.Entry<Integer, Double>> topN = new ArrayList<>();
        for (int i = 0; i < n && !maxHeap.isEmpty(); i++) {
            topN.add(maxHeap.poll());
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < topN.size(); i++) {
            if (i == topN.size() - 1) {
                // System.out.println(topN.get(i).getValue());
                result.append(topN.get(i).getKey());
            } else {
                // System.out.print(topN.get(i).getValue()+" ");
                result.append(topN.get(i).getKey()).append(" ");
            }
        }

        // 補 -1
        for (int i = topN.size(); i < n; i++) {
            result.append(" -1");
        }

        return result.toString();
    }

    static Set<Integer> Intersection(Indexer deserializedIdx, List<String> queryArray) {
        if (queryArray.isEmpty()) return Collections.emptySet();

        Set<Integer> intersection = null;
        for (String query : queryArray) {
            Set<Integer> termSet = deserializedIdx.getTerm(query);
            if (termSet == null || termSet.isEmpty()) return Collections.emptySet();
            if (intersection == null || termSet.size() < intersection.size()) {
                intersection = new HashSet<>(termSet);
            }
        }

        for (String query : queryArray) {
            Set<Integer> termSet = deserializedIdx.getTerm(query);
            if (termSet == null) return Collections.emptySet();
            intersection.retainAll(termSet);
            if (intersection.isEmpty()) return Collections.emptySet();
        }
        return intersection;
    }

    static Set<Integer> Union(Indexer deserializedIdx, List<String> queryArray) {
        if (queryArray.isEmpty()) return Collections.emptySet();

        Set<Integer> union = new HashSet<>();
        for (String query : queryArray) {
            Set<Integer> termSet = deserializedIdx.getTerm(query);
            if (termSet != null) {
                union.addAll(termSet);
            }
        }
        return union.isEmpty() ? Collections.emptySet() : union;
    }

}
