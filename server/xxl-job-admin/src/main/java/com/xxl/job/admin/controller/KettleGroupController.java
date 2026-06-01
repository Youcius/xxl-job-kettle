package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.admin.service.KettleGroupService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/api/kettle/group")
public class KettleGroupController {

    @Resource
    private KettleGroupService kettleGroupService;

    @RequestMapping("/list")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<List<XxlKettleGroup>> list() { return kettleGroupService.list(); }

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> add(@RequestBody XxlKettleGroup group) { return kettleGroupService.add(group); }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> update(@RequestBody XxlKettleGroup group) { return kettleGroupService.update(group); }

    @RequestMapping("/delete")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> delete(@RequestParam int id) { return kettleGroupService.delete(id); }
}
