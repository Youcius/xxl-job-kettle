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
        sb.append("#!/bin/bash
");
        sb.append("# XXL-JOB Kettle Task
");
        sb.append("# Generated at: #AUTO#
");
        sb.append("# File: ").append(fileName).append("

");
        sb.append("KETTLE_HOME=\"").append(kettleHome).append("\"
");
        sb.append("JOB_FILE=\"").append(jobPath).append("\"
");
        sb.append("LOG_FILE=\"").append(logFile).append("\"

");
        sb.append("mkdir -p \"").append(logPath).append("\"

");
        sb.append("if [ ! -f \"$JOB_FILE\" ]; then
");
        sb.append("    echo \"ERROR: Kettle file not found: $JOB_FILE\"
");
        sb.append("    exit 1
");
        sb.append("fi

");
        sb.append("cd \"$KETTLE_HOME\" || exit 1
");
        sb.append("sh ./").append(tool)
                .append(" -file=\"$JOB_FILE\"")
                .append(" -level=Basic")
                .append(" > \"$LOG_FILE\" 2>&1");
        if (!params.isEmpty()) {
            sb.append(" ").append(params);
        }
        sb.append("
exit $?
");
        return sb.toString();
    }

    private static String stripExt(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
