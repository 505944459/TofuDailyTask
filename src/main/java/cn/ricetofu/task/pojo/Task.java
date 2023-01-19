package cn.ricetofu.task.pojo;


import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 任务实体类
 * */
public class Task {

    //任务的id
    public String id;

    //任务的名称
    public String name;

    //任务的显示物品
    public String display;

    //任务的权重
    public Integer weight;

    //任务的物品lore
    public List<String> lore;

    //任务的类型,包含了一个任务类型和任务参数的字符串
    public String type;

    //该任务完成的单独奖励
    public List<String> rewards;

}
