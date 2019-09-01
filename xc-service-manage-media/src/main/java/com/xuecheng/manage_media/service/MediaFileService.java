package com.xuecheng.manage_media.service;


import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        // 查询条件
        MediaFile mediaFile = new MediaFile();
        if(queryMediaFileRequest == null ){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        // 查询条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                                    .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains())  // tag字段模糊查询
                                    .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains()) // 文件原始名模糊查询
                                    .withMatcher("processStatus",ExampleMatcher.GenericPropertyMatchers.exact()); // 处理状态精确匹配（默认）
        // 查询条件
        if(!StringUtils.isEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if(!StringUtils.isEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if(!StringUtils.isEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        // 定义example实例
        Example<MediaFile> example = Example.of(mediaFile, matcher);

        if(page <= 0){
            page = 1;
        }
        page = page - 1;
        if(size <= 0){
            size = 5;
        }
        // 分页查询
        Pageable pageable = new PageRequest(page, size);

        Page<MediaFile> all = mediaFileRepository.findAll(example, pageable);

        List<MediaFile> fileList = all.getContent();
        long totalElements = all.getTotalElements();

        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();

        mediaFileQueryResult.setList(fileList);
        mediaFileQueryResult.setTotal(totalElements);

        return new QueryResponseResult(CommonCode.SUCCESS, mediaFileQueryResult);
    }
}
