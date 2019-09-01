package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @Autowired
    MediaFileRepository mediaFileRepository;

    @RabbitListener(queues = {"${xc-service-manage-media.mq.queue-media-video-processor}"},
            containerFactory="customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        // 解析消息内容，得到mediaId
        Map msgMap = JSON.parseObject(msg, Map.class);
        String mediaId = (String) msgMap.get("mediaId");
        // 用mediaId从数据库中获取信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent() ){
            return;
        }
        MediaFile mediaFile = optional.get();
        // 媒资文件类型
        String fileType = mediaFile.getFileType();
        if(fileType == null || !fileType.equals("avi")){
            mediaFile.setProcessStatus("303004"); // 处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            mediaFile.setProcessStatus("303001"); // 处理状态为处理中
            mediaFileRepository.save(mediaFile);
        }
        // 使用工具类将avi转成mp4
        // 转换文件路径
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        // 生成MP4文件名
        String mp4_name = mediaFile.getFileId() + ".mp4";
        // 生成mp4文件的目录
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        // 转换文件
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        String resultMp4 = videoUtil.generateMp4();
        if(resultMp4 == null || !resultMp4.equals("success")){
            // 操作失败写入处理日志
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(resultMp4);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 将mp4生成m3u8和ts文件
        // mp4文件路径
        String mp4video_path = serverPath + mediaFile.getFilePath() + mp4_name;
        // 生成m3u8文件名
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        // 生成m3u8目录
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
        // 转换文件
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4video_path,m3u8_name,m3u8folder_path);
        String resultM3u8 = hlsVideoUtil.generateM3u8();
        if(resultM3u8 == null || !resultM3u8.equals("success")){
            // 操作失败写入处理日志
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(resultM3u8);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 获取m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        // 更新处理状态为成功
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        // m3u8文件URL
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);

        mediaFileRepository.save(mediaFile);

    }
}
