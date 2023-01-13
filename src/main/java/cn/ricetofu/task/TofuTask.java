package cn.ricetofu.task;

import cn.ricetofu.task.core.ConfigManager;
import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 插件主类，插件的加载从这儿开始
 * */
public final class TofuTask extends JavaPlugin {

    //日志记录类
    private static Logger logger = Bukkit.getLogger();

    @Override
    public void onEnable() {
        logger.log(Level.INFO,"欢迎使用tofu-task 1.0.0");
        logger.log(Level.INFO,"正在加载tofu-task……");
        //默认文件夹文件创建
        saveDefaultConfig();
        File file = new File("./TofuTask");


        //配置文件读取
        logger.log(Level.INFO,"正在读取config.yml文件");
        if (!ConfigManager.load(getConfig())) {
            logger.log(Level.SEVERE,"读取config.yml文件时出现了错误!!");
        }
        //mysql数据库的连接(如果配置了)
        if(ConfigManager.mysql){
            logger.log(Level.INFO,"检测到配置了mysql连接:正在尝试连接数据库……");
            if (MysqlManager.connect()) {
                logger.log(Level.INFO,"连接数据库成功!使用mysql端数据模式!");
            }else {
                logger.log(Level.SEVERE,"连接到数据库失败，默认使用本地数据。");
            }
        }

        //任务配置文件读取，如果mysql中没有表数据，默认进行数据同步




    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO,"正在卸载tofu-task……");
        logger.log(Level.INFO,"卸载tofu-task成功");
    }
}
