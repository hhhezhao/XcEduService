package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    @Test
    public void testUpload(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);
            //创建客户端
            TrackerClient tc = new TrackerClient();
            //连接tracker Server
            TrackerServer ts = tc.getConnection();
            if (ts == null) {
                System.out.println("getConnection return null");
                return;
            }
            //获取一个storage server
            StorageServer ss = tc.getStoreStorage(ts);
            if (ss == null) {
                System.out.println("getStoreStorage return null");
            }
            //创建一个storage存储客户端
            StorageClient1 sc1 = new StorageClient1(ts, ss);
            //NameValuePair[] meta_list = null; //new NameValuePair[0];
            String item = "d:/001.jpg";
            String fileId = sc1.upload_file1(item, "png", null);
            // group1/M00/00/00/wKhlA10sUiaAGnK7AAJNJkFZfog018.png
            System.out.println("Upload local file " + item + " ok, fileid=" + fileId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testDownLoad(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1  storageClient1  =    new StorageClient1(trackerServer, storageServer);
            byte[] result = storageClient1.download_file1("group1/M00/00/00/wKhlA10sUiaAGnK7AAJNJkFZfog018.png");
            File file = new File("d:/1.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(result);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    //查询文件
    @Test
    public void testQueryFile(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer,storageServer);
            FileInfo fileInfo = storageClient.query_file_info("group1", "M00/00/00/wKhlA10sUiaAGnK7AAJNJkFZfog018.png");
            System.out.println(fileInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }


}
