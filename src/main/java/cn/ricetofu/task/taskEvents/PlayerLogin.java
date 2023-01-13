package cn.ricetofu.task.taskEvents;

import cn.ricetofu.task.mysql.MysqlManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 玩家登陆事件监听
 * */
public class PlayerLogin implements Listener {

    @EventHandler
    public void loginEvent(PlayerLoginEvent event){

        Player player = event.getPlayer();
        //如果开启了数据库支持的话，要从数据库同步该玩家的任务数据
        if(MysqlManager.enable){
            player.sendMessage("正在从数据库同步你的任务数据……");

            player.sendMessage("数据同步成功!");
        }

        //自动接取任务




    }

}
