package cn.ricetofu.task.events;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.core.MessageManager;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 玩家登陆事件监听
 * */
public class PlayerJoin implements Listener {

    @EventHandler
    public void loginEvent(PlayerJoinEvent event){
        Player player = event.getPlayer();

        //如果开启了数据库支持的话，要从数据库同步该玩家的任务完成的数据
        if(!MysqlManager.enable)PlayerDataManager.loadOneLocal(TofuDailyTask.data_file,player.getName());//加载一个玩家的配置文件
        else {}//TODO


        //是否接取每日任务
        if (TaskManager.getDailyTask(player.getName())) {
            player.sendMessage("又是新的一天啦~~~已经为你自动领取每日任务咯~赶快去完成吧!");
        }


    }

}
