package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    // 接收选课响应结果
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if(xcTask != null && !StringUtils.isEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }
    }

    @Scheduled(cron = "0/3 * * * * *")  // 每隔三秒执行一次
    public void sendChoosecourseTask(){
        // 取出当前一分钟之前得时间
        Calendar calendar = new GregorianCalendar();
        try {
            SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            calendar.setTime(sp.parse(sp.format(new Date())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 100);
        System.out.println(taskList);
        // 调用service方法，将添加选课得任务发送给mq
        for(XcTask xcTask : taskList){
            //调用乐观锁方法校验任务是否可以执行
            if(taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0){
                String exchange = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,exchange,routingkey);
            }
        }

    }
    // @Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
    // @Scheduled(fixedDelay = 5000) //上次执行完毕后5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次
    /*@Scheduled(cron = "0/3 * * * * *")  // 每隔三秒执行一次
    public void task1(){

        LOGGER.info("===============测试定时任务1开始===============");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("===============测试定时任务1结束===============");

    }

    @Scheduled(cron = "0/3 * * * * *")  // 每隔三秒执行一次
    public void task2(){

        LOGGER.info("===============测试定时任务2开始===============");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("===============测试定时任务2结束===============");

    }*/

}
