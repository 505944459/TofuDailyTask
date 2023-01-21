package cn.ricetofu.task;

import cn.ricetofu.task.core.ConfigManager;
import cn.ricetofu.task.core.MessageManager;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.core.gui.PlayerTaskGUI;
import cn.ricetofu.task.events.*;
import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 插件主类，插件的加载从这儿开始
 * */
public final class TofuDailyTask extends JavaPlugin {

    //日志记录类
    public static Logger logger = Bukkit.getLogger();

    //控制台输出头
    public static String console_header = "§a[§bTofuDailyTask§a]§f";

    //玩家数据文件夹
    public static File data_file;

    @Override
    public void onEnable() {
        data_file = new File(getDataFolder(),"playerdata");
        logger.log(Level.INFO,console_header+"欢迎使用Tofu-DailyTask 1.0.0");
        logger.log(Level.INFO,console_header+"正在加载Tofu-DailyTask……");

        //默认文件夹文件创建加载
        saveDefaultConfig();

        //config配置文件读取
        if (!ConfigManager.load(getConfig())) {
            logger.log(Level.SEVERE,console_header+"读取config.yml文件时出现了错误!!");
        }
        //mysql数据库的连接(如果配置了)
        if(ConfigManager.mysql){
            logger.log(Level.INFO,console_header+"检测到配置了mysql连接:正在尝试连接数据库……");
            if (MysqlManager.connect()) {
                logger.log(Level.INFO,console_header+"连接数据库成功!使用mysql端数据模式!");
            }else {
                logger.log(Level.SEVERE,console_header+"连接到数据库失败，默认使用本地数据。");
            }
        }

        //其他数据文件创建加载,tasks.yml,playerdata.yml,message.yml
        fileCreateAndLoad();


        //事件注册
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(),this); //玩家加入事件
        Bukkit.getPluginManager().registerEvents(new PlayerQuite(),this);//玩家退出事件
        Bukkit.getPluginManager().registerEvents(new PlayerTaskGUI(),this);//GUI相关事件
        //任务事件
        Bukkit.getPluginManager().registerEvents(new Break(),this);//物品/方块破坏
        Bukkit.getPluginManager().registerEvents(new Get(),this);//掉落物获取
        Bukkit.getPluginManager().registerEvents(new Place(),this);//放置东西
        Bukkit.getPluginManager().registerEvents(new Fish(),this);//钓鱼
        Bukkit.getPluginManager().registerEvents(new Kill(),this);//击杀
        Bukkit.getPluginManager().registerEvents(new Eat(),this);//食用
        Bukkit.getPluginManager().registerEvents(new Craft(),this);//合成
        Bukkit.getPluginManager().registerEvents(new Destroy(),this);//损坏
        Bukkit.getPluginManager().registerEvents(new Enchant(),this);//附魔

        //指令注册
        Bukkit.getPluginCommand("task").setExecutor(new PlayerTaskGUI());//task指令，打开一个箱子GUI

        //每日凌晨刷新定时任务
        Date today = null;
        try {
            today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())+" 00:41:00");
            today = new Date(today.getTime() + 1000*60*60*24);
        } catch (ParseException e) {
            logger.severe(console_header+"定时任务的时间解析出现了一个错误!");
        }
        //TODO 这里可能需要改为BUKKIT内置的多线程类，测试中用java自带的方式没有出现问题
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long l = System.currentTimeMillis();
                //保存现有的数据并清空
                logger.info(console_header+"开始凌晨刷新每日任务……");
                logger.severe(console_header+"正在保存在线玩家的任务数据:");

                //这里的任务保存也许也算是没有必要的
                if(!MysqlManager.enable)PlayerDataManager.saveLocal(data_file);
                else MysqlManager.save();

                TaskManager.player_tasks = new HashMap<>();//这个表的任务具有失效性，所以需要清空
                PlayerDataManager.playerDataMap = new HashMap<>();//这个顺便就一起咯
                logger.severe(console_header+"重新向在线玩家发放每日任务");
                //给在线的玩家领取新的每日任务
                Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                    if (TaskManager.getDailyTask(player.getName())) {
                        player.sendMessage(MessageManager.header+MessageManager.get_task);
                    }
                });
                logger.info(console_header+"每日任务刷新成功!耗时:"+(System.currentTimeMillis()-l)+"毫秒");
            }
        };
        new Timer().schedule(task,today.getTime() - new Date().getTime() + 1000*2,1000*60*60*24);//每日凌晨刷新，间隔24h

        logger.log(Level.INFO,console_header+"插件加载成功啦~~~~");
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO,console_header+"正在保存玩家数据");

        //保存插件暂存的所有数据
        if(!MysqlManager.enable)PlayerDataManager.saveLocal(new File(getDataFolder(),"playerdata"));
        else MysqlManager.save();

        logger.log(Level.INFO,console_header+"卸载Tofu-DailyTask成功");
    }

    private void fileCreateAndLoad(){
        //任务文件,不存在先创建，然后给Manager加载
        File tasks_file = new File(getDataFolder(),"tasks.yml");
        if(!tasks_file.exists()){
            InputStream in = getResource("tasks.yml");
            OutputStream out = null;
            try {
                out = new FileOutputStream(tasks_file);
                int n;
                byte[] buf = new byte[1024*10];
                while ((n = in.read(buf))>0){
                    out.write(buf,0,n);
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE,console_header+"创建tasks.yml文件时出现了错误!");
            } catch (IOException e) {
                logger.log(Level.SEVERE,console_header+"创建tasks.yml文件时出现了错误!");
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE,console_header+"创建task.yml文件时出现了错误!");
                }
            }
        }
        TaskManager.loadTasks(tasks_file);//加载任务文件

        //消息提示配置文件
        File message_file = new File(getDataFolder(),"message.yml");
        if(!message_file.exists()){
            InputStream in = getResource("message.yml");
            OutputStream out = null;
            try {
                out = new FileOutputStream(message_file);
                int n;
                byte[] buf = new byte[1024*10];
                while ((n = in.read(buf))>0){
                    out.write(buf,0,n);
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE,console_header+"创建message.yml文件时出现了错误!");
            } catch (IOException e) {
                logger.log(Level.SEVERE,console_header+"创建message.yml文件时出现了错误!");
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE,console_header+"创建message.yml文件时出现了错误!");
                }
            }
        }
        MessageManager.load(message_file);//加载消息配置

        //帮助文档输出
        File help_file = new File(getDataFolder(),"help.md");
        if(!help_file.exists()){
            InputStream in = getResource("help.md");
            OutputStream out = null;
            try {
                out = new FileOutputStream(help_file);
                int n;
                byte[] buf = new byte[1024*10];
                while ((n = in.read(buf))>0){
                    out.write(buf,0,n);
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE,console_header+"创建help.md文件时出现了错误!");
            } catch (IOException e) {
                logger.log(Level.SEVERE,console_header+"创建help.md文件时出现了错误!");
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE,console_header+"创建help.md文件时出现了错误!");
                }
            }
        }

        //玩家信息本地存储文件夹,没有则新建一个
        File data_file = new File(getDataFolder(),"playerdata");
        if(!data_file.exists())data_file.mkdir();

    }
}
