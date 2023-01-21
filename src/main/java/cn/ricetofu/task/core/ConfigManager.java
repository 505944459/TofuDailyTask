package cn.ricetofu.task.core;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 与配置读取相关的类
 * */
public class ConfigManager {

    //配置文件对象
    private static FileConfiguration fileConfiguration = null;
    //插件版本
    public static String version = null;
    //每日任务的小任务个数
    public static Integer small_tasks = 0;
    //每日任务可以刷新的次数
    public static Integer change_times = 0;
    //每日任务完成过后的奖励列表
    public static List<String> rewards = null;
    //每日任务的展示信息
    public static List<String> rewards_lore = null;
    //是否在任务完成后自动领取每日任务奖励
    public static boolean auto_reward = false;
    //是否开启mysql
    public static boolean mysql = false;
    //mysql配置
    public static String mysql_host = null;//地址
    public static String mysql_database = null;//数据库名称
    public static String mysql_username = null;//用户名
    public static String mysql_password = null;//密码

    public static boolean load(FileConfiguration configuration){
        fileConfiguration = configuration;
        //加载config配置文件
        version = fileConfiguration.getString("version");
        small_tasks = fileConfiguration.getInt("tasks");
        change_times = fileConfiguration.getInt("change-times");
        rewards = fileConfiguration.getStringList("rewards");
        rewards_lore = fileConfiguration.getStringList("rewards-lore");
        auto_reward = fileConfiguration.getBoolean("auto-rewards");

        mysql = fileConfiguration.getBoolean("mysql");
        if(mysql){
            //加载mysql配置
            mysql_host = fileConfiguration.getString("mysql-host");
            mysql_database = fileConfiguration.getString("mysql-database");
            mysql_username = fileConfiguration.getString("mysql-username");
            mysql_password = fileConfiguration.getString("mysql-password");
        }
        return true;
    }

}
