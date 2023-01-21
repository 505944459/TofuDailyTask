package cn.ricetofu.task.core.gui;

import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import cn.ricetofu.task.pojo.SavedPlayerData;
import cn.ricetofu.task.pojo.Task;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Skull;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-18
 * @Discription: 玩家使用task命令后打开的菜单
 * */
public class PlayerTaskGUI implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) { //发送者是一个玩家
            //打开一个箱子GUI
            openTaskGui((Player) sender);
        }
        return false;
    }

    public void openTaskGui(Player player){
        //初始化一个箱子
        Inventory inv = Bukkit.createInventory(player,6*9,"§a[§b每日任务§a]§f");
        //开始绘制
        new Skull(SkullType.PLAYER.ordinal());


        ItemStack head = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
        SkullMeta headMeta = (SkullMeta)head.getItemMeta();
        headMeta.setOwningPlayer(player.getPlayer());
        //玩家信息
        SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player.getName());
        headMeta.setDisplayName("§f"+player.getName()+":");
        List<String> lore = new ArrayList<>();
        lore.add("§a-------------------");
        lore.add("§f上次完成时间:§a"+(savedPlayerData.last_finished_date.equals("")?"无记录":savedPlayerData.last_finished_date));
        lore.add("§f累计完成次数:§a"+savedPlayerData.finished_times);
        headMeta.setLore(lore);
        head.setItemMeta(headMeta);//玩家头颅

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 0,(byte)3);//淡蓝色的玻璃板
        ItemMeta itemMeta = glass.getItemMeta();
        itemMeta.setDisplayName("§f边界线");
        glass.setItemMeta(itemMeta);


        inv.setItem(0,head);//放置头颅

        //放置边界
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                if(i==0&&j==0)continue;
                if(i==0||i==5||j==0||j==8){
                    //边界方块
                    inv.setItem(i*9+j,glass);
                }

            }
        }


        //放置任务
        List<PlayerTask> playerTasks = TaskManager.player_tasks.get(player.getName());
        int i = 2;
        int j = 2;
        for (PlayerTask playerTask : playerTasks) {
            Task task = TaskManager.all_tasks.get(playerTask.task_id);
            ItemStack itemStack = new ItemStack(Material.matchMaterial(task.display));//显示的物品

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName("§f"+task.name);//设置名字

            lore =new ArrayList<>();
            lore.add(0,"§a-------------------");
            for (String s : task.lore) {
                lore.add("§f"+loreAnalysis(playerTask, s));//lore解析
            }
            lore.add("§a-------------------");
            String finish = playerTask.isFinish?"§a已完成":"§c未完成";
            lore.add("完成情况:"+finish);
            meta.setLore(lore);//设置Lore
            itemStack.setItemMeta(meta);
            inv.setItem(i*9+j,itemStack);
            j++;
            if(j==7){
                j = 2;
                i++;
            }
        }

        //让玩家打开箱子GUI
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        //保护箱子GUI
        Player player = (Player) event.getWhoClicked();
        InventoryView inv = player.getOpenInventory();
        if(inv.getTitle().equals("§a[§b每日任务§a]§f"))event.setCancelled(true);
    }

    /**
     * 解析这段lore中存在的变量等
     * @param task 玩家任务的对象
     * @lore 需要解析的lore字符串
     * @return 解析完成后的lore字符串
     * */
    private static String loreAnalysis(PlayerTask task,String lore){
        //TODO Lore变量解析
        //
        //

        switch (task.task_type){
            case "kill":
            case "get":
            case "place":
            case "break":{
                lore = lore.replaceAll("%need%",task.args.get(1));
                lore = lore.replaceAll("%finished%",task.args.get(2));
                break;
            }
            case "fish":{
                lore = lore.replaceAll("%need%",task.args.get(0));
                lore = lore.replaceAll("%finished%",task.args.get(1));
                break;
            }


        }

        return lore;
    }

}
