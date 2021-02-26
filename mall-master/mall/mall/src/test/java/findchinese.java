import java.io.*;

public class findchinese {

    public static void readTxtFile() throws IOException {
        String path = "G:\\testChineseTaiwan.txt"; // 原日志文件
        String filename = "G:\\xml.txt"; // 存文件
        //FileReader fileReader;
        InputStreamReader isr;
        try {
            isr = new InputStreamReader(new FileInputStream(path), "utf-8");
            BufferedReader read = new BufferedReader(isr);
            String s = null;
            //List<String> list = new ArrayList<String>();
            while ((s = read.readLine()) != null) {
                if (s.trim().length() > 1) {
                    String reg = "[^\u4e00-\u9fa5]";
                    s = s.replaceAll(reg, "");
                    if (s.length()!=0){
                        PrintStream ps = new PrintStream(new FileOutputStream(filename, true));
                        ps.println(s);// 往txt文件里写入字符串
                    }
                }
            }
            System.out.println("OK！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        try {
            readTxtFile();
        } catch (IOException e) {
            System.err.println("读取出错啦。。。。。");
            e.printStackTrace();
        }
        System.err.println("执行成功");
    }

}