package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @DateL 2023-01-21
 * @Discription: 附魔事件监听
 * */
public class Enchant implements Listener {

    @EventHandler
    public void enchant(EnchantItemEvent event){
        String player_id = event.getEnchanter().getName();
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(player_id);

        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("enchant")){
                List<String> args = playerTask.args;
                args.add(1,(Integer.parseInt(args.get(1))+1)+"");
                if(Integer.parseInt(args.get(1))>=Integer.parseInt(args.get(0)))TaskManager.finishOne(player_id, playerTask.task_id);
            }
        }


    }

}
