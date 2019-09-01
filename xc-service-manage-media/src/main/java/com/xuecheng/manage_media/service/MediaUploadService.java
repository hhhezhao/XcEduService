package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.DigestInputStream;
import java.util.*;

@Service
public class MediaUploadService {


    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;


    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;


    // 根据文件Md5得到文件路径
    private String getFilePath(String fileMd5, String fileExt){
        String filePath = upload_location + fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
        return filePath;
    }

    // 得到文件目录相对路径，去掉根目录
    private String getFileFolderRelativePath(String fileMd5){
        String filePath = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
        return filePath;
    }

    // 得到文件所在目录
    private String getFileFolderPath(String fileMd5){
        String fileFolderPath = upload_location + fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
        return fileFolderPath;
    }

    // 得到块文件所在目录
    private String getChunkFileFolderPath(String fileMd5){
        String fileChunkFolderPath = upload_location + fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
        return fileChunkFolderPath;
    }

    // 创建文件目录
    private boolean createFileFolder(String fileMd5){
        // 创建上传文件目录
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    // 创建块文件目录
    private boolean createChunkFileFolder(String fileMd5){
        // 创建上传文件目录
        String fileFolderPath = getChunkFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }



    /**
     * 上传文件注册
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 检查文件是否上传
        // 1、得到文件路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);

        // 2、查询数据库是否存在文件
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        // 文件存在直接返回
        if(file.exists() && optional.isPresent()){
            ExceptionCast.cast(MediaCode.CHUNK_FILE_EXIST_CHECK);
        }
        boolean fileFold = createFileFolder(fileMd5);
        if(!fileFold){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_CREATEFOLDER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * 检查块文件是否存在
     * @param fileMd5
     * @param chunk 块的下标
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        // 得到块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件的文件名称以1,2,3..序号命名，没有扩展名
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        } else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    // 块文件上传
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        if(file == null){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }
        // 创建块文件目录
        boolean fileFolder = createChunkFileFolder(fileMd5);
        // 块文件
        File chunkFile = new File(getChunkFileFolderPath(fileMd5) + chunk);
        // 上传块文件
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkFile);
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 合并分块
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 获取文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if(chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        // 合并文件路径
        File mergeFile = new File(getFilePath(fileMd5,fileExt));
        // 创建合并文件，如果存在则删除
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        boolean newFile = false;
        try {
            newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!newFile){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        // 获取块文件，并且已经拍好序
        List<File> chunkFIies = getChunkFIies(chunkFileFolder);
        // 合并文件
        mergeFile = mergeFile(mergeFile, chunkFIies);
        if(mergeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        // 判断Md5值
        boolean checkResult = checkFileMd5(mergeFile, fileMd5);
        if (!checkResult){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        // 将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        // 状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);

    }

    public ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optional.get();
        // 发送视频消息
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        // 发送的消息
        String msg = JSON.toJSONString(msgMap);

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);

    }

    private boolean checkFileMd5(File mergeFile, String md5) {
        if(mergeFile == null || StringUtils.isEmpty(md5)){
            return false;
        }
        // 进行md5校验
        FileInputStream mergeFileInputStream = null;
        try {
            mergeFileInputStream = new FileInputStream(mergeFile);
            // 得到文件的Md5
            String md5Hex = DigestUtils.md5Hex(mergeFileInputStream);
            // 比较md5
            if(md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mergeFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private File mergeFile(File mergeFile, List<File> chunkFIies) {
        try {
            // 创建写入对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            // 遍历分块文件开始合并
            byte[] b = new byte[1024];
            for(File chunkFile : chunkFIies){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while((len = raf_read.read(b)) != -1){
                    // 像合并文件中写数据
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mergeFile;
    }

    // 获取多有块文件
    private List<File> getChunkFIies(File chunkfileFolder){
        // 获取路径下的所有文件
        File[] files = chunkfileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName()) ){
                    return 1;
                }
                return -1;
            }
        });
        return fileList;
    }


}
