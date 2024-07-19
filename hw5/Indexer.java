import java.io.Serializable;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Indexer implements Serializable{
    private static final long serialVersionUID = 1L;
    private double docCount =0;
    private List<String> doc = new ArrayList<>();
    private HashMap<String, Double> tf = new HashMap<>();
    private HashMap<String,Set<Integer>> AppearedTime = new HashMap<>();
    private HashMap<String, Double> tfIdf = new HashMap<>();

    public Indexer(HashMap<String,Set<Integer>> AppearedTime , double docCount){
        this.AppearedTime = AppearedTime;
        this.docCount = docCount;
    }

    public Indexer(HashMap<String, Double> tfIdf) {
        this.tfIdf = tfIdf;
    }

    public void getSizeofDoc(){
        System.out.println(doc.size()+" "+tf.size());
    }

    public void getSizeOfAppearedTime() {
        System.out.println(AppearedTime.size());
    }

    public Set<Integer> getTerm(String term){
        return AppearedTime.get(term);
    }

    public double getSizeOfTermAppeard(String term) {
        return AppearedTime.get(term).size();
    }

    public double getDocCount(){
        return docCount;
    }

    public double getTF(String term){
        return tf.get(term);
    }

    public double getTfIdf(String term) {
        if (tfIdf.get(term) == null ) {
            return 0;
        } else {
            return tfIdf.get(term);
        }
    }
}
