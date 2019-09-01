package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;

    @Autowired
    FileSystemRepository fileSystemRepository;

    // 加载fdfs配置
    private void initFdfsConfig(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    // 上传文件
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata){
        if(multipartFile == null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        // 上传文件到fdfs
        String fileId = fdfs_upload(multipartFile);
        if(StringUtils.isEmpty(fileId)){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_BUSINESSISNULL);
        }
        // 创建文件对象
        FileSystem fileSystem = new FileSystem();
        // 文件Id

        fileSystem.setFileId(fileId);
        // 文件在系统中的路径
        fileSystem.setFilePath(fileId);
        // 业务标识
        fileSystem.setBusinesskey(businesskey);
        // 标签
        fileSystem.setFiletag(filetag);
        // 元数据
        if(StringUtils.isEmpty(metadata)){
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        // 名称
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        // 大小
        fileSystem.setFileSize(multipartFile.getSize());
        // 文件类型
        fileSystem.setFileType(multipartFile.getContentType());

        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    // 上传文件到fastdfs，返回文件Id
    public String fdfs_upload(MultipartFile multipartFile){

        try {
            // 加载配置文件
            initFdfsConfig();
            // 创建tracker client
            TrackerClient trackerClient = new TrackerClient();
            // 获取trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            // 获取storageServer
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            // 创建storage client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
            // 上传文件
            // 文件字节
            byte[] bytes = multipartFile.getBytes();
            // 文件原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            // 文件扩展名
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            // 文件id
            //String fileId = storageClient1.upload_file1(bytes, extName, null);
            String file1 = storageClient1.upload_file1(bytes, extName, null);
            return file1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


}
