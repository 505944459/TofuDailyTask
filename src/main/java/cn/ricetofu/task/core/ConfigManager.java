package cn.ricetofu.task.core;

import org.bukkit.configuration.file.FileConfiguration;

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
