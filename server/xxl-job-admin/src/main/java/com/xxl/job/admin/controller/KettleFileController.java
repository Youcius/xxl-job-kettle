package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.admin.service.KettleFileService;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/api/kettle/file")
public class KettleFileController {
    private static final Logger logger = LoggerFactory.getLogger(KettleFileController.class);

    @Resource
    private KettleFileService kettleFileService;

    @RequestMapping("/list")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "10") int length,
            @RequestParam int groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType) {
        return kettleFileService.pageList(start, length, groupId, keyword, fileType);
    }

    @RequestMapping("/upload")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<XxlKettleFile> upload(@RequestParam("file") MultipartFile file, @RequestParam("groupId") int groupId) {
        return kettleFileService.upload(file, groupId);
    }

    @RequestMapping("/upload/batch")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> uploadBatch(@RequestParam("files") MultipartFile[] files, @RequestParam("groupId") int groupId) {
        int success = 0, fail = 0;
        for (MultipartFile file : files) {
            if (kettleFileService.upload(file, groupId).getCode() == ReturnT.SUCCESS_CODE) success++;
            else fail++;
        }
        return new ReturnT<>("Uploaded: " + success + " ok, " + fail + " failed");
    }

    @RequestMapping("/delete")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> delete(@RequestParam int id) { return kettleFileService.delete(id); }

    @RequestMapping("/delete/batch")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> deleteBatch(@RequestBody int[] ids) { return kettleFileService.deleteBatch(ids); }

    @RequestMapping("/download")
    @PermissionLimit(limit = false)
    public void download(@RequestParam int id, HttpServletResponse response) { kettleFileService.download(id, response); }

    @RequestMapping("/detail")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<XxlKettleFile> detail(@RequestParam int id) {
        XxlKettleFile file = kettleFileService.load(id);
        return file != null ? new ReturnT<>(file) : new ReturnT<>(ReturnT.FAIL_CODE, "File not found");
    }
}
