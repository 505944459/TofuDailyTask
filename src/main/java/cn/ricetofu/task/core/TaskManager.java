package cn.ricetofu.task.core;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.mysql.MysqlManager;
import cn.ricetofu.task.pojo.PlayerTask;
import cn.ricetofu.task.pojo.SavedPlayerData;
import cn.ricetofu.task.pojo.Task;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-16
 * @Discription: 与任务操作相关的类
 * */
public class TaskManager {

    //所有任务的列表
    private static Map<String,Task> all_tasks = new HashMap<>();

    //玩家以及玩家的每日任务map,这个表具有时效性，只会存储当天的玩家每日任务!
    public static Map<String, List<PlayerTask>> player_tasks = new HashMap<>();

    //日志记录对象
    private static Logger logger = Bukkit.getLogger();

    /**
     * 加载配置文件中的任务
     * */
    public static boolean loadTasks(File file){
        YamlConfiguration tasks = YamlConfiguration.loadConfiguration(file);
        //得到键集，也就是各个子任务的id啦
        Set<String> keys = tasks.getKeys(false);
        //任务加载
        for (String key : keys) {
            Task task = new Task();
            task.id = key;
            MemorySection section =(MemorySection) tasks.get(key);
            task.name = section.getString("name").replace('&','§');//替换特殊符号
            task.display = section.getString("display");
            task.weight = section.getInt("weight");
            List<String> lore = section.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                lore.add(i,lore.remove(i).replace('&','§'));
            }
            task.lore = lore;
            task.type = section.getString("type");
            if(section.contains("rewards"))task.rewards = section.getStringList("rewards");
            all_tasks.put(key,task);
        }
        //任务配置正确性校验
        taskCheck();
        logger.info("tasks.yml加载完成!!");
        return true;
    }

    /**
     * 完成一个任务过后调用的方法,在这里进行所有任务是否都完成的判断
     * @param player_id 玩家的id
     * @param id 任务的id
     * */
    public static void finishOne(String player_id,String id){
        List<PlayerTask> playerTasks = player_tasks.get(player_id);
        boolean all_finish = true;
        for (PlayerTask playerTask : playerTasks) {
            if(playerTask.task_id.equals(id)){
                playerTask.isFinish = true;
                Bukkit.getPlayer(player_id).sendMessage("§a[§b每日任务§a]§f你完成了一个每日任务: " +all_tasks.get(id).name);
            }
            if(!playerTask.isFinish)all_finish = false;
        }
        if(all_finish){
            //发送消息提示玩家
            Bukkit.getPlayer(player_id).sendMessage("§a[§b每日任务§a]§f你已经完成了所有的每日任务~~~");
            //给玩家完成奖励
            if (!givePlayerDailyTaskReward(player_id)) {
                //如果没有成功给他，则证明玩家离线或者背包空间不足，可以下次直接使用命令手动获取
                Bukkit.getPlayer(player_id).sendMessage("§a[§b每日任务§a]§f似乎没有自动成功领取奖励？背包空间不足？");
                Bukkit.getPlayer(player_id).sendMessage("§a[§b每日任务§a]§f请清空背包使用/task reward 领取哦~");
            }
            //给玩家增加累计完成次数
            SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
            savedPlayerData.finished_times = savedPlayerData.finished_times +1;
            //立即保存一次
            if(!MysqlManager.enable)PlayerDataManager.saveOneLocal(TofuDailyTask.data_file,player_id);//本地保存
            else {}//mysql保存
        }
    }

    /**
     * 给一个指定玩家每日任务的完成奖励
     * */
    private static boolean givePlayerDailyTaskReward(String player_id){


        return false;
    }

    /**
     * 检查目前插件已经加载的任务，主要是看参数有没有配置正确等……
     * */
    private static void taskCheck(){

    }

    /**
     * 接取每日任务，如果已经接取了则不会二次接取
     * @param player_id 玩家id
     * @return 是否成功接取
     * */
    public static boolean getDailyTask(String player_id){
        if(!player_tasks.containsKey(player_id)){
            //接取每日任务,这里需要写一个随机算法


            List<PlayerTask> playerTasks = new ArrayList<>();
            for (Task task : all_tasks.values()) {
                playerTasks.add(new PlayerTask(task));
            }
            player_tasks.put(player_id,playerTasks);


            //如果玩家是第一次进来，那么PlayerDataManager里的玩家信息表就没有这个玩家，需要新建一个
            SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
            if (savedPlayerData==null) {
                savedPlayerData = new SavedPlayerData();
                savedPlayerData.finished_times = 0;
                PlayerDataManager.playerDataMap.put(player_id,savedPlayerData);
            }
            savedPlayerData.list = playerTasks;
            savedPlayerData.last_receive_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());//更新收到任务的时间
            return true;
        }

        return false;
    }

}
