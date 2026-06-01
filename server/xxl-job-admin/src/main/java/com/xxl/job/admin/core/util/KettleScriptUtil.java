package com.xxl.job.admin.core.util;

public class KettleScriptUtil {
    private KettleScriptUtil() {}

    public static String generateScript(String kettleHome,
                                        String groupName,
                                        String fileName,
                                        String fileType,
                                        String logPath,
                                        String execParam) {
        String tool = "KJB".equalsIgnoreCase(fileType) ? "kitchen.sh" : "pan.sh";
        String jobPath = kettleHome + "/" + groupName + "/" + fileName;
        String logFile = logPath + "/" + stripExt(fileName) +
                "_$(date +%Y%m%d_%H%M%S).log";
        String params = (execParam != null && !execParam.trim().isEmpty())
                ? " " + execParam.trim() : "";

        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");
        sb.append("# XXL-JOB Kettle Task\n");
        sb.append("# Generated at: #AUTO#\n");
        sb.append("# File: ").append(fileName).append("\n\n");
        sb.append("KETTLE_HOME=\"").append(kettleHome).append("\"\n");
        sb.append("JOB_FILE=\"").append(jobPath).append("\"\n");
        sb.append("LOG_FILE=\"").append(logFile).append("\"\n\n");
        sb.append("mkdir -p \"").append(logPath).append("\"\n\n");
        sb.append("if [ ! -f \"$JOB_FILE\" ]; then\n");
        sb.append("    echo \"ERROR: Kettle file not found: $JOB_FILE\"\n");
        sb.append("    exit 1\n");
        sb.append("fi\n\n");
        sb.append("cd \"$KETTLE_HOME\" || exit 1\n");
        sb.append("sh ./").append(tool)
                .append(" -file=\"$JOB_FILE\"")
                .append(" -level=Basic");
        if (!params.isEmpty()) {
            sb.append(params);
        }
        sb.append(" > \"$LOG_FILE\" 2>&1\n");
        sb.append("exit $?\n");
        return sb.toString();
    }

    private static String stripExt(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
