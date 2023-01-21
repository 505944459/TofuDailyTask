package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @DateL 2023-01-21
 * @Discription: 物品合成的监听
 * */
public class Craft implements Listener {


    @EventHandler
    public void craft(CraftItemEvent event){
        HumanEntity whoClicked = event.getWhoClicked();
        if(!(whoClicked instanceof Player))return;
        Player player = (Player) whoClicked;
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(player.getName());
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("craft")){
                Material type = event.getCurrentItem().getType();
                List<String> args = playerTask.args;
                if(!type.equals(Material.matchMaterial(args.get(0))))return;//物品不匹配
                args.add(2,(Integer.parseInt(args.remove(2))+1)+"");//完成数量更新
                if(Integer.parseInt(args.get(2))>=Integer.parseInt(args.get(1)))TaskManager.finishOne(player.getName(),playerTask.task_id);
            }
        }

    }

}
