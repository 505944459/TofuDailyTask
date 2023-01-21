package cn.ricetofu.task.core;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.mysql.MysqlManager;
import cn.ricetofu.task.pojo.PlayerTask;
import cn.ricetofu.task.pojo.SavedPlayerData;
import cn.ricetofu.task.pojo.Task;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-16
 * @Discription: 与任务操作相关的类
 * */
public class TaskManager {

    //所有任务的列表
    public static Map<String,Task> all_tasks = new HashMap<>();

    //玩家以及玩家的每日任务map,这个表具有时效性，只会存储当天的玩家每日任务!
    public static Map<String, List<PlayerTask>> player_tasks = new HashMap<>();

    //日志记录对象
    private static Logger logger = Bukkit.getLogger();

    /**
     * 加载配置文件中的任务
     * */
    public static boolean loadTasks(File file){
        YamlConfiguration tasks = YamlConfiguration.loadConfiguration(file);
        //得到键集，也就是各个子任务的id啦
        Set<String> keys = tasks.getKeys(false);
        //任务加载
        for (String key : keys) {
            Task task = new Task();
            task.id = key;
            MemorySection section =(MemorySection) tasks.get(key);
            task.name = section.getString("name").replace('&','§');//替换特殊符号
            task.display = section.getString("display");
            task.weight = section.getInt("weight");
            List<String> lore = section.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                lore.add(i,lore.remove(i).replace('&','§'));
            }
            task.lore = lore;
            task.type = section.getString("type");
            if(section.contains("rewards"))task.rewards = section.getStringList("rewards");
            all_tasks.put(key,task);
        }
        //任务配置正确性校验
        taskCheck();
        return true;
    }

    /**
     * 完成一个任务过后调用的方法,在这里进行所有任务是否都完成的判断
     * @param player_id 玩家的id
     * @param id 任务的id
     * */
    public static void finishOne(String player_id,String id){
        List<PlayerTask> playerTasks = player_tasks.get(player_id);
        boolean all_finish = true;
        for (PlayerTask playerTask : playerTasks) {
            if(playerTask.task_id.equals(id)){
                playerTask.isFinish = true;
                Bukkit.getPlayer(player_id).sendMessage(MessageManager.header+MessageManager.task_finish_one.replaceAll("%task_name%",all_tasks.get(playerTask.task_id).name));
                //检测这个任务是否有单独奖励需要给玩家
                if(all_tasks.get(playerTask.task_id).rewards!=null&&all_tasks.get(playerTask.task_id).rewards.size()!=0){
                    if (RewardManager.givePlayerTaskReward(player_id,playerTask.task_id)) {
                        // TODO

                    }else {

                    }
                }
            }
            if(!playerTask.isFinish)all_finish = false;
        }

        //发送消息提示玩家
        if(all_finish)Bukkit.getPlayer(player_id).sendMessage(MessageManager.header+MessageManager.task_finished);
        if(all_finish&&ConfigManager.auto_reward){//完成了所有任务且开启了自动领取奖励

            //给玩家完成奖励
            if (!RewardManager.givePlayerDailyTaskReward(player_id)) {
                //如果没有成功给他，则证明玩家离线或者其他原因，可以下次直接使用命令手动获取
                Bukkit.getPlayer(player_id).sendMessage(MessageManager.header+MessageManager.get_reward_fail);
                logger.warning(TofuDailyTask.console_header+"玩家:"+player_id+"尝试领取每日奖励而没有成功");
            }
            /*

            //给玩家增加累计完成次数
            SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
            savedPlayerData.finished_times = savedPlayerData.finished_times +1;
            savedPlayerData.last_finished_date = PlayerDataManager.sdf.format(new Date());//修改上次完成的时间
            //立即保存一次
            if(!MysqlManager.enable)PlayerDataManager.saveOneLocal(TofuDailyTask.data_file,player_id);//本地保存
            else MysqlManager.saveOne(player_id);//Mysql保存

             */
        }
    }

    /**
     * 接取每日任务，如果已经接取了则不会二次接取
     * @param player_id 玩家id
     * @return 是否成功接取
     * */
    public static boolean getDailyTask(String player_id){
        if(!player_tasks.containsKey(player_id)){

            //分配配置文件里面个数的每日小任务
            List<PlayerTask> playerTasks = getRandomTasksByWeight(ConfigManager.small_tasks);


            if(playerTasks==null){//没有返回值诶~~什么情况捏~
                return false;
            }
            player_tasks.put(player_id,playerTasks);


            //如果玩家是第一次进来，那么PlayerDataManager里的玩家信息表就没有这个玩家，需要新建一个
            SavedPlayerData savedPlayerData = PlayerDataManager.playerDataMap.get(player_id);
            if (savedPlayerData==null) {
                savedPlayerData = new SavedPlayerData();
                savedPlayerData.finished_times = 0;
                PlayerDataManager.playerDataMap.put(player_id,savedPlayerData);
            }
            savedPlayerData.list = playerTasks;
            savedPlayerData.last_receive_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());//更新收到任务的时间
            return true;
        }

        return false;
    }

    /**
     * 按照权重获取指定个数的随机任务
     * @param count 数量
     * @return 获取到的任务表(但返回的列表长度不一定为count)
     * */
    public static List<PlayerTask> getRandomTasksByWeight(int count){
        if(count==0)return null;
        Collection<Task> values = all_tasks.values();
        List<Task> list = new ArrayList<>(values);

        //先从大到小的顺序排列
        //list.sort(Comparator.comparingInt(o -> o.weight));

        //TODO 打乱算法
        //自定义权重打乱算法
        Collections.shuffle(list);

        List<PlayerTask> result = new ArrayList<>();

        for (Task task : list) {
            result.add(new PlayerTask(task));
            count--;
            if(count==0)break;
        }
        if(count!=0){
            logger.warning(TofuDailyTask.console_header+"只能给玩家:"+(result.size())+"个任务!还缺少:"+count+"个,是否缺少任务配置？");
        }
        return result;
    }

    /**
     * 按照权重获取指定个数的随机任务+去掉had表里已经存在的任务id的任务
     * @param had 排除在外的任务id的列表
     * @param count 数量
     * @return 获取到的任务表
     * */
    public static List<PlayerTask> getRandomTasksByWeight(List<PlayerTask> had,int count){
        if(count==0)return null;

        Collection<Task> values = all_tasks.values();
        List<Task> list = new ArrayList<>(values);
        List<PlayerTask> result = new ArrayList<>();
        //TODO 打乱算法
        //自定义权重打乱算法
        Collections.shuffle(list);
        for (Task task : list) {
            if(!had.contains(task.id)){
                result.add(new PlayerTask(task));
                count--;
                if(count==0)break;;
            }
        }
        if(count!=0){
            logger.warning(TofuDailyTask.console_header+"只能给玩家:"+(result.size())+"个任务!还缺少:"+count+"个,是否缺少任务配置？");
        }
        return result;
    }


    /**
     * 检查目前插件已经加载的任务，主要是看参数有没有配置正确等，并在控制台予以提示
     * */
    private static void taskCheck(){
        int total = all_tasks.size();
        Set<String> wrong_tasks = new HashSet<>();
        for (String k : all_tasks.keySet()) {
            Task v = all_tasks.get(k);
            String[] s = v.type.split(" ");
            String type = s[0];
            switch (type){
                case "break":
                case "place":
                case "get": {//破坏，放置，获取任务
                    //参数长度校验
                    if(s.length!=3){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数个数错误!应为2个，实有"+(s.length-1)+"个");
                        wrong_tasks.add(k);
                        break;
                    }
                    //参数一校验，是否存在那么一种方块
                    if(Material.matchMaterial(s[1])==null){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数1错误!无法匹配到一个名字/id为:"+s[1]+"的物品");
                        wrong_tasks.add(k);
                        break;
                    }
                    //参数二校验，是否是一个数字
                    if(!isNumStr(s[2])){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数2错误!它应该是一个数字，而不是:"+s[2]);
                        wrong_tasks.add(k);
                        break;
                    }
                    //数字格式
                    if(Integer.parseInt(s[2])<=0){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数2错误!它应该是一个大于0的数字，而不是:"+s[2]);
                        wrong_tasks.add(k);
                        break;
                    }
                    break;
                }
                case "fish":{//钓鱼任务
                    //参数长度校验
                    if(s.length!=2){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数个数错误!应为1个，实有"+(s.length-1)+"个");
                        wrong_tasks.add(k);
                        break;
                    }
                    //参数一校验
                    if(!isNumStr(s[1])){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数1错误!它应该是一个数字，而不是:"+s[1]);
                        wrong_tasks.add(k);
                        break;
                    }
                    //数字格式
                    if(Integer.parseInt(s[1])<=0){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数1错误!它应该是一个大于0的数字，而不是:"+s[1]);
                        wrong_tasks.add(k);
                        break;
                    }
                    break;
                }
                case "kill":{//击杀任务
                    //参数长度校验
                    if(s.length!=3){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数个数错误!应为2个，实有"+(s.length-1)+"个");
                        wrong_tasks.add(k);
                        break;
                    }
                    //参数一校验，匹配实体类型
                    if(EntityType.fromName(s[1])==null){
                        if(isNumStr(s[1])){
                            if(EntityType.fromId(Integer.parseInt(s[1]))==null){
                                logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数1错误，无法匹配一个名字/id为:"+s[1]+"的实体");
                                wrong_tasks.add(k);
                                break;
                            }
                        }else {
                            logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数1错误，无法匹配一个名字/id为:"+s[1]+"的实体");
                            wrong_tasks.add(k);
                            break;
                        }
                    }
                    //参数二校验，是否是数字
                    if(!isNumStr(s[2])){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数2错误!它应该是一个数字，而不是:"+s[2]);
                        wrong_tasks.add(k);
                        break;
                    }
                    //数字格式
                    if(Integer.parseInt(s[2])<=0){
                        logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务参数2错误!它应该是一个大于0的数字，而不是:"+s[2]);
                        wrong_tasks.add(k);
                        break;
                    }
                    break;
                }
                default:{
                    logger.warning(TofuDailyTask.console_header+"id为:"+v.id+"的任务类型错误,未能成功匹配一个名为:"+s[0]+"的事件类型，他真的被插件支持吗");
                    wrong_tasks.add(k);
                }
            }
        }
        //删除错误的任务
        wrong_tasks.forEach( s ->{
            all_tasks.remove(s);
        });
        //加载提示
        logger.info(TofuDailyTask.console_header +"共成功加载了:§a"+all_tasks.size()+"§f个任务!加载失败了:§c"+(total-all_tasks.size())+"§f个");
    }

    /**
     * 判断传入的字符串是否是一个数字字符串
     * */
    private static boolean isNumStr(String s){
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i)>'9'||s.charAt(i)<'0')return false;
        }
        return true;
    }

}
