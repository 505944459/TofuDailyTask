package cn.ricetofu.task.mysql;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.core.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
     * @return 连接是否成功
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
        String url = "jdbc:mysql://"+ ConfigManager.mysql_host+"/"+ConfigManager.mysql_database+"?useSSL=false";
        String username = ConfigManager.mysql_username;
        String password = ConfigManager.mysql_password;
        try{
            connection = DriverManager.getConnection(url,username,password);
        }catch (SQLException e){
            logger.log(Level.SEVERE,"获取数据库连接时出现了异常？配置的数据库信息有误？？");
            return false;
        }

        //表创建

        //保持Mysql连接的定时任务
        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    connection.createStatement().execute("SELECT 1");
                } catch (SQLException e) {
                    cancel();//取消这个任务
                    logger.log(Level.WARNING,"数据库连接貌似断掉了捏~~正在尝试重连……");
                    if(connect()){
                        logger.log(Level.INFO,"数据库重连成功!");
                    }else {
                        logger.log(Level.SEVERE,"数据库重连失败!!!请检查，为确保运作，已经卸载本插件!!");
                        //插件卸载
                        Bukkit.getPluginManager().disablePlugin(TofuDailyTask.getProvidingPlugin(TofuDailyTask.class));
                    }
                }
            }
        }.runTaskTimerAsynchronously(TofuDailyTask.getProvidingPlugin(TofuDailyTask.class),60*20,60*20);
        //连接成功
        enable = true;
        return true;
    }


}
