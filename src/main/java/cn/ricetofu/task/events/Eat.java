package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @DateL 2023-01-21
 * @Discription: 吃东西的事件监听
 * */
public class Eat implements Listener {

    @EventHandler
    public void eat(PlayerItemConsumeEvent event){
        String name = event.getPlayer().getName();
        //判断是否有任务
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(name);
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("eat")){
                //如果有一个未完成的吃东西任务，判断物品类型，更新完成数量
                List<String> args = playerTask.args;
                Material type = event.getItem().getType();
                if(!type.equals(Material.matchMaterial(args.get(0))))return;//类型不匹配，返回
                args.add(2,(Integer.parseInt(args.remove(2))+1)+"");
                if(Integer.parseInt(args.get(2))>=Integer.parseInt(args.get(1)))TaskManager.finishOne(name,playerTask.task_id);
            }
        }
    }

}
