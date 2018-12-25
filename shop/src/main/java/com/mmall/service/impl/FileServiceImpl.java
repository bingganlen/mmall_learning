package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by geely
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();//原始文件名  如abc.jpg
        //扩展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);//后面获取 .jpg  但我们不要这个点，所以往后移一位
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;//上传文件的名字  不带重复的UUID
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName); //大括号占位

        File fileDir = new File(path);
        if(!fileDir.exists()){//fileDir文件夹不存在
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);//文件夹有了就创建文件
        try {
            file.transferTo(targetFile);
            //文件已经上传成功了


            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上

            targetFile.delete();//上传到服务器后删除upload下面的文件
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        return targetFile.getName();//返回目标文件的文件名
    }

}
