package cn.ricetofu.task.pojo;


import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-19
 * @Discription: 玩家任务对象
 * */
public class PlayerTask {

    //对应任务id
    public String task_id;

    //任务的类型
    public String task_type;

    //任务的完成情况
    public boolean isFinish = false;

    //任务的参数，一般在任务进行中会使用，用于保存任务的进度等
    public List<String> args = new ArrayList<>();


    public PlayerTask(Task task){
        this.task_id = task.id;
        this.task_type = task.type.split(" ")[0];
        argsInit(task.type);
    }

    public PlayerTask(Task task,boolean isFinish){
        this.task_id = task.id;
        this.task_type = task.type.split(" ")[0];
        this.isFinish = isFinish;
        argsInit(task.type);
    }

    /**
     * 对该任务对象的任务参数进行初始化操作
     * */
    public void argsInit(String type){
        switch (task_type){

            case "break":
            case "place":
            case "get":
            case "kill":
            {
                String[] s = type.split(" ");
                args.add(s[1]);//材料/方块/实体的id
                args.add(s[2]);//需要完成的数量
                args.add("0");//已经完成的数量
                break;
            }
            case "fish":{
                String[] s = type.split(" ");
                args.add(s[1]);//需要完成的数量
                args.add("0");//已经完成的数量
                break;
            }


        }


    }

}
