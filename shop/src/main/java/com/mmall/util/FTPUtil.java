package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by geely
 */
public class FTPUtil {

    private static  final Logger logger = LoggerFactory.getLogger(FTPUtil.class);
//mmall.properties
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    //构造器
    public FTPUtil(String ip,int port,String user,String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }
    public static boolean uploadFile(List<File> fileList) throws IOException {  //布尔类型返回上传成功或失败
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img",fileList);//传ftp文件夹下的img文件夹下
        logger.info("开始连接ftp服务器,结束上传,上传结果:{}");
        return result;
    }


    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException { //remotePath远程路径  上传到ftp文件夹下的文件夹用到该方法
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接FTP服务器
        if(connectServer(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);//需不需要切换文件夹
                ftpClient.setBufferSize(1024);//缓冲区
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//  2进制  防止乱码
                ftpClient.enterLocalPassiveMode();
                for(File fileItem : fileList){//chaun
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fis);  //storeFile存储文件
                }

            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded = false;//异常时该方法返回false
                e.printStackTrace();
            } finally {
                fis.close();
                ftpClient.disconnect();//释放连接
            }
        }
        return uploaded;
    }



    private boolean connectServer(String ip,int port,String user,String pwd){//封装连接服务器  IP  端口  用户  密码

        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }











    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
