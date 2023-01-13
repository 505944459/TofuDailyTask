package cn.ricetofu.task;

import cn.ricetofu.task.core.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Scanner;
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



        //文件读取
        logger.log(Level.INFO,"正在读取config.yml文件");
        if (!ConfigManager.load(getConfig())) {
            logger.log(Level.SEVERE,"读取config.yml文件时出现了错误!!");
        }

    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO,"正在卸载tofu-task……");
        logger.log(Level.INFO,"卸载tofu-task成功");
    }
}
