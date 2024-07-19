import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.lang.StringBuilder;

public class TFIDFCalculator {

    static List<TrieNode> TrieTree = new ArrayList<>();
    static List<String> appearedTerms = new ArrayList<>();
    static List<Double> appearedTermsNum = new ArrayList<>();

    public static void main(String[] args) {
        List<List<String>> docs = new ArrayList<>();
        List<String> docIndex = new ArrayList<>();
        List<String> terms = new ArrayList<>();
        docs = StoreInArrayList (args[0]);
        double tfIdf = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[1]));
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            String termsArray[] = parseText(line1);
            for(String term : termsArray) terms.add(term);
            String docIndexArray[] = line2.split(" ");
            for(String idx : docIndexArray) docIndex.add(idx);
            reader.close();
        } catch (IOException e) {
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            for(int i = 0 ; i < docIndex.size() ; i++) {
                int idx = Integer.parseInt(docIndex.get(i));
                tfIdf = tfIdfCalculate(docs.get(idx) , docs, terms.get(i),TrieTree);
                if( i != docIndex.size()-1) {
                    writer.write(String.format("%.5f", tfIdf)+" ");
                } else {
                    writer.write(String.format("%.5f", tfIdf));
                }
            }
            writer.close();
        } catch (IOException e){
            return;
        }
    }

    public static List<List<String>> StoreInArrayList(String file) {
        List<List<String>> docs = new ArrayList<>();
        List<String> lines;
        try {
            Path myPath = Paths.get(file);
            lines = Files.readAllLines(myPath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i += 5) {
                TrieNode root = new TrieNode();
                List<String> doc = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    String[] words = parseText(lines.get(i + j));
                    for (String term : words) {
                        doc.add(term);
                        insert(term,root);
                    }
                }
                TrieTree.add(root);
                docs.add(doc);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return docs;
    }

    static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isEndOfWord = false;
    }

    public static void insert(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
    }

    public static String[] parseText(String inputText) {
        return inputText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim().split("\\s+");
    }

    public static double tf(List<String> doc, String term) {
        double number_term_in_doc = 0;
        for(int i = 0 ; i < doc.size() ; i++) {
            if(doc.get(i).equalsIgnoreCase(term)) number_term_in_doc++;
        }
        return number_term_in_doc / doc.size();
    }
    
    public static double idf(List<List<String>> docs, String term,List<TrieNode> TrieTree) {
        double number_doc_contain_term = 0;

        if(appearedTerms.contains(term)) {
            int index = appearedTerms.indexOf(term);
            return appearedTermsNum.get(index);
        } else {
            appearedTerms.add(term);
            for(int i = 0 ; i < TrieTree.size() ; i++) {
                if(search(term,TrieTree.get(i))) number_doc_contain_term++;
            }
            appearedTermsNum.add(Math.log(docs.size() / number_doc_contain_term));
            return Math.log(docs.size() / number_doc_contain_term);
        }

    }

    public static double tfIdfCalculate(List<String> doc, List<List<String>> docs, String term, List<TrieNode> TrieTree) {
        return tf(doc, term) * idf(docs, term, TrieTree);
    }

    public static boolean search(String word,TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }
}