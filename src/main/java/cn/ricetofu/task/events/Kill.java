package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class Kill implements Listener {

    @EventHandler
    public void kill(EntityDeathEvent event){
        Player killer = event.getEntity().getKiller();
        //判断有无击杀者
        if(killer ==null)return;
        //判断有无任务
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(killer.getName());
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            //有一个未完成的击杀任务则继续
            if(!playerTask.isFinish&&playerTask.task_type.equals("kill")){
                //解析来自参数中的实体对象
                EntityType entityType = EntityType.fromName(playerTask.args.get(0));
                if(entityType==null)entityType = EntityType.fromId(Integer.valueOf(playerTask.args.get(0)));
                if(event.getEntity().getType().equals(entityType)){
                    List<String> args = playerTask.args;
                    args.add(2, (Integer.parseInt(args.remove(2)) + 1 )+"");//更新完成次数
                    //判断是否完成
                    int need = Integer.parseInt(args.get(1));
                    int finished = Integer.parseInt(args.get(2));
                    if(finished>=need){
                        //任务完成
                        TaskManager.finishOne(killer.getName(), playerTask.task_id);
                    }
                }
            }
        }
    }


}
