package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @DateL 2023-01-21
 * @Discription: 物品损坏监听
 * */
public class Destroy implements Listener {

    @EventHandler
    public void destroy(PlayerItemBreakEvent event){
        Player player = event.getPlayer();
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(player.getName());
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("destroy")){
                List<String> args = playerTask.args;
                Material type = event.getBrokenItem().getType();
                if(!type.equals(Material.matchMaterial(args.get(0))))return;//物品不匹配
                args.add(2,(Integer.parseInt(args.get(2))+1)+"");
                if(Integer.parseInt(args.get(2))>=Integer.parseInt(args.get(1)))TaskManager.finishOne(player.getName(), playerTask.task_id);
            }
        }
    }

}
