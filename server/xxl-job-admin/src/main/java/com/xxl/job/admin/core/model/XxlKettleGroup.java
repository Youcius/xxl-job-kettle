package com.xxl.job.admin.core.model;

import java.util.Date;

public class XxlKettleGroup {
    private int id;
    private String groupName;
    private String groupDesc;
    private Date addTime;
    private Date updateTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getGroupDesc() { return groupDesc; }
    public void setGroupDesc(String groupDesc) { this.groupDesc = groupDesc; }
    public Date getAddTime() { return addTime; }
    public void setAddTime(Date addTime) { this.addTime = addTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
