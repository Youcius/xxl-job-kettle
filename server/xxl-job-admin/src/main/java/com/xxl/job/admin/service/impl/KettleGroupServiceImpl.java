package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.admin.dao.XxlKettleFileDao;
import com.xxl.job.admin.dao.XxlKettleGroupDao;
import com.xxl.job.admin.service.KettleGroupService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class KettleGroupServiceImpl implements KettleGroupService {
    @Resource
    private XxlKettleGroupDao kettleGroupDao;
    @Resource
    private XxlKettleFileDao kettleFileDao;

    @Override
    public ReturnT<List<XxlKettleGroup>> list() {
        return new ReturnT<List<XxlKettleGroup>>(kettleGroupDao.findAll());
    }

    @Override
    public ReturnT<String> add(XxlKettleGroup group) {
        ReturnT<String> validResult = validGroupName(group);
        if (validResult.getCode() != ReturnT.SUCCESS_CODE) {
            return validResult;
        }

        if (kettleGroupDao.loadByName(group.getGroupName()) != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group name already exists.");
        }

        Date now = new Date();
        group.setAddTime(now);
        group.setUpdateTime(now);
        int ret = kettleGroupDao.save(group);
        return ret > 0 ? ReturnT.SUCCESS : new ReturnT<String>(ReturnT.FAIL_CODE, "Add group failed.");
    }

    @Override
    public ReturnT<String> update(XxlKettleGroup group) {
        if (group == null || group.getId() <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Invalid group id.");
        }
        ReturnT<String> validResult = validGroupName(group);
        if (validResult.getCode() != ReturnT.SUCCESS_CODE) {
            return validResult;
        }

        XxlKettleGroup exists = kettleGroupDao.load(group.getId());
        if (exists == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group not found.");
        }

        XxlKettleGroup sameName = kettleGroupDao.loadByName(group.getGroupName());
        if (sameName != null && sameName.getId() != group.getId()) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group name already exists.");
        }

        group.setUpdateTime(new Date());
        int ret = kettleGroupDao.update(group);
        return ret > 0 ? ReturnT.SUCCESS : new ReturnT<String>(ReturnT.FAIL_CODE, "Update group failed.");
    }

    @Override
    public ReturnT<String> delete(int id) {
        if (id <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Invalid group id.");
        }
        XxlKettleGroup exists = kettleGroupDao.load(id);
        if (exists == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group not found.");
        }
        if (kettleFileDao.countByGroupId(id) > 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group contains Kettle files and cannot be deleted.");
        }

        int ret = kettleGroupDao.remove(id);
        return ret > 0 ? ReturnT.SUCCESS : new ReturnT<String>(ReturnT.FAIL_CODE, "Delete group failed.");
    }

    private ReturnT<String> validGroupName(XxlKettleGroup group) {
        if (group == null || group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group name is required.");
        }

        String groupName = group.getGroupName().trim();
        if (groupName.length() > 100) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group name length must be less than or equal to 100.");
        }
        if (groupName.contains("/") || groupName.contains("\\") || groupName.contains("..")) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Group name cannot contain slash, backslash, or '..'.");
        }

        group.setGroupName(groupName);
        if (group.getGroupDesc() != null) {
            group.setGroupDesc(group.getGroupDesc().trim());
        }
        return ReturnT.SUCCESS;
    }
}
