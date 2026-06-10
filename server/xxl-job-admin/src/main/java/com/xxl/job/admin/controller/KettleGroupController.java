package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.admin.service.KettleGroupService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/api/kettle/group")
public class KettleGroupController {
    @Resource
    private KettleGroupService kettleGroupService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ReturnT<List<XxlKettleGroup>> list() {
        return kettleGroupService.list();
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> add(@RequestBody XxlKettleGroup group) {
        return kettleGroupService.add(group);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> update(@RequestBody XxlKettleGroup group) {
        return kettleGroupService.update(group);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnT<String> delete(int id) {
        return kettleGroupService.delete(id);
    }
}
