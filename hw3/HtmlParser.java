import java.text.DecimalFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlParser {
    public static void main(String[] args) { 
        List<String> stockName = getDataName();
        if(args[0].contains("0")) {
            getDataFromWeb();
        } else if (args[0].contains("1")) {
            String File = "data.csv";
            /*
            List<String> stockName = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(File))) {
                String line = br.readLine();
                if (line != null) {
                    String[] names = line.split(",");
                    for (String name : names) {
                        stockName.add(name);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            double[] Price = new double[stockName.size()];
            switch (args[1]) {
                case "0":
                    outputAllData();
                    break;
                case "1":
                    Price = getDataPrice(stockName,args[2]);
                    simpleMovingAverage(args[2],Integer.parseInt(args[3]), Integer.parseInt(args[4]), Price);
                    break;
                case "2":
                    Price = getDataPrice(stockName,args[2]);
                    standardDeviation(args[2],Integer.parseInt(args[3]), Integer.parseInt(args[4]), Price,false);
                    break;
                case "3":
                    Top3(stockName,Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                    break;
                case "4":
                    Price = getDataPrice(stockName,args[2]);
                    LinearRegression(args[2],Integer.parseInt(args[3]), Integer.parseInt(args[4]), Price);
                    break;
                default:
                    break;
            }
        }
    }

    public static List<String> getDataName() {
        List<String> stockName = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
            int day = Integer.parseInt(doc.title().substring(3)); //第X天 
            // 讀取整個網頁的 HTML 內容 and 分割
            String htmlContent = doc.html();
            String[] htmlLines = htmlContent.split("\\r?\\n");

            int stockNumber = 0;
            for (int i = 0; i < htmlLines.length; i++) {
                if (htmlLines[i].contains("<th>") && htmlLines[i].contains("</th>")) {
                    String thContent = htmlLines[i].replaceAll("<[^>]*>", ""); // 移除 HTML 標籤
                    stockName.add(thContent.trim());
                    stockNumber++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockName;
    }

    public static double[] getDataPrice(List<String> stockName, String target) {
        double[] stockPrice = new double[30];
        try {
            File myData = new File("data.csv");
            BufferedReader br = new BufferedReader(new FileReader(myData));
            int idx = 0;
            for (idx = 0 ; idx< stockName.size() ; idx++) {
                if(stockName.get(idx).equals(target)) break;
            }
            String line = "";
            int stringIdx = 0;
            double price = 0;
            while((line = br.readLine()) != null ) {
                String[] cols = line.split(",");
                if(cols.length == stockName.size() ) {
                    if(!cols[idx].contains(target) && cols.length != 1) {
                        if(!cols[idx].isEmpty()) {
                            price = Double.parseDouble(cols[idx]);
                        }
                        stockPrice[stringIdx++] = price;
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockPrice;
    }

    public static void createCSVFile(String filePath,List<String> stockName) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (!fileExists) {
                for (int i = 0; i < stockName.size(); i++) {
                    writer.write(stockName.get(i));
                    if (i != stockName.size() - 1) {
                        writer.write(",");
                    } else {
                        writer.newLine(); 
                    }
                }
                for(int i = 30 ; i>0 ; i--) writer.newLine();
            } else {
                System.out.println("exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToSpecificLine(String filePath, int lineToUpdate, String newData) {
        List<String> lines = new ArrayList<>();
        // 讀整個csv  放到 array list裡面
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //確認lines.get(i) 是不是已經有東西，把某day放到他的那行裡面
        if (lineToUpdate >= 0 && lineToUpdate <= lines.size() && lines.get(lineToUpdate).trim().isEmpty()) {
            String updatedLine = lines.get(lineToUpdate) + newData;
            lines.set(lineToUpdate, updatedLine);
        } else if (lineToUpdate == lines.size()) { // 如果是最後一行
            lines.add(newData);
        }

        // 把東西寫回csv
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getDataFromWeb () {
        try {
            List<String> stockName = getDataName();
            createCSVFile("data.csv",stockName);
            
            FileWriter fw = new FileWriter("data.csv", true); // 使用append模式
            BufferedWriter bw = new BufferedWriter(fw);
            Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
            System.out.println(doc.title());
            int day = Integer.parseInt(doc.title().substring(3)); //第X天 
            // 讀取整個網頁的 HTML 內容
            String htmlContent = doc.html();
            String[] htmlLines = htmlContent.split("\\r?\\n");
 
            double[] stockPrice = new double[stockName.size()];

            int idx = 0;
            String newData2 = "";
            for (int i = 0; i < htmlLines.length && idx < stockName.size(); i++) {
                if (htmlLines[i].contains("<td>") && htmlLines[i].contains("</td>")) {
                    String thContent = htmlLines[i].replaceAll("<[^>]*>", "").trim(); // 移除 HTML 標籤
                    newData2 += thContent;
                    if (idx != stockName.size() - 1) {
                        newData2 += ",";
                    }
                    idx++;
                }
            }
            appendToSpecificLine("data.csv", day, newData2);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputAllData() {
        File myData = new File("data.csv");
        File outputFile = new File("output.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            BufferedReader br = new BufferedReader(new FileReader(myData));
            String line = "";
            while((line = br.readLine()) != null ) {
                if(!line.isEmpty()) bw.write(line+"\n");
            }
            bw.close();
            br.close();
        } catch (Exception e) {
            return;
        }
    }

    public static void simpleMovingAverage(String target,int start, int end ,double[] stockPrice) {
        File outputFile = new File("output.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            bw.write(target+","+start+","+end+"\n");
            start--;
            double timePeriod = 5;
            boolean isFirst = true;
            DecimalFormat df = new DecimalFormat("###.##");
            for(int i = start ; i+4 < end ; i++) {
                double sum = 0;
                for(int j = 0 ; j<5 ; j++) {
                    sum += stockPrice[i+j];
                }
                double ans = sum/timePeriod;
                if(isFirst){
                    bw.write(df.format(ans));
                    isFirst =false;
                } else {
                    bw.write(","+df.format(ans));
                }
            }
            bw.write("\n");
            bw.close();
        } catch (Exception e) {
            return;
        }
    }

    public static double root(double x) { //開根號
        // 牛頓
        double a = x / 2.0;
        double b;
        do {
            b = a;
            a = (a + x / a) / 2.0;
        } while ((a - b) >= 0.0001 || (b - a) >= 0.0001);
        
        return a;
    }

    public static String standardDeviation(String target,int start, int end ,double[] stockPrice,boolean isTop3) {
        File outputFile = new File("output.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            DecimalFormat df = new DecimalFormat("###.##");
            bw.write(target+","+start+","+end+"\n");
            start--;
            double n = end - start;
            double sum = 0;
            for(int i = start ; i<end ; i++) {
                sum += stockPrice[i];
            }
            double avg = sum / n;
            double xMinusxAvg = 0;
            for(int i = start ; i < end ; i++) {
                xMinusxAvg += ((stockPrice[i]-avg) * (stockPrice[i]-avg));
            }
            double SD = 0; 
            SD = (root((xMinusxAvg/(n-1))));
            if(!isTop3) {
                bw.write(df.format(SD)+"\n");
                bw.close();
            }
            return df.format(SD);
        } catch (Exception e) {
            return null;
        }
    }
 
    public static void Top3 (List<String> stockName,int start, int end) {
        File outputFile = new File("output.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            String[][] SD = new String[stockName.size()][2];
            for(int i = 0 ; i < stockName.size() ; i++) {
                double[] Price = new double[stockName.size()];
                Price = getDataPrice(stockName,stockName.get(i));
                SD[i][0] = stockName.get(i);
                SD[i][1] = standardDeviation(stockName.get(i), start, end, Price,true);
            }
            double[] tmp = new double[stockName.size()];
            for(int i = 0 ; i<stockName.size() ; i++) {
                tmp[i] = Double.parseDouble(SD[i][1]);
            }
            Arrays.sort(tmp);
            String name = "";
            String price = "";
            for(int i = 0 ; i<3 ; i++) {
                for(int idx = 0 ; idx<stockName.size() ; idx++) {
                    if(tmp[tmp.length-1-i] == Double.parseDouble(SD[idx][1])) {
                        name += SD[idx][0];
                        price += SD[idx][1];
                        if(i != 2) name += ",";
                        if(i != 2) price += ",";
                        if(i == 2) price += "\n";
                    }
                }
            }
            bw.write(name+","+start+","+end+"\n");
            bw.write(price);
            bw.close();
        } catch (Exception e) {
            return;
        }

    }
    public static void LinearRegression(String target,int start, int end ,double[] stockPrice) {
        File outputFile = new File("output.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            bw.write(target+","+start+","+end+"\n");
            DecimalFormat df = new DecimalFormat("###.##");
            start--;
            double avg = 0;
            double timeAvg = 0;
            double n = end - start;
            double denominator  = 0;
            double numerator = 0; 
    
            for(int i = start ; i<end ; i++) {
                avg += (stockPrice[i]/n);
            }
            for(int i = start +1 ; i <= end ; i++) timeAvg += i / n;
            for(int i = start +1 ; i <= end ; i++) {
                denominator += (i - timeAvg) * (i - timeAvg);
            }
            for(int i = start ; i<end ; i++) {
                numerator += (i+1-timeAvg)*(stockPrice[i]-avg);
            }
            double slope = numerator / denominator;
            double intercept = avg - slope * timeAvg;
            bw.write(df.format(slope)+","+df.format(intercept)+"\n");
            bw.close();
        } catch (Exception e) {
            return;
        }
    }
}