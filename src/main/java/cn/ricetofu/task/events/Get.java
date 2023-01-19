package cn.ricetofu.task.events;


import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-17
 * @Discription: 物品获取任务监听
 * */
public class Get implements Listener {

    @EventHandler
    public void getSomething(EntityPickupItemEvent event){
        //不是玩家
        LivingEntity entity = event.getEntity();
        if(! (entity instanceof Player))return;
        Player player = (Player) entity;
        String name = player.getName();
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(name);
        //判断这个玩家有没有获取任务
        if(playerTasks!=null)for(PlayerTask playerTask:playerTasks){
            if(playerTask.task_type.equals("get")&&!playerTask.isFinish){
                //判断是不是掉落物品
                if (event.getItem().getType().equals(EntityType.DROPPED_ITEM)&&event.getItem().getItemStack().getType().equals(Material.matchMaterial(playerTask.args.get(0)))) {
                    //更新args中的完成数量
                    playerTask.args.add(2,(Integer.valueOf(playerTask.args.remove(2)) + event.getItem().getItemStack().getAmount())+"");
                    Integer finish = Integer.valueOf(playerTask.args.get(2));
                    Integer need = Integer.valueOf(playerTask.args.get(1));
                    if(finish>=need){
                        TaskManager.finishOne(name, playerTask.task_id);
                    }
                }
            }
        }

    }

}
