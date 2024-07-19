import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CodeGenerator {

    static String getKey(String lines){
        String key = "";

        int index = 0;
        while(lines.charAt(index) != 'c') 
        {
            index++;
        }
        index+=6;
        while (index<lines.length() && lines.charAt(index) != ' ') {
            key += lines.charAt(index);
            index++;
        }
        return key;
    }
    static String bigKey (String lines) { //有大括號的(challenge)
        String key = "";
        int index = 0;
        while(lines.charAt(index) != 's')
        {
            index++;
        }
        index+=3;
        while(lines.charAt(index) != ' ')
        {
            key += lines.charAt(index);
            index++;
        }
        return key;
    }
    public static void main(String[] args) {
		// 讀取文件
        String fileName = args[0];
        //System.out.println("File name: " + fileName);
        FileReader fileReader = new FileReader();
        List<String> lines = fileReader.read(fileName);
        String key1 = "";
        String key2 = "";
        String key3 = "";
        for(int i = 0 ; i<lines.size() ; i++) //find every "class xxxxxx" and make xxxxx to key1 2 3
        {
            if(lines.get(i).contains("class "))
            {
                if(key1.length() == 0){
                    if(lines.get(i).contains("{")) {
                        key1 = bigKey(lines.get(i));
                    }else {
                        key1 = getKey(lines.get(i));
                    }
                }else if(key2.length() == 0){
                    if(lines.get(i).contains("{")) {
                        key2 = bigKey(lines.get(i));
                    }else {
                        key2 = getKey(lines.get(i));
                    }
                }else if(key3.length() == 0){
                    if(lines.get(i).contains("{")) {
                        key3 = bigKey(lines.get(i));
                    }else {
                        key3 = getKey(lines.get(i));
                    }
                }
            }
        }
         
        if(key1.length()!=0) {
            Spliter KeySpliter1 = new Spliter();
            KeySpliter1.split(lines,key1);
        }
        if(key2.length()!=0) {
            Spliter KeySpliter2 = new Spliter();
            KeySpliter2.split(lines,key2);
        }
        if(key3.length()!=0) {
            Spliter KeySpliter3 = new Spliter();
            KeySpliter3.split(lines, key3);
        }
        
    }
}

class FileReader {
    List<String> lines;
    public List<String> read(String fileName) {
        try {
            Path myPath = Paths.get(fileName);
            lines = Files.readAllLines(myPath, StandardCharsets.UTF_8);
            for(int i = 0 ; i<lines.size() ; i++) {
                lines.set(i, lines.get(i).trim());
                lines.set(i, lines.get(i).replaceAll("\\s+"," "));

            }
        }
        catch (IOException e) {
            System.err.println("無法讀取文件 " + fileName);
            e.printStackTrace();
        }
		return lines;
    }
}

class Spliter {

    public String getAttribute(String functionName) { //切出get/setXXXXX 的XXXXX
        int idx = 3;
        String attribute = "";
        while(functionName.charAt(idx) != '(')
        {
            attribute += functionName.charAt(idx);
            idx++;
        }
        return attribute;
    }
    
