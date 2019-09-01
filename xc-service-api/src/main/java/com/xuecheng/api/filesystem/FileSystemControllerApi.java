package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件管理接口", description = "文件管理接口，提供文件的上传和下载")
public interface FileSystemControllerApi {

    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata);

}
