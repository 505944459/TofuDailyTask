package cn.ricetofu.task.core;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.mysql.MysqlManager;
import cn.ricetofu.task.pojo.SavedPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-20
 * @Discription: 管理奖励获取和进行奖励操作的类
 * */
public class RewardManager {

    /**
     * 判断玩家今天有没有领取过奖励
     * @param player_id 玩家id
     * @return 是否领取过，如果不存在这个玩家（玩家没来过服务器）则也会返回true
     * */
    public static boolean isRewardToday(String player_id){
        SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
        if(savedPlayerData==null)return true;//按理来说不应该出现这个情况，除非这个玩家一次也没来过本服务器
        else return  savedPlayerData.last_reward_date.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));//比较时间字符串来判断
    }

    /**
     * 给一个指定玩家每日任务的完成奖励
     * @param player_id 玩家id
     * @return 是否成功
     * */
    public static boolean givePlayerDailyTaskReward(String player_id){
        //玩家是否在线
        Player player = Bukkit.getPlayer(player_id);
        if(player==null||!player.isOnline())return false;
        //获取每日奖励内容
        List<String> rewards = ConfigManager.rewards;
        //解析奖励内容并开始发放
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        Server server = Bukkit.getServer();

        for (String reward : rewards) {
            reward = reward.replaceAll("%player%", player_id);
            String[] s = reward.split(" ");

            if(s[0].equals("give")&&s.length==4){//GIVE指令解析
                reward = s[0] +" "+ s[1]+" "+Material.matchMaterial(s[2]).name()+" " + s[3];
            }

            if (!server.dispatchCommand(consoleSender,reward)) {
                Bukkit.getLogger().severe(TofuDailyTask.console_header+"服务器执行:"+reward+"指令时出现了异常,指令真的正确吗?");
            }
        }
        player.sendMessage(MessageManager.header+MessageManager.get_reward);
        for (String s : ConfigManager.rewards_lore) {
            player.sendMessage(MessageManager.header+"§a"+s);
        }
        //数据保存
        SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
        savedPlayerData.last_reward_date = PlayerDataManager.sdf.format(new Date());
        //立即保存到本地/数据库一次，防止重复领取奖励的可能
        if(MysqlManager.enable)MysqlManager.saveOne(player_id);
        else PlayerDataManager.saveOneLocal(TofuDailyTask.data_file,player_id);

        return true;
    }

    /**
     * 给一个指定玩家指定每日任务的完成奖励（如果这个任务存在单独奖励的话）
     * @param player_id 玩家id
     * @param task_id 任务id
     * @return 是否成功
     * */
    public static boolean givePlayerTaskReward(String player_id,String task_id){
        //TODO 暂未支持的功能

        return false;
    }

}
