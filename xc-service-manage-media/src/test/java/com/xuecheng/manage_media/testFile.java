package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testFile {

    // 分块文件
    @Test
    public void testChunk() throws Exception {
        // 源文件
        File sourceFile = new File("H:\\DATA\\lucene.avi");
        // 分块文件路径
        String chunkPath = "H:\\DATA\\chunk1\\";
        // 分块大小
        long chunkSize = 1*1024*1024;
        // 分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        if(chunkNum <= 0){
            chunkNum = 1;
        }
        //缓冲区大小
        byte[] b = new byte[1024];
        // 使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");
        // 分块
        for(int i = 0; i < chunkNum; i++){
            File file = new File(chunkPath + i);

                // 像分块文件中写数据
            RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
            int len = -1;
            while ((len = raf_read.read(b)) != -1){
                raf_write.write(b, 0 , len);
                if(file.length()>chunkSize){
                    break;
                }
            }
            raf_write.close();

         }
        raf_read.close();
    }

    // 合并文件
    @Test
    public void testMergeFile() throws IOException {
        // 分块文件目录
        String chunkPath = "H:\\DATA\\chunk1\\";
        // 块文件目录对象
        File chunkFile = new File(chunkPath);
        // 块文件列表
        File[] files = chunkFile.listFiles();
        // 将块文件分许，按照升序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }

        });
        // 合并文件
        File mergeFile = new File("H:\\DATA\\lucene_merge.avi");
        // 创建新文件
        mergeFile.createNewFile();
        // 创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for (File file : fileList){
            // 创建一个读文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(file,"rw");
            int len = -1;
            while((len = raf_read.read(b)) != -1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("H:\\DATA\\chunk\\");
        //合并文件
        File mergeFile = new File("H:\\DATA\\lucene_merge.mp4");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();
        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //分块列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
        // 从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                } return 1;
            }
        });
        //合并文件
        for(File chunkFile:fileList){
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
