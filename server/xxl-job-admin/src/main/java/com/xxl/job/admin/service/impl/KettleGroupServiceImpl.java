package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlKettleGroup;
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

    @Override
    public ReturnT<List<XxlKettleGroup>> list() {
        return new ReturnT<>(kettleGroupDao.findAll());
    }

    @Override
    public ReturnT<String> add(XxlKettleGroup group) {
        group.setAddTime(new Date());
        group.setUpdateTime(new Date());
        kettleGroupDao.save(group);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> update(XxlKettleGroup group) {
        group.setUpdateTime(new Date());
        kettleGroupDao.update(group);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> delete(int id) {
        kettleGroupDao.remove(id);
        return ReturnT.SUCCESS;
    }

    @Override
    public XxlKettleGroup load(int id) { return kettleGroupDao.load(id); }
}
