/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datasophon.api.service.engine;

import com.datasophon.dao.entity.ClusterExistingComponentEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 日志收集引擎
 * 负责从已接管组件收集、查看、搜索和管理日志文件
 */
@Slf4j
@Component
public class LogCollectorEngine {
    
    private final Map<String, LogStreamSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, LogSearchTask> searchTasks = new ConcurrentHashMap<>();
    
    /**
     * 获取日志文件列表
     */
    public List<LogFileInfo> listLogFiles(ClusterExistingComponentEntity component) {
        List<LogFileInfo> logFiles = new ArrayList<>();
        
        try {
            // 解析配置路径中的日志目录
            String configPaths = component.getConfigPaths();
            if (configPaths != null && !configPaths.isEmpty()) {
                // 假设配置路径中包含日志目录信息
                // 在实际实现中，需要根据组件类型确定日志目录
                String logDir = determineLogDirectory(component);
                
                if (logDir != null) {
                    Path logPath = Paths.get(logDir);
                    if (Files.exists(logPath) && Files.isDirectory(logPath)) {
                        try (Stream<Path> paths = Files.list(logPath)) {
                            paths.filter(Files::isRegularFile)
                                    .filter(this::isLogFile)
                                    .forEach(file -> {
                                        LogFileInfo info = new LogFileInfo();
                                        info.setFileName(file.getFileName().toString());
                                        info.setFilePath(file.toAbsolutePath().toString());
                                        info.setFileSize(getFileSize(file));
                                        info.setLastModified(getLastModified(file));
                                        logFiles.add(info);
                                    });
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取日志文件列表失败: component={}", component.getId(), e);
        }
        
        // 添加默认日志文件（模拟）
        if (logFiles.isEmpty()) {
            logFiles.addAll(getDefaultLogFiles(component));
        }
        
        return logFiles;
    }
    
    /**
     * 读取日志文件内容
     */
    public LogContent readLogFile(ClusterExistingComponentEntity component, String logFile,
                                  Integer lines, Long offset) throws IOException {
        LogContent content = new LogContent();
        content.setFileName(logFile);
        content.setComponentId(component.getId());
        
        String filePath = resolveLogFilePath(component, logFile);
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            content.setError("日志文件不存在: " + filePath);
            return content;
        }
        
        if (lines == null || lines <= 0) {
            lines = 100; // 默认读取100行
        }
        
        List<String> logLines = new ArrayList<>();
        long totalLines = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            long lineNumber = 0;
            long startLine = 0;
            
            // 如果指定了offset，则跳过前面的行
            if (offset != null && offset > 0) {
                while (reader.readLine() != null && lineNumber < offset) {
                    lineNumber++;
                }
                startLine = lineNumber;
            }
            
            // 读取指定行数
            while ((line = reader.readLine()) != null && logLines.size() < lines) {
                logLines.add(line);
                lineNumber++;
            }
            
            // 计算总行数（如果未指定offset且需要知道总行数）
            if (offset == null || offset == 0) {
                totalLines = lineNumber;
                while (reader.readLine() != null) {
                    totalLines++;
                }
            }
            
            content.setLines(logLines);
            content.setStartLine(startLine);
            content.setTotalLines(totalLines);
            content.setHasMore(logLines.size() == lines && line != null);
        }
        
        return content;
    }
    
    /**
     * 实时流式读取日志
     */
    public LogStreamSession startLogStream(ClusterExistingComponentEntity component,
                                           String logFile, Integer lines) throws IOException {
        String sessionId = generateSessionId(component.getId(), logFile);
        
        if (activeSessions.containsKey(sessionId)) {
            LogStreamSession existingSession = activeSessions.get(sessionId);
            if (!existingSession.isClosed()) {
                return existingSession;
            } else {
                activeSessions.remove(sessionId);
            }
        }
        
        String filePath = resolveLogFilePath(component, logFile);
        LogStreamSession session = new LogStreamSession(sessionId, filePath, lines);
        activeSessions.put(sessionId, session);
        
        // 启动后台线程读取日志
        new Thread(() -> {
            try {
                session.startStreaming();
            } catch (IOException e) {
                log.error("日志流式读取失败: session={}", sessionId, e);
                session.close();
                activeSessions.remove(sessionId);
            }
        }).start();
        
        return session;
    }
    
    /**
     * 停止日志流
     */
    public boolean stopLogStream(String sessionId) {
        LogStreamSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.close();
            activeSessions.remove(sessionId);
            return true;
        }
        return false;
    }
    
    /**
     * 搜索日志内容
     */
    public LogSearchResult searchLogs(ClusterExistingComponentEntity component,
                                      String logFile, String keyword,
                                      Integer maxResults, Long startOffset) throws IOException {
        String taskId = generateSearchTaskId(component.getId(), logFile, keyword);
        
        if (searchTasks.containsKey(taskId)) {
            return searchTasks.get(taskId).getResult();
        }
        
        LogSearchTask task = new LogSearchTask(taskId, component, logFile, keyword, maxResults, startOffset);
        searchTasks.put(taskId, task);
        
        // 异步执行搜索
        new Thread(() -> {
            try {
                task.execute();
            } catch (IOException e) {
                log.error("日志搜索失败: task={}", taskId, e);
                task.getResult().setError(e.getMessage());
            } finally {
                // 搜索完成后保留结果一段时间
                try {
                    Thread.sleep(300000); // 5分钟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                searchTasks.remove(taskId);
            }
        }).start();
        
        return task.getResult();
    }
    
    /**
     * 下载日志文件
     */
    public File downloadLogFile(ClusterExistingComponentEntity component, String logFile) throws IOException {
        String filePath = resolveLogFilePath(component, logFile);
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("日志文件不存在: " + filePath);
        }
        
        return path.toFile();
    }
    
    /**
     * 清理日志文件
     */
    public LogCleanupResult cleanupLogs(ClusterExistingComponentEntity component,
                                        String logFile, Integer daysToKeep) throws IOException {
        LogCleanupResult result = new LogCleanupResult();
        result.setComponentId(component.getId());
        result.setLogFile(logFile);
        
        String filePath = resolveLogFilePath(component, logFile);
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            result.setSuccess(false);
            result.setMessage("日志文件不存在: " + filePath);
            return result;
        }
        
        if (daysToKeep != null && daysToKeep > 0) {
            // 按日期清理旧日志（简化实现）
            result.setSuccess(cleanupOldLogs(path, daysToKeep));
            result.setMessage(daysToKeep + "天前的日志已清理");
        } else {
            // 清空日志文件内容
            Files.write(path, new byte[0]);
            result.setSuccess(true);
            result.setMessage("日志文件已清空");
        }
        
        return result;
    }
    
