package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-19
 * @Discription: 钓鱼任务事件
 * */
public class Fish implements Listener {

    @EventHandler
    public void fish(PlayerFishEvent event){
        Entity caught = event.getCaught();
        if(caught==null||!caught.getType().equals(EntityType.DROPPED_ITEM))return;//没有掉到东西，或者掉到的不是一个掉落物
        String name = event.getPlayer().getName();
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(name);
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("fish")){
                List<String> args = playerTask.args;
                args.add(1,(Integer.valueOf(args.remove(1))+1) +"");//更新完成次数
                if(Integer.parseInt(args.get(0))<=Integer.parseInt(args.get(1))){
                    TaskManager.finishOne(name,playerTask.task_id);
                }
            }
        }
    }

}