    public void split(List<String> lines, String key) {
        try {
            String output = key+".java";
            File file = new File(output);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("public class "+key+" {\n");
            for(int i = 0 ; i<lines.size() ; i++)
            {
                String copyKey = key.concat(" : ");
                if(lines.get(i).contains(copyKey) && lines.get(i).contains(":")){     //make sure the line contain the key
                    //System.out.println(lines.get(i));
                    String string = "";
                    string = lines.get(i).replace(copyKey,"");
                    String first = "";
                    String second = "";
                    
                    if(string.contains("(") && string.contains(") ")) {
                        if(string.contains("+")) {
                            string = string.replace("+", "");
                        }else if (string.contains("-")) {
                            string = string.replace("-", "");
                        }
                        int idx = 0;
                        while(idx<string.length()) {
                            if(string.charAt(idx) == ')') break;
                            if(string.charAt(idx) != ')') {
                                first += string.charAt(idx);
                            }
                            idx++;
                        }
                        idx+=2;
                        first = first.concat(")");
                        while(idx<string.length()){
                            second += string.charAt(idx);
                            idx++;
                        }
                    }else{
                        if(string.contains("+")) {
                            string = string.replace("+", "");
                        }else if (string.contains("-")) {
                            string = string.replace("-", "");
                        }
                        if(!string.contains("()")) {
                            int idx = 0;
                            while(idx<string.length()) {
                                if(string.charAt(idx) == ' ') break;
                                first += string.charAt(idx);
                                idx++;
                            }
                            idx++;
                            while(idx<string.length()) {
                                second += string.charAt(idx);
                                idx++;
                            }
                        }
                    }
                    if(first.contains("(") && !first.contains(")")) {
                        first = first.concat(" ");
                        first = first.concat(second);
                        second = "void";
                    }
                    if(first.length() == 0 && second.length() == 0 ) {
                        if(lines.get(i).contains("+")) {
                            bw.write("    public void "+string+" {;}\n");
                        }else{
                            bw.write("    private void "+string+" {;}\n");
                        }
                    }else{
                        if(!first.contains("()") && !first.contains("(") && !first.contains(")")) {
                            if(lines.get(i).contains("+")) {
                                bw.write("    public "+first+" "+second+";\n");
                            }else{
                                bw.write("    private "+first+" "+second+";\n");
                            }
                        }else{
                            String attribute = "";
                            if(first.contains("set")||first.contains("get")) {
                                if(first.contains("set")) {
                                    attribute = getAttribute(first);
                                    attribute = attribute.toLowerCase().charAt(0) + attribute.substring(1);
                                    if(lines.get(i).contains("+")) {
                                        bw.write("    public "+second+" "+first+" {\n");
                                        bw.write("        this."+attribute+" = "+attribute+";\n");
                                        bw.write("    }\n");
                                    }else{
                                        bw.write("    private "+second+" "+first+" {\n");
                                        bw.write("        this."+attribute+" = "+attribute+";\n");
                                        bw.write("    }\n");
                                    }
                                }else if(first.contains("get")) {
                                    attribute = getAttribute(first);
                                    attribute = attribute.toLowerCase().charAt(0) + attribute.substring(1);
                                    if(lines.get(i).contains("+")) {
                                        bw.write("    public "+second+" "+first+" {\n");
                                        bw.write("        return "+attribute+";\n");
                                        bw.write("    }\n");
                                    }else{
                                        bw.write("    private "+second+" "+first+" {\n");
                                        bw.write("        return "+attribute+";\n");
                                        bw.write("    }\n");
                                    }
                                }
                            }else{
                                if(second.contains("int") || second.contains("String") || second.contains("boolean") || second.contains("void")) {
                                    if(second.contains("int")) {
                                        if(lines.get(i).contains("+")) {
                                            bw.write("    public "+second+" "+first+" {return 0;}\n");
                                        }else{
                                            bw.write("    private "+second+" "+first+" {return 0;}\n");
                                        }
                                    }else if(second.contains("String")) {
                                        if(lines.get(i).contains("+")) {
                                            bw.write("    public "+second+" "+first+" {return \"\";}\n");
                                        }else{
                                            bw.write("    private "+second+" "+first+" {return \"\";}\n");
                                        }
                                    }else if(second.contains("boolean")) {
                                        if(lines.get(i).contains("+")) {
                                            bw.write("    public "+second+" "+first+" {return false;}\n");
                                        }else{
                                            bw.write("    private "+second+" "+first+" {return false;}\n");
                                        }
                                    }else if(second.contains("void")) {
                                        if(lines.get(i).contains("+")) {
                                            bw.write("    public "+second+" "+first+" {;}\n");
                                        }else{
                                            bw.write("    private "+second+" "+first+" {;}\n");
                                        }
                                    }
                                }
                            }
                        }
                    }

                }else if(lines.get(i).contains(key) && lines.get(i).contains("{")) { ///////挑戰分開始處
                    for(int j = i ; j<lines.size() ; j++)
                    {
                        if(lines.get(j).contains("}")) break;
                        if(lines.get(j).contains("+") || lines.get(j).contains("-")) {
                            String string = "";
                            String first = "";
                            String second ="";
                            string = lines.get(j);
                            if(string.contains("(") && string.contains(") ")) {
                                if(string.contains("+")) {
                                    string = string.replace("+", "");
                                }else if (string.contains("-")) {
                                    string = string.replace("-", "");
                                }
                                int idx = 0;
                                while(idx<string.length()) {
                                    if(string.charAt(idx) == ')') break;
                                    if(string.charAt(idx) != ')') {
                                        first += string.charAt(idx);
                                    }
                                    idx++;
                                }
                                idx+=2;
                                first = first.concat(")");
                                while(idx<string.length()){
                                    second += string.charAt(idx);
                                    idx++;
                                }
                            }else{
                                if(string.contains("+")) {
                                    string = string.replace("+", "");
                                }else if (string.contains("-")) {
                                    string = string.replace("-", "");
                                }
                                if(!string.contains("()")) {
                                    int idx = 0;
                                    while(idx<string.length()) {
                                        if(string.charAt(idx) == ' ') break;
                                        first += string.charAt(idx);
                                        idx++;
                                    }
                                    idx++;
                                    while(idx<string.length()) {
                                        second += string.charAt(idx);
                                        idx++;
                                    }
                                }
                            }
                            if(first.length() == 0 && second.length() == 0 ) {
                                if(lines.get(j).contains("+")) {
                                    bw.write("    public void "+string+" {;}\n");
                                }else{
                                    bw.write("    private void "+string+" {;}\n");
                                }
                            }else{
                                if(!first.contains("()") && !first.contains("(") && !first.contains(")")) {
                                    if(lines.get(j).contains("+")) {
                                        bw.write("    public "+first+" "+second+";\n");
                                    }else{
                                        bw.write("    private "+first+" "+second+";\n");
                                    }
                                }else{
                                    String attribute = "";
                                    if(first.contains("set")||first.contains("get")) {
                                        if(first.contains("set")) {
                                            attribute = getAttribute(first);
                                            attribute = attribute.toLowerCase().charAt(0) + attribute.substring(1);
                                            if(lines.get(j).contains("+")) {
                                                bw.write("    public "+second+" "+first+" {\n");
                                                bw.write("        this."+attribute+" = "+attribute+";\n");
                                                bw.write("    }\n");
                                            }else{
                                                bw.write("    private "+second+" "+first+" {\n");
                                                bw.write("        this."+attribute+" = "+attribute+";\n");
                                                bw.write("    }\n");
                                            }
                                        }else if(first.contains("get")) {
                                            attribute = getAttribute(first);
                                            attribute = attribute.toLowerCase().charAt(0) + attribute.substring(1);
                                            if(lines.get(j).contains("+")) {
                                                bw.write("    public "+second+" "+first+" {\n");
                                                bw.write("        return "+attribute+";\n");
                                                bw.write("    }\n");
                                            }else{
                                                bw.write("    private "+second+" "+first+" {\n");
                                                bw.write("        return "+attribute+";\n");
                                                bw.write("    }\n");
                                            }
                                        }
        
                                    }else{
                                        if(second.contains("int") || second.contains("String") || second.contains("boolean") || second.contains("void")) {
                                            if(second.contains("int")) {
                                                if(lines.get(j).contains("+")) {
                                                    bw.write("    public "+second+" "+first+" {return 0;}\n");
                                                }else{
                                                    bw.write("    private "+second+" "+first+" {return 0;}\n");
                                                }
                                            }else if(second.contains("String")) {
                                                if(lines.get(j).contains("+")) {
                                                    bw.write("    public "+second+" "+first+" {return \"\";}\n");
                                                }else{
                                                    bw.write("    private "+second+" "+first+" {return \"\";}\n");
                                                }
                                            }else if(second.contains("boolean")) {
                                                if(lines.get(j).contains("+")) {
                                                    bw.write("    public "+second+" "+first+" {return false;}\n");
                                                }else{
                                                    bw.write("    private "+second+" "+first+" {return false;}\n");
                                                }
                                            }else if(second.contains("void")) {
                                                if(lines.get(j).contains("+")) {
                                                    bw.write("    public "+second+" "+first+" {;}\n");
                                                }else{
                                                    bw.write("    private "+second+" "+first+" {;}\n");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            bw.write("}\n");
            bw.close();
        }
        catch (IOException e) {
            System.err.println("無法讀取文件 ");
            return;
        }
    }
}