package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-19
 * @Discription: 物品放置任务监听
 * */
public class Place implements Listener {

    @EventHandler
    public void blockPlace(BlockPlaceEvent event){
        String name = event.getPlayer().getName();
        //判断是否有这个任务
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(name);
        if(playerTasks!=null) for (PlayerTask playerTask : playerTasks) {
            if(!playerTask.isFinish&&playerTask.task_type.equals("place")){
                //判断物品类型是否相同
                if (event.getBlock().getType().equals(Material.matchMaterial(playerTask.args.get(0)))) {
                    //更新args中的完成数量
                    playerTask.args.add(2,(Integer.parseInt(playerTask.args.remove(2)) + 1)+"");
                    int finish = Integer.parseInt(playerTask.args.get(2));
                    int need = Integer.parseInt(playerTask.args.get(1));
                    if(finish>=need){
                        TaskManager.finishOne(name, playerTask.task_id);
                    }
                }
            }
        }
    }

}
