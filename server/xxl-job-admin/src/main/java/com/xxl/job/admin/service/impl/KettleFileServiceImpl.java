package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.admin.core.util.KettleScriptUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlKettleFileDao;
import com.xxl.job.admin.dao.XxlKettleGroupDao;
import com.xxl.job.admin.service.KettleFileService;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KettleFileServiceImpl implements KettleFileService {
    private static final long DEFAULT_MAX_FILE_SIZE = 100L * 1024L * 1024L;

    @Value("${xxl.kettle.file.store-path:/data/etl/jobs}")
    private String storePath;
    @Value("${xxl.kettle.file.max-size:104857600}")
    private long maxFileSize;
    @Value("${xxl.kettle.path:/opt/pdi/data-integration}")
    private String kettlePath;
    @Value("${xxl.kettle.log.path:/opt/pdi/logs}")
    private String kettleLogPath;
    @Resource
    private XxlKettleFileDao kettleFileDao;
    @Resource
    private XxlKettleGroupDao kettleGroupDao;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobService xxlJobService;

    @Override
    public ReturnT<Map<String, Object>> pageList(int start, int length, int groupId, String keyword, String fileType) {
        if (groupId <= 0) {
            return new ReturnT<Map<String, Object>>(ReturnT.FAIL_CODE, "Invalid group id.");
        }
        int offset = Math.max(start, 0);
        int pagesize = length > 0 ? length : 10;
        String type = normalizeFileType(fileType);
        if (fileType != null && fileType.trim().length() > 0 && type == null) {
            return new ReturnT<Map<String, Object>>(ReturnT.FAIL_CODE, "Invalid file type.");
        }

        List<XxlKettleFile> data = kettleFileDao.pageList(offset, pagesize, groupId, trimToNull(keyword), type);
        int recordsFiltered = kettleFileDao.pageListCount(offset, pagesize, groupId, trimToNull(keyword), type);
        int recordsTotal = kettleFileDao.countByGroupId(groupId);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("recordsTotal", recordsTotal);
        result.put("recordsFiltered", recordsFiltered);
        result.put("data", data);
        return new ReturnT<Map<String, Object>>(result);
    }

    @Override
    public ReturnT<XxlKettleFile> detail(int id) {
        if (id <= 0) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Invalid file id.");
        }
        XxlKettleFile file = kettleFileDao.load(id);
        return file != null ? new ReturnT<XxlKettleFile>(file) : new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "File not found.");
    }

    @Override
    public ReturnT<XxlKettleFile> upload(MultipartFile file, int groupId) {
        ReturnT<XxlKettleFile> validResult = validateUpload(file, groupId);
        if (validResult.getCode() != ReturnT.SUCCESS_CODE) {
            return validResult;
        }

        String originalName = file.getOriginalFilename().trim();
        String fileType = getFileType(originalName);
        XxlKettleGroup group = kettleGroupDao.load(groupId);
        Path root = getStoreRoot();
        Path groupDir = root.resolve(getSafeGroupDir(group, groupId)).normalize();
        if (!groupDir.startsWith(root)) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Invalid group directory.");
        }

        try {
            Files.createDirectories(groupDir);
            String physicalName = buildPhysicalName(originalName);
            Path target = groupDir.resolve(physicalName).normalize();
            if (!target.startsWith(groupDir)) {
                return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Invalid file path.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target);
            }

            Date now = new Date();
            XxlKettleFile exists = kettleFileDao.findByGroupAndName(groupId, originalName);
            XxlKettleFile saved = exists != null ? exists : new XxlKettleFile();
            saved.setGroupId(groupId);
            saved.setFileName(originalName);
            saved.setFileType(fileType);
            saved.setFilePath(root.relativize(target).toString().replace(File.separatorChar, '/'));
            saved.setFileSize(file.getSize());
            saved.setUpdateTime(now);
            if (exists != null) {
                saved.setVersion(exists.getVersion() + 1);
                if (kettleFileDao.update(saved) <= 0) {
                    deleteQuietly(target);
                    return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Update file failed.");
                }
            } else {
                saved.setVersion(1);
                saved.setAddTime(now);
                if (kettleFileDao.save(saved) <= 0) {
                    deleteQuietly(target);
                    return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Save file failed.");
                }
            }
            return new ReturnT<XxlKettleFile>(saved);
        } catch (IOException e) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Upload file failed: " + e.getMessage());
        }
    }

    @Override
    public ReturnT<List<XxlKettleFile>> uploadBatch(MultipartFile[] files, int groupId) {
        if (files == null || files.length == 0) {
            return new ReturnT<List<XxlKettleFile>>(ReturnT.FAIL_CODE, "File is required.");
        }
        List<XxlKettleFile> result = new ArrayList<XxlKettleFile>();
        for (MultipartFile file : files) {
            ReturnT<XxlKettleFile> uploadResult = upload(file, groupId);
            if (uploadResult.getCode() != ReturnT.SUCCESS_CODE) {
                return new ReturnT<List<XxlKettleFile>>(ReturnT.FAIL_CODE, uploadResult.getMsg());
            }
            result.add(uploadResult.getContent());
        }
        return new ReturnT<List<XxlKettleFile>>(result);
    }

    @Override
    public ReturnT<String> delete(int id) {
        if (id <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Invalid file id.");
        }
        XxlKettleFile file = kettleFileDao.load(id);
        if (file == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "File not found.");
        }
        Path target = resolveRegisteredPath(file.getFilePath());
        if (target == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Invalid registered file path.");
        }
        int ret = kettleFileDao.remove(id);
        if (ret <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Delete file failed.");
        }
        deleteQuietly(target);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "File id is required.");
        }
        for (Integer id : ids) {
            ReturnT<String> deleteResult = delete(id == null ? 0 : id);
            if (deleteResult.getCode() != ReturnT.SUCCESS_CODE) {
                return deleteResult;
            }
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<File> downloadFile(int id) {
        if (id <= 0) {
            return new ReturnT<File>(ReturnT.FAIL_CODE, "Invalid file id.");
        }
        XxlKettleFile file = kettleFileDao.load(id);
        if (file == null) {
            return new ReturnT<File>(ReturnT.FAIL_CODE, "File not found.");
        }
        Path target = resolveRegisteredPath(file.getFilePath());
        if (target == null || !Files.isRegularFile(target)) {
            return new ReturnT<File>(ReturnT.FAIL_CODE, "File not found on disk.");
        }
        return new ReturnT<File>(target.toFile());
    }

    @Override
    @Transactional
    public ReturnT<String> createJob(Map<String, Object> data, XxlJobUser loginUser) {
        int kettleFileId = getInt(data, "kettleFileId", getInt(data, "fileId", 0));
        if (kettleFileId <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Kettle file id is required.");
        }

        XxlKettleFile file = kettleFileDao.load(kettleFileId);
        if (file == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Kettle file not found.");
        }
        if (file.getJobId() != null && file.getJobId() > 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Kettle file already has a related job.");
        }

        Path target = resolveRegisteredPath(file.getFilePath());
        if (target == null || !Files.isRegularFile(target)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Kettle file not found on disk.");
        }

        int jobGroup = getInt(data, "jobGroup", 0);
        if (jobGroup <= 0) {
            jobGroup = resolveDefaultExecutorGroup();
        }
        if (jobGroup <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "No executor group is available.");
        }
        if (loginUser != null && !loginUser.validPermission(jobGroup)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Permission denied for executor group.");
        }

        Path root = getStoreRoot();
        Path relative = root.relativize(target);
        String physicalFileName = relative.getFileName().toString();
        Path parent = relative.getParent();
        String groupDir = parent == null ? "" : parent.toString().replace(File.separatorChar, '/');

        String executorParam = trimToEmpty(getString(data, "executorParam", ""));
        String glueSource = KettleScriptUtil.generateScript(
                trimToDefault(kettlePath, "/opt/pdi/data-integration"),
                groupDir,
                physicalFileName,
                file.getFileType(),
                trimToDefault(kettleLogPath, "/opt/pdi/logs"),
                executorParam);

        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setJobGroup(jobGroup);
        jobInfo.setJobDesc(trimToDefault(getString(data, "jobDesc", null), stripExt(file.getFileName()) + "-调度任务"));
        jobInfo.setAuthor(trimToDefault(getString(data, "author", null), loginUser == null ? "admin" : loginUser.getUsername()));
        jobInfo.setAlarmEmail(trimToEmpty(getString(data, "alarmEmail", "")));
        jobInfo.setScheduleType(trimToDefault(getString(data, "scheduleType", null), "CRON"));
        jobInfo.setScheduleConf(trimToDefault(getString(data, "scheduleConf", null), "0 */5 * * * ?"));
        jobInfo.setMisfireStrategy(trimToDefault(getString(data, "misfireStrategy", null), "DO_NOTHING"));
        jobInfo.setExecutorRouteStrategy(trimToDefault(getString(data, "executorRouteStrategy", null), "FIRST"));
        jobInfo.setExecutorHandler("");
        jobInfo.setExecutorParam(executorParam);
        jobInfo.setExecutorBlockStrategy(trimToDefault(getString(data, "executorBlockStrategy", null), "SERIAL_EXECUTION"));
        jobInfo.setExecutorTimeout(getInt(data, "executorTimeout", 0));
        jobInfo.setExecutorFailRetryCount(getInt(data, "executorFailRetryCount", 0));
        jobInfo.setGlueType("GLUE_SHELL");
        jobInfo.setGlueSource(glueSource);
        jobInfo.setGlueRemark("Kettle file: " + file.getFileName() + " (#" + file.getId() + ")");
        jobInfo.setChildJobId(trimToEmpty(getString(data, "childJobId", "")));

        ReturnT<String> addResult = xxlJobService.add(jobInfo, loginUser);
        if (addResult.getCode() != ReturnT.SUCCESS_CODE) {
            return addResult;
        }

        int jobId = Integer.parseInt(addResult.getContent());
        file.setJobId(jobId);
        file.setUpdateTime(new Date());
        if (kettleFileDao.update(file) <= 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Create job succeeded but linking Kettle file failed.");
        }
        return new ReturnT<String>(String.valueOf(jobId));
    }

    private ReturnT<XxlKettleFile> validateUpload(MultipartFile file, int groupId) {
        if (groupId <= 0 || kettleGroupDao.load(groupId) == null) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Group not found.");
        }
        if (file == null || file.isEmpty()) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "File is required.");
        }
        long limit = maxFileSize > 0 ? maxFileSize : DEFAULT_MAX_FILE_SIZE;
        if (file.getSize() > limit) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "File size exceeds limit.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "File name is required.");
        }
        originalName = originalName.trim();
        if (originalName.contains("/") || originalName.contains("\\") || originalName.contains("..")) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Invalid file name.");
        }
        if (getFileType(originalName) == null) {
            return new ReturnT<XxlKettleFile>(ReturnT.FAIL_CODE, "Only .ktr and .kjb files are allowed.");
        }
        return new ReturnT<XxlKettleFile>((XxlKettleFile) null);
    }

    private Path getStoreRoot() {
        String path = trimToNull(storePath);
        if (path == null) {
            path = "/data/etl/jobs";
        }
        return Paths.get(path).toAbsolutePath().normalize();
    }

    private String getSafeGroupDir(XxlKettleGroup group, int groupId) {
        String groupName = group == null ? null : trimToNull(group.getGroupName());
        if (groupName != null && !groupName.contains("/") && !groupName.contains("\\") && !groupName.contains("..")) {
            return groupName;
        }
        return String.valueOf(groupId);
    }

    private Path resolveRegisteredPath(String filePath) {
        String path = trimToNull(filePath);
        if (path == null) {
            return null;
        }
        Path root = getStoreRoot();
        Path target = Paths.get(path);
        if (!target.isAbsolute()) {
            target = root.resolve(path);
        }
        target = target.toAbsolutePath().normalize();
        return target.startsWith(root) ? target : null;
    }

    private String buildPhysicalName(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? originalName.substring(0, dotIndex) : originalName;
        String ext = dotIndex > -1 ? originalName.substring(dotIndex).toLowerCase() : "";
        String suffix = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return baseName + "_" + suffix + ext;
    }

    private String normalizeFileType(String fileType) {
        String type = trimToNull(fileType);
        if (type == null) {
            return null;
        }
        type = type.toUpperCase();
        return "KTR".equals(type) || "KJB".equals(type) ? type : null;
    }

    private String getFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return normalizeFileType(fileName.substring(dotIndex + 1));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimValue = value.trim();
        return trimValue.length() > 0 ? trimValue : null;
    }

    private String trimToEmpty(String value) {
        String trimValue = trimToNull(value);
        return trimValue == null ? "" : trimValue;
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimValue = trimToNull(value);
        return trimValue == null ? defaultValue : trimValue;
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        if (data == null || !data.containsKey(key) || data.get(key) == null) {
            return defaultValue;
        }
        return String.valueOf(data.get(key));
    }

    private int getInt(Map<String, Object> data, String key, int defaultValue) {
        String value = getString(data, key, null);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int resolveDefaultExecutorGroup() {
        List<XxlJobGroup> groups = xxlJobGroupDao.findAll();
        if (groups == null || groups.isEmpty()) {
            return 0;
        }
        for (XxlJobGroup group : groups) {
            if ("xxl-job-executor-kettle".equals(group.getAppname())) {
                return group.getId();
            }
        }
        return groups.get(0).getId();
    }

    private String stripExt(String fileName) {
        if (fileName == null) {
            return "Kettle";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private void deleteQuietly(Path path) {
        try {
            if (path != null && path.startsWith(getStoreRoot())) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
        }
    }
}
