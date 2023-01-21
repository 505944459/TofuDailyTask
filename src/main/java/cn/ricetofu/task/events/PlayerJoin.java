package cn.ricetofu.task.events;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.core.MessageManager;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.mysql.MysqlManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

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
        else MysqlManager.loadOne(player.getName());


        //玩家现在拥有的任务校验，可能会存在玩家拥有一个未成功加载或不存在的任务的情况
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(player.getName());
        if(playerTasks!=null) for (int i = 0; i < playerTasks.size(); i++) {
            PlayerTask playerTask = playerTasks.get(i);

            if(!TaskManager.all_tasks.containsKey(playerTask.task_id)){//任务表中不存在这个任务
                TofuDailyTask.logger.warning(TofuDailyTask.console_header+"玩家:"+player.getName()+"领取过了一个不存在的任务,id:"+playerTask.task_id);
                TofuDailyTask.logger.warning(TofuDailyTask.console_header+"如果这个是最近更改这个任务导致的，请忽略，否则请联系技术人员寻求帮助");

                PlayerDataManager.playerDataMap.get(player.getName()).list.remove(playerTasks.remove(i));//移除这两个位置的任务信息
                i--;

                //这个任务未完成则需要强制刷新了
                if(!playerTask.isFinish){
                    player.sendMessage(MessageManager.header+MessageManager.task_force_update);//给玩家发送提醒消息
                    List<PlayerTask> randomTasksByWeight = TaskManager.getRandomTasksByWeight(playerTasks, 1);
                    if(randomTasksByWeight!=null&&randomTasksByWeight.size()==1){
                        PlayerDataManager.playerDataMap.get(player.getName()).list.add(randomTasksByWeight.get(0));
                        TaskManager.player_tasks.get(player.getName()).add(randomTasksByWeight.get(0));
                    }
                }
            }
        }

        //是否接取每日任务
        if (TaskManager.getDailyTask(player.getName())) {
            player.sendMessage(MessageManager.header+MessageManager.get_task);
        }


    }

}
