package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.admin.service.KettleFileService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/kettle/file")
public class KettleFileController {
    @Resource
    private KettleFileService kettleFileService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ReturnT<Map<String, Object>> list(@RequestParam(value = "start", defaultValue = "0") int start,
                                             @RequestParam(value = "length", defaultValue = "10") int length,
                                             @RequestParam(value = "groupId", defaultValue = "0") int groupId,
                                             String keyword,
                                             String fileType) {
        return kettleFileService.pageList(start, length, groupId, keyword, fileType);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ReturnT<XxlKettleFile> detail(int id) {
        return kettleFileService.detail(id);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<XxlKettleFile> upload(@RequestParam("file") MultipartFile file, int groupId) {
        return kettleFileService.upload(file, groupId);
    }

    @RequestMapping(value = "/upload/batch", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<List<XxlKettleFile>> uploadBatch(@RequestParam(value = "files", required = false) MultipartFile[] files,
                                                    @RequestParam(value = "file", required = false) MultipartFile[] file,
                                                    int groupId) {
        return kettleFileService.uploadBatch(files != null ? files : file, groupId);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnT<String> delete(int id) {
        return kettleFileService.delete(id);
    }

    @RequestMapping(value = "/delete/batch", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnT<String> deleteBatch(@RequestBody List<Integer> ids) {
        return kettleFileService.deleteBatch(ids);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<?> download(int id) throws UnsupportedEncodingException {
        ReturnT<File> downloadResult = kettleFileService.downloadFile(id);
        if (downloadResult.getCode() != ReturnT.SUCCESS_CODE) {
            return ResponseEntity.status(404).body(downloadResult);
        }
        File file = downloadResult.getContent();
        String fileName = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(new FileSystemResource(file));
    }
}
