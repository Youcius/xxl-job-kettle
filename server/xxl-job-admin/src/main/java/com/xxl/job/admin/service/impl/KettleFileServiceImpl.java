package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.admin.dao.XxlKettleFileDao;
import com.xxl.job.admin.dao.XxlKettleGroupDao;
import com.xxl.job.admin.service.KettleFileService;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class KettleFileServiceImpl implements KettleFileService {

    private static final Logger logger = LoggerFactory.getLogger(KettleFileServiceImpl.class);
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".ktr", ".kjb"));

    @Value("${xxl.kettle.file.store-path:/data/etl/jobs}")
    private String storePath;

    @Value("${xxl.kettle.file.max-size:104857600}")
    private long maxFileSize;

    @Resource
    private XxlKettleFileDao kettleFileDao;
    @Resource
    private XxlKettleGroupDao kettleGroupDao;

    @Override
    public ReturnT<Map<String, Object>> pageList(int start, int length, int groupId, String keyword, String fileType) {
        List<XxlKettleFile> list = kettleFileDao.pageList(start, length, groupId, keyword, fileType);
        int count = kettleFileDao.pageListCount(start, length, groupId, keyword, fileType);
        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", count);
        result.put("recordsFiltered", count);
        result.put("data", list);
        return new ReturnT<>(result);
    }

    @Override
    public ReturnT<XxlKettleFile> upload(MultipartFile file, int groupId) {
        if (file.isEmpty()) return new ReturnT<>(ReturnT.FAIL_CODE, "File is empty");
        String originalName = file.getOriginalFilename();
        if (originalName == null) return new ReturnT<>(ReturnT.FAIL_CODE, "Invalid filename");
        validateExtension(originalName);
        validatePathSafety(originalName);
        if (file.getSize() > maxFileSize)
            return new ReturnT<>(ReturnT.FAIL_CODE, "File too large, max " + maxFileSize/1024/1024 + "MB");
        XxlKettleGroup group = kettleGroupDao.load(groupId);
        if (group == null) return new ReturnT<>(ReturnT.FAIL_CODE, "Group not found");
        String fileType = getFileType(originalName);
        try {
            Path dir = Paths.get(storePath, group.getGroupName());
            Files.createDirectories(dir);
            Path targetFile = dir.resolve(originalName);
            file.transferTo(targetFile.toFile());
            XxlKettleFile exist = kettleFileDao.findByGroupAndName(groupId, originalName);
            if (exist != null) {
                exist.setVersion(exist.getVersion() + 1);
                exist.setFileSize(file.getSize());
                exist.setUpdateTime(new Date());
                exist.setFilePath(group.getGroupName() + "/" + originalName);
                kettleFileDao.update(exist);
                return new ReturnT<>(exist);
            }
            XxlKettleFile kettleFile = new XxlKettleFile();
            kettleFile.setGroupId(groupId);
            kettleFile.setFileName(originalName);
            kettleFile.setFileType(fileType);
            kettleFile.setFilePath(group.getGroupName() + "/" + originalName);
            kettleFile.setFileSize(file.getSize());
            kettleFile.setVersion(1);
            kettleFile.setAddTime(new Date());
            kettleFile.setUpdateTime(new Date());
            kettleFileDao.save(kettleFile);
            return new ReturnT<>(kettleFile);
        } catch (IOException e) {
            logger.error("Kettle file upload failed", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "Save failed: " + e.getMessage());
        }
    }

    @Override
    public ReturnT<String> delete(int id) {
        XxlKettleFile file = kettleFileDao.load(id);
        if (file == null) return new ReturnT<>(ReturnT.FAIL_CODE, "File not found");
        try {
            Path physical = Paths.get(storePath, file.getFilePath());
            Files.deleteIfExists(physical);
        } catch (IOException e) {
            logger.warn("Failed to delete physical file: {}", file.getFilePath(), e);
        }
        kettleFileDao.remove(id);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> deleteBatch(int[] ids) {
        for (int id : ids) delete(id);
        return ReturnT.SUCCESS;
    }

    @Override
    public void download(int id, HttpServletResponse response) {
        XxlKettleFile file = kettleFileDao.load(id);
        if (file == null) throw new RuntimeException("File not found");
        Path physical = Paths.get(storePath, file.getFilePath());
        if (!Files.exists(physical)) throw new RuntimeException("Physical file missing");
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFileName(), "UTF-8") + "\"");
            Files.copy(physical, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    @Override
    public XxlKettleFile load(int id) { return kettleFileDao.load(id); }

    private void validateExtension(String filename) {
        String lower = filename.toLowerCase();
        if (ALLOWED_EXTENSIONS.stream().noneMatch(lower::endsWith))
            throw new RuntimeException("Only .ktr and .kjb files allowed");
    }

    private void validatePathSafety(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\"))
            throw new RuntimeException("Filename contains invalid characters");
        if (filename.length() > 255) throw new RuntimeException("Filename too long");
    }

    private String getFileType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".kjb")) return "KJB";
        if (lower.endsWith(".ktr")) return "KTR";
        return "UNKNOWN";
    }
}
