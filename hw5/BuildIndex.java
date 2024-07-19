import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BuildIndex {
    public static void main(String[] args) {
        String corpusFilePath = args[0];
        int index = corpusFilePath.lastIndexOf("/");
        String fileName = corpusFilePath.substring(index + 1);

        List<List<String>> docs = new ArrayList<>();
        HashMap<String, Set<Integer>> appearedTime = new HashMap<>();
        List<HashMap<String, Double>> tfList = new ArrayList<>();
        int docID = 0;

        try {
            List<String> lines;
            Path myPath = Paths.get(corpusFilePath);
            lines = Files.readAllLines(myPath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i += 5) {
                List<String> doc = new ArrayList<>();
                HashMap<String, Integer> wordCount = new HashMap<>();

                for (int j = 0; j < 5; j++) {
                    String[] words = parseText(lines.get(i + j));
                    for (String word : words) {
                        doc.add(word);
                        wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                        appearedTime.putIfAbsent(word, new HashSet<>());
                        appearedTime.get(word).add(docID); // 將文章索引加入集合中
                    }
                }
                
                // 計算 TF
                HashMap<String, Double> tf = new HashMap<>();
                int totalWords = doc.size();
                for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                    tf.put(entry.getKey(), entry.getValue() / (double) totalWords);
                }
                tfList.add(tf);
                docs.add(doc);
                docID++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 計算 TF-IDF
        List<HashMap<String, Double>> tfidfList = new ArrayList<>();
        for (int i = 0; i < tfList.size(); i++) {
            Set<String> keySet = tfList.get(i).keySet();
            HashMap<String, Double> tfIdf = new HashMap<>();
            for (String key : keySet) {
                double idf = 0;
                if(!(appearedTime.get(key).size() == 0)) {
                    idf = Math.log((double) docs.size() / (appearedTime.get(key).size()));
                }
                double tfidf = tfList.get(i).get(key) * idf;
                tfIdf.put(key, tfidf);
            }
            tfidfList.add(tfIdf);
        }
        
        docID = 0;
        for(HashMap<String, Double> tfIdf : tfidfList) {
            Indexer idx = new Indexer(tfIdf);
            String indexPath = fileName + docID+".ser";
            indexPath = 1 + indexPath;
            try {
                FileOutputStream fos = new FileOutputStream(indexPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(idx);
                // System.out.println("成功"+docID);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();	
            }
            docID++;
        }
        
         
        Indexer idx = new Indexer(appearedTime,docs.size());
        String indexPath = fileName+".ser";
        try {
            FileOutputStream fos = new FileOutputStream(indexPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(idx);
            // System.out.println("成功");
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();	
        }
    }

    public static String[] parseText(String inputText) {
        return inputText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim().split("\\s+");
    }
}

