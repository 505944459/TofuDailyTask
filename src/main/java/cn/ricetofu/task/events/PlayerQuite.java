package cn.ricetofu.task.events;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @Author: RiceTofu123
 *
 * */
public class PlayerQuite implements Listener {

    @EventHandler
    public void playerExit(PlayerQuitEvent event){
        //保存到本地或数据库
        if(!MysqlManager.enable)PlayerDataManager.saveOneLocal(TofuDailyTask.data_file,event.getPlayer().getName());
        else MysqlManager.saveOne(event.getPlayer().getName());
        //删除内存中的数据
        TaskManager.player_tasks.remove(event.getPlayer().getName());
        PlayerDataManager.playerDataMap.remove(event.getPlayer().getName());

    }
}
