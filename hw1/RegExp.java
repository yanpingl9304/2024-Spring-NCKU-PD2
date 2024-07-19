import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RegExp {

    static String method1(String line){
        String checker1 = "Y";
        int length = line.length();
        for(int i = 0, j = length-1 ; i<j ; i++,j--)
        {
            if(line.charAt(i) != line.charAt(j)) {
                checker1 = "N";
                break;
            }
        }
        return checker1;
    }

    static String method2(String line, String str1){
        String checker2 = "Y";
        int count = 0;
        int length = line.length();
        int Str1length = str1.length();
        if(length < str1.length()){
            checker2 = "N";
        }else{
            for(int i = 0 ; i < length - Str1length + 1 ; i++)
            {
                int times = 0;
                for(int j = 0 ; j < Str1length ; j++)
                {
                    if(line.charAt(i+j)==str1.charAt(j)){
                        times++;
                        if(times == Str1length){
                            count++;
                        }
                    }else{
                        break;
                    }
                }
            }
            if(count == 0 ){
                checker2 = "N";
            }
        }
        return checker2;
    }

    static String method3(String line, String str2, int s2Count){
        String checker3 = "Y";
        int count2 = 0;
        int length = line.length();
        int Str2length = str2.length();
        if(length < str2.length()){
            checker3 = "N";
        }else{
            for(int i = 0 ; i < length - Str2length + 1 ; i++)
            {
                int times = 0;
                for(int j = 0 ; j < Str2length ; j++)
                {
                    
                    if(line.charAt(i+j)==str2.charAt(j)){
                        times++;
                        
                    }else{
                        break;
                    }
                    if(times == Str2length){
                        count2++;
                    }
                }
            }
            if(count2 >= s2Count){
                checker3 = "Y";
            }else{
                checker3 = "N";
            }
            //System.out.println(count2);
        }
        return checker3;
    }

    static String method4(String line)
    {
        String checker4 = "N";
        int countA=0,countB=0;
        int length = line.length();
        int i = 0;
        
        if(line.charAt(i)!='A'){
            while(i<length)
            {
                if(line.charAt(i)!='A'){
                    i++;
                }else{
                    break;
                }
            }
            while(i<length)
            {
                if(line.charAt(i)=='A'){
                    countA++;
                    i++;
                }else{
                    break;
                }
            }
            while(i<length){
                if(line.charAt(i)!='B'){
                    if(countA >=1 && countB >= 2){
                        checker4 = "Y";
                    }
                    countB = 0;
                    i++;
                }else if(line.charAt(i)=='B'){
                    countB++;
                    i++;
                }
            }
            if(countA >=1 && countB >= 2){
                checker4 = "Y";
            }
        }else{
            while(i<length)
            {
                if(line.charAt(i)=='A'){
                    countA++;
                    i++;
                }else{
                    break;
                }
            }
            while(i<length){
                if(line.charAt(i)!='B'){
                    if(countA >=1 && countB >= 2){
                        checker4 = "Y";
                        break;
                    }
                    countB = 0;
                    i++;
                }else if(line.charAt(i)=='B'){
                    countB++;
                    i++;
                }
            }
            if(countA >=1 && countB >= 2){
                checker4 = "Y";
            }
        }
        
       return checker4; 
    }
    
    public static void main(String[] args) {
        String str1 = args[1];
        String str2 = args[2];
        int s2Count = Integer.parseInt(args[3]);

        /* 
        //For your testing of input correctness
        System.out.println("The input file:"+args[0]);
        System.out.println("str1="+str1);
        System.out.println("str2="+str2);
        System.out.println("num of repeated requests of str2 = "+s2Count);
        */

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {
                //You main code should be invoked here
                
                line = line.toUpperCase();
                str1 = str1.toUpperCase();
                str2 = str2.toUpperCase();
                String checker1 = method1(line);
                String checker2 = method2(line,str1);
                String checker3 = method3(line,str2,s2Count);
                String checker4 = method4(line);
                System.out.println(checker1+","+checker2+","+checker3+","+checker4);

            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}