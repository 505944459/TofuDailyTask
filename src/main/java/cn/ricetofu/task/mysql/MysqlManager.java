package cn.ricetofu.task.mysql;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 与数据库操作相关的类
 * */
public class MysqlManager {

    //是否启用了数据库,如果连接失败的话也会为false
    public static boolean enable = false;
    //数据库连接对象，如果没有配置或者连接失败会为null
    public static Connection connection = null;
    //日志记录对象
    private static Logger logger = Bukkit.getLogger();

    /**
     * 连接数据库的方法
     * */
    public static boolean connect(){
        //驱动加载
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,"加载mysql驱动时出现异常？找不到驱动类？");
            return false;
        }
        //连接获取


        //连接获取
        return false;
    }


}
