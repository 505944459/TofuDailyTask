package cn.ricetofu.task.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {
    //配置文件对象
    private static FileConfiguration fileConfiguration = null;

    //给玩家发送消息的前缀
    public static String header = "";
    //获得每日任务
    public static String get_task = "";
    //完成一个小任务的提示
    public static String task_finish_one = "";
    //完成全部任务的提示
    public static String task_finished = "";
    //获取奖励的提示
    public static String get_reward = "";
    //获取奖励失败时的提示，可能是由于背包空间不足或者离线
    public static String get_reward_fail = "";
    //未领取今日任务完成的奖励
    public static String could_get_reward = "";
    //外因导致的小任务强制更新(也可能是插件出错，大概率是因为任务配置文件近期被修改)
    public static String task_force_update = "";

    public static boolean load(File file){

        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        //加载消息配置文件
        header = fileConfiguration.getString("message-prefix");
        task_finish_one = fileConfiguration.getString("task-finish-one");
        task_finished = fileConfiguration.getString("task-finished");
        get_reward = fileConfiguration.getString("get-reward");
        get_reward_fail = fileConfiguration.getString("get-reward-fail");
        could_get_reward = fileConfiguration.getString("could-get-reward");
        task_force_update = fileConfiguration.getString("task-force-update");
        get_task =  fileConfiguration.getString("get-task");
        return true;
    }

}
