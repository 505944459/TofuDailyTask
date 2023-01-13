package cn.ricetofu.task.core;

import cn.ricetofu.task.mysql.MysqlManager;
import cn.ricetofu.task.pojo.Task;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 与数据交互相关的类
 * */
public class DataManager {

    //任务列表
    private static Map<String, List<Task>> taskMap = new HashMap<>();

    /**
     * 加载配置文件
     * */
    private static void loadData(){
        if(MysqlManager.enable)syncDataFromMysql();
        else syncLocalData();
    }

    /**
     * 从mysql中同步任务数据
     * @return 是否成功
     * */
    private static boolean syncDataFromMysql(){

        return false;
    }

    /**
     * 从本地文件中同步任务数据
     * @return 是否成功
     * */
    private static boolean syncLocalData(){
        File file = new File("./tofu-task/task");

        //读取任务文件夹下所有的文件,加载进任务类别之中
        File[] files = file.listFiles();
        for (File f : files) {



        }

        return false;
    }

}