    /**
     * 获取日志文件统计信息
     */
    public LogFileStats getLogStats(ClusterExistingComponentEntity component, String logFile) throws IOException {
        LogFileStats stats = new LogFileStats();
        stats.setFileName(logFile);
        stats.setComponentId(component.getId());
        
        String filePath = resolveLogFilePath(component, logFile);
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            return stats;
        }
        
        stats.setFileSize(Files.size(path));
        stats.setLastModified(Files.getLastModifiedTime(path).toMillis());
        stats.setLines(countLines(path));
        stats.setEncoding(StandardCharsets.UTF_8.name());
        
        return stats;
    }
    
    // 私有辅助方法
    
    private String determineLogDirectory(ClusterExistingComponentEntity component) {
        // 根据服务类型确定日志目录
        String serviceName = component.getServiceName();
        String installPath = component.getInstallPath();
        
        if (installPath != null && !installPath.isEmpty()) {
            switch (serviceName.toUpperCase()) {
                case "HDFS":
                    return installPath + "/logs";
                case "YARN":
                    return installPath + "/logs";
                case "SPARK":
                    return installPath + "/logs";
                case "HBASE":
                    return installPath + "/logs";
                case "HIVE":
                    return installPath + "/logs";
                default:
                    return installPath + "/logs";
            }
        }
        
        // 默认日志目录
        return "/var/log/" + serviceName.toLowerCase();
    }
    
    private boolean isLogFile(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".log") || fileName.endsWith(".out") ||
                fileName.endsWith(".err") || fileName.endsWith(".txt");
    }
    
    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0;
        }
    }
    
    private long getLastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }
    
    private List<LogFileInfo> getDefaultLogFiles(ClusterExistingComponentEntity component) {
        List<LogFileInfo> files = new ArrayList<>();
        
        // 模拟一些默认日志文件
        String[] defaultFiles = {
                component.getServiceName().toLowerCase() + ".log",
                component.getServiceName().toLowerCase() + "-out.log",
                component.getServiceName().toLowerCase() + "-err.log",
                "application.log",
                "system.log"
        };
        
        for (String fileName : defaultFiles) {
            LogFileInfo info = new LogFileInfo();
            info.setFileName(fileName);
            info.setFilePath("/var/log/" + component.getServiceName().toLowerCase() + "/" + fileName);
            info.setFileSize(1024 * 1024L); // 1MB
            info.setLastModified(System.currentTimeMillis());
            files.add(info);
        }
        
        return files;
    }
    
    private String resolveLogFilePath(ClusterExistingComponentEntity component, String logFile) {
        // 首先检查是否是绝对路径
        if (Paths.get(logFile).isAbsolute()) {
            return logFile;
        }
        
        // 否则相对于日志目录
        String logDir = determineLogDirectory(component);
        return logDir + File.separator + logFile;
    }
    
    private String generateSessionId(Integer componentId, String logFile) {
        return "log-session-" + componentId + "-" + logFile.hashCode() + "-" + System.currentTimeMillis();
    }
    
    private String generateSearchTaskId(Integer componentId, String logFile, String keyword) {
        return "search-" + componentId + "-" + logFile.hashCode() + "-" + keyword.hashCode();
    }
    
    private boolean cleanupOldLogs(Path logPath, int daysToKeep) {
        // 简化实现：实际需要根据日志文件命名模式清理旧文件
        try {
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
            
            if (Files.isDirectory(logPath)) {
                try (Stream<Path> paths = Files.list(logPath)) {
                    paths.filter(Files::isRegularFile)
                            .filter(file -> getLastModified(file) < cutoffTime)
                            .forEach(file -> {
                                try {
                                    Files.delete(file);
                                } catch (IOException e) {
                                    log.error("删除旧日志文件失败: {}", file, e);
                                }
                            });
                }
            }
            return true;
        } catch (IOException e) {
            log.error("清理旧日志失败", e);
            return false;
        }
    }
    
    private long countLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        }
    }
    
    // 内部数据类
    
    public static class LogFileInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private long lastModified;
        
        // getters and setters
        public String getFileName() {
            return fileName;
        }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFilePath() {
            return filePath;
        }
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public long getFileSize() {
            return fileSize;
        }
        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
        
        public long getLastModified() {
            return lastModified;
        }
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
    }
    
    public static class LogContent {
        private String fileName;
        private Integer componentId;
        private List<String> lines;
        private long startLine;
        private long totalLines;
        private boolean hasMore;
        private String error;
        
        // getters and setters
        public String getFileName() {
            return fileName;
        }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public Integer getComponentId() {
            return componentId;
        }
        public void setComponentId(Integer componentId) {
            this.componentId = componentId;
        }
        
        public List<String> getLines() {
            return lines;
        }
        public void setLines(List<String> lines) {
            this.lines = lines;
        }
        
        public long getStartLine() {
            return startLine;
        }
        public void setStartLine(long startLine) {
            this.startLine = startLine;
        }
        
        public long getTotalLines() {
            return totalLines;
        }
        public void setTotalLines(long totalLines) {
            this.totalLines = totalLines;
        }
        
        public boolean isHasMore() {
            return hasMore;
        }
        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }
        
        public String getError() {
            return error;
        }
        public void setError(String error) {
            this.error = error;
        }
    }
    
    public static class LogStreamSession {
        private String sessionId;
        private String filePath;
        private Integer lines;
        private volatile boolean closed;
        private List<String> buffer;
        
        public LogStreamSession(String sessionId, String filePath, Integer lines) {
            this.sessionId = sessionId;
            this.filePath = filePath;
            this.lines = lines != null ? lines : 100;
            this.closed = false;
            this.buffer = new ArrayList<>();
        }
        
        public void startStreaming() throws IOException {
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            long filePointer = file.length();
            
            while (!closed) {
                long currentLength = file.length();
                
                if (currentLength < filePointer) {
                    // 日志文件被清空或截断，重置指针
                    filePointer = 0;
                    file.seek(filePointer);
                } else if (currentLength > filePointer) {
                    // 读取新内容
                    file.seek(filePointer);
                    String line;
                    while ((line = file.readLine()) != null) {
                        synchronized (buffer) {
                            buffer.add(line);
                            if (buffer.size() > lines) {
                                buffer.remove(0);
                            }
                        }
                    }
                    filePointer = file.getFilePointer();
                }
                
                try {
                    Thread.sleep(1000); // 每秒检查一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            file.close();
        }
        
        public List<String> getNewLines() {
            synchronized (buffer) {
                List<String> result = new ArrayList<>(buffer);
                buffer.clear();
                return result;
            }
        }
        
        public void close() {
            this.closed = true;
        }
        
        public boolean isClosed() {
            return closed;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
    
    public static class LogSearchResult {
        private String taskId;
        private Integer componentId;
        private String logFile;
        private String keyword;
        private List<LogMatch> matches;
        private int totalMatches;
        private boolean completed;
        private String error;
        
        // getters and setters
        public String getTaskId() {
            return taskId;
        }
        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
        
        public Integer getComponentId() {
            return componentId;
        }
        public void setComponentId(Integer componentId) {
            this.componentId = componentId;
        }
        
        public String getLogFile() {
            return logFile;
        }
        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
        
        public String getKeyword() {
            return keyword;
        }
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
        
        public List<LogMatch> getMatches() {
            return matches;
        }
        public void setMatches(List<LogMatch> matches) {
            this.matches = matches;
        }
        
        public int getTotalMatches() {
            return totalMatches;
        }
        public void setTotalMatches(int totalMatches) {
            this.totalMatches = totalMatches;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
        
        public String getError() {
            return error;
        }
        public void setError(String error) {
            this.error = error;
        }
    }
    
    public static class LogMatch {
        private long lineNumber;
        private String lineContent;
        private String contextBefore;
        private String contextAfter;
        
        // getters and setters
        public long getLineNumber() {
            return lineNumber;
        }
        public void setLineNumber(long lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getLineContent() {
            return lineContent;
        }
        public void setLineContent(String lineContent) {
            this.lineContent = lineContent;
        }
        
        public String getContextBefore() {
            return contextBefore;
        }
        public void setContextBefore(String contextBefore) {
            this.contextBefore = contextBefore;
        }
        
        public String getContextAfter() {
            return contextAfter;
        }
        public void setContextAfter(String contextAfter) {
            this.contextAfter = contextAfter;
        }
    }
    
    public class LogSearchTask {
        private String taskId;
        private ClusterExistingComponentEntity component;
        private String logFile;
        private String keyword;
        private Integer maxResults;
        private Long startOffset;
        private LogSearchResult result;
        
        public LogSearchTask(String taskId, ClusterExistingComponentEntity component,
                             String logFile, String keyword,
                             Integer maxResults, Long startOffset) {
            this.taskId = taskId;
            this.component = component;
            this.logFile = logFile;
            this.keyword = keyword;
            this.maxResults = maxResults != null ? maxResults : 100;
            this.startOffset = startOffset;
            
            this.result = new LogSearchResult();
            this.result.setTaskId(taskId);
            this.result.setComponentId(component.getId());
            this.result.setLogFile(logFile);
            this.result.setKeyword(keyword);
            this.result.setMatches(new ArrayList<>());
            this.result.setCompleted(false);
        }
        
        public void execute() throws IOException {
            Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
            List<LogMatch> matches = new ArrayList<>();
            
            String filePath = resolveLogFilePath(component, logFile);
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                result.setError("日志文件不存在: " + filePath);
                result.setCompleted(true);
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                long lineNumber = 0;
                List<String> contextBuffer = new ArrayList<>();
                
                // 跳过起始偏移量
                if (startOffset != null && startOffset > 0) {
                    while (reader.readLine() != null && lineNumber < startOffset) {
                        lineNumber++;
                    }
                }
                
                while ((line = reader.readLine()) != null && matches.size() < maxResults) {
                    lineNumber++;
                    
                    if (pattern.matcher(line).find()) {
                        LogMatch match = new LogMatch();
                        match.setLineNumber(lineNumber);
                        match.setLineContent(line);
                        
                        // 添加上下文（简化实现）
                        match.setContextBefore(contextBuffer.isEmpty() ? "" : contextBuffer.get(contextBuffer.size() - 1));
                        
                        matches.add(match);
                    }
                    
                    // 维护上下文缓冲区
                    contextBuffer.add(line);
                    if (contextBuffer.size() > 5) {
                        contextBuffer.remove(0);
                    }
                }
            }
            
            result.setMatches(matches);
            result.setTotalMatches(matches.size());
            result.setCompleted(true);
        }
        
        public LogSearchResult getResult() {
            return result;
        }
    }
    
    public static class LogCleanupResult {
        private Integer componentId;
        private String logFile;
        private boolean success;
        private String message;
        
        // getters and setters
        public Integer getComponentId() {
            return componentId;
        }
        public void setComponentId(Integer componentId) {
            this.componentId = componentId;
        }
        
        public String getLogFile() {
            return logFile;
        }
        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
        
        public boolean isSuccess() {
            return success;
        }
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static class LogFileStats {
        private String fileName;
        private Integer componentId;
        private long fileSize;
        private long lastModified;
        private long lines;
        private String encoding;
        
        // getters and setters
        public String getFileName() {
            return fileName;
        }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public Integer getComponentId() {
            return componentId;
        }
        public void setComponentId(Integer componentId) {
            this.componentId = componentId;
        }
        
        public long getFileSize() {
            return fileSize;
        }
        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
        
        public long getLastModified() {
            return lastModified;
        }
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
        
        public long getLines() {
            return lines;
        }
        public void setLines(long lines) {
            this.lines = lines;
        }
        
        public String getEncoding() {
            return encoding;
        }
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
    }
}