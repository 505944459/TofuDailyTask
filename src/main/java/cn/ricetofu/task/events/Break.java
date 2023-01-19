package cn.ricetofu.task.events;

import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;


/**
 * @Author: RiceTofu123
 * @DateL 2023-01-13
 * @Discription: 方块破坏监听及任务完成判断
 * */
public class Break implements Listener {

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        String name = event.getPlayer().getName();

        //判断这个玩家有没有方块破坏任务
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(name);
        if(playerTasks!=null)for(PlayerTask playerTask:playerTasks){
            if(playerTask.task_type.equals("break")&&!playerTask.isFinish){
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
