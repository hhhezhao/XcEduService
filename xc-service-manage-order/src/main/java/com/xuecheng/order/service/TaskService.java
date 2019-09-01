package com.xuecheng.order.service;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    public List<XcTask> findTaskList(Date updateTime, int size){
        // 设置分页参数
        Pageable pageable = new PageRequest(0, size);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks;
    }

    // 发布消息
    @Transactional
    public void publish(XcTask xcTask, String ex, String routingKey){
        // 查询任务
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(xcTask.getId());
        if(optionalXcTask.isPresent()){
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            XcTask one = optionalXcTask.get();
            try {
                SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                one.setUpdateTime(sp.parse(sp.format(new Date())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            xcTaskRepository.save(one);
        }

    }

    // 获取任务
    @Transactional
    public int getTask(String taskId, int version){
        int count = xcTaskRepository.updateTaskVersion(taskId, version);
        return count;
    }

    // 删除任务
    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if(optional.isPresent()){
            XcTask xcTask = optional.get();
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }

}
