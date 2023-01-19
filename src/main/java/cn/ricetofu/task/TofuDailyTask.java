package cn.ricetofu.task;

import cn.ricetofu.task.core.ConfigManager;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.events.*;
import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.Files;
import java.sql.Time;
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
    private static Logger logger = Bukkit.getLogger();

    //玩家数据文件夹
    public static File data_file;

    @Override
    public void onEnable() {
        data_file = new File(getDataFolder(),"playerdata");
        logger.log(Level.INFO,"欢迎使用Tofu-DailyTask 1.0.0");
        logger.log(Level.INFO,"正在加载Tofu-DailyTask……");

        //默认文件夹文件创建加载
        saveDefaultConfig();

        //config配置文件读取
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

        //其他数据文件创建加载,tasks.yml,playerdata.yml,message.yml
        fileCreateAndLoad();


        //事件注册
        logger.log(Level.INFO,"注册事件中……");
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(),this); //玩家登陆事件
        //任务事件
        Bukkit.getPluginManager().registerEvents(new Break(),this);//物品/方块破坏
        Bukkit.getPluginManager().registerEvents(new Get(),this);//掉落物获取
        Bukkit.getPluginManager().registerEvents(new Place(),this);//放置东西
        Bukkit.getPluginManager().registerEvents(new Fish(),this);//钓鱼
        Bukkit.getPluginManager().registerEvents(new Kill(),this);//击杀


        //指令注册


        //每日凌晨刷新定时任务
        Date today = null;
        try {
            today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())+" 00:00:00");
            today = new Date(today.getTime() + 1000*60*60*24);
        } catch (ParseException e) {
            logger.severe("定时任务的时间解析出现了一个错误!");
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //保存现有的数据并清空
                logger.info("定时任务执行了!");
                if(!MysqlManager.enable)PlayerDataManager.saveLocal(data_file);
                else {}//TODO 数据库保存

                TaskManager.player_tasks = new HashMap<>();//这个表的任务具有失效性，所以需要清空

                //给在线的玩家领取新的每日任务
                Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                    TaskManager.getDailyTask(player.getName());
                });

            }
        };
        new Timer().schedule(task,today.getTime() -new Date().getTime() + 1000*5 ,1000*60*60*24);
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO,"正在保存玩家数据");
        if(!MysqlManager.enable)PlayerDataManager.saveLocal(new File(getDataFolder(),"playerdata"));
        logger.log(Level.INFO,"卸载Tofu-DailyTask成功");
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
                logger.log(Level.SEVERE,"创建tasks.yml文件时出现了错误!");
            } catch (IOException e) {
                logger.log(Level.SEVERE,"创建tasks.yml文件时出现了错误!");
            } finally {
                try {
                    out.close();
                    in.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE,"创建task.yml文件时出现了错误!");
                }
            }
        }
        TaskManager.loadTasks(tasks_file);//加载任务文件
        //消息文件


        //玩家信息文件夹,没有则新建一个
        File data_file = new File(getDataFolder(),"playerdata");
        if(!data_file.exists())data_file.mkdir();

        /*if(!MysqlManager.enable)PlayerDataManager.loadLocal(data_file);//本地加载玩家数据
        else {}//mysql加载*/
    }
}
