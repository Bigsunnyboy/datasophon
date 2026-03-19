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

package com.datasophon.api.strategy;

import com.datasophon.api.utils.MinaUtils;
import com.datasophon.common.enums.DetectionStatus;
import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;
import com.datasophon.common.model.DiscoveredComponent;
import com.datasophon.common.model.ExistingComponentConfig;

import org.apache.sshd.client.session.ClientSession;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDetectionStrategy implements ComponentDetectionStrategy {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public DetectionResult detect(DetectionContext context) {
        DetectionResult result = new DetectionResult();
        result.setStartTime(new Date());
        
        try {
            result = doDetect(context);
            result.setStatus(DetectionStatus.COMPLETED);
        } catch (Exception e) {
            logger.error("Detection failed: {}", e.getMessage(), e);
            result.setStatus(DetectionStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setEndTime(new Date());
            if (result.getStartTime() != null && result.getEndTime() != null) {
                result.setDurationMs(result.getEndTime().getTime() - result.getStartTime().getTime());
            }
        }
        
        return result;
    }
    
    /**
     * 子类实现的检测逻辑
     */
    protected abstract DetectionResult doDetect(DetectionContext context) throws Exception;
    
    /**
     * 执行远程命令
     */
    protected String executeRemoteCommand(String host, String command, Integer sshPort, String sshUser) {
        ClientSession session = null;
        try {
            session = MinaUtils.openConnection(host, sshPort, sshUser);
            if (session == null) {
                throw new RuntimeException("Failed to connect to host: " + host);
            }
            return MinaUtils.execCmdWithResult(session, command);
        } finally {
            if (session != null) {
                MinaUtils.closeConnection(session);
            }
        }
    }
    
    /**
     * 端口扫描
     */
    protected boolean scanPort(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 批量端口扫描
     */
    protected Map<Integer, Boolean> scanPorts(String host, List<Integer> ports, int timeoutMs) {
        Map<Integer, Boolean> results = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(ports.size(), 10));
        List<Future<PortScanResult>> futures = new ArrayList<>();
        
        for (Integer port : ports) {
            futures.add(executor.submit(new PortScanTask(host, port, timeoutMs)));
        }
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(timeoutMs * ports.size(), TimeUnit.MILLISECONDS);
            for (Future<PortScanResult> future : futures) {
                try {
                    PortScanResult scanResult = future.get();
                    results.put(scanResult.port, scanResult.open);
                } catch (Exception e) {
                    // Ignore
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return results;
    }
    
    /**
     * 从现有组件配置获取服务特定的端口列表
     */
    protected List<Integer> getServicePortsFromConfig(ExistingComponentConfig config) {
        List<Integer> ports = new ArrayList<>();
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getPortMappings() != null) {
            for (ExistingComponentConfig.PortMapping mapping : config.getExistingComponentSupport().getPortMappings()) {
                if (mapping.getPort() != null) {
                    ports.add(mapping.getPort());
                }
            }
        }
        return ports;
    }
    
    /**
     * 根据端口映射获取服务角色
     */
    protected String getServiceRoleForPort(ExistingComponentConfig config, Integer port) {
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getPortMappings() != null) {
            for (ExistingComponentConfig.PortMapping mapping : config.getExistingComponentSupport().getPortMappings()) {
                if (mapping.getPort() != null && mapping.getPort().equals(port)) {
                    return mapping.getServiceRole();
                }
            }
        }
        return "UNKNOWN";
    }
    
    /**
     * 从现有组件配置获取进程名称模式
     */
    protected List<String> getProcessPatternsFromConfig(ExistingComponentConfig config) {
        List<String> patterns = new ArrayList<>();
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getServiceSpecificDetection() != null
                && config.getExistingComponentSupport().getServiceSpecificDetection().getExpectedProcessNames() != null) {
            patterns.addAll(config.getExistingComponentSupport().getServiceSpecificDetection().getExpectedProcessNames());
        }
        return patterns;
    }
    
    /**
     * 从现有组件配置获取配置文件路径
     */
    protected List<String> getConfigFilePathsFromConfig(ExistingComponentConfig config) {
        List<String> paths = new ArrayList<>();
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getConfigFilePaths() != null) {
            for (ExistingComponentConfig.ConfigFilePath configFilePath : config.getExistingComponentSupport().getConfigFilePaths()) {
                if (configFilePath.getPath() != null) {
                    paths.add(configFilePath.getPath());
                }
            }
        }
        return paths;
    }
    
    /**
     * 创建发现的组件对象
     */
    protected DiscoveredComponent createDiscoveredComponent(String host, Integer port, String serviceRole,
                                                            String serviceName, String detectionMethod) {
        DiscoveredComponent component = new DiscoveredComponent();
        component.setHost(host);
        component.setPort(port);
        component.setServiceRole(serviceRole);
        component.setServiceName(serviceName);
        component.setDetectionMethod(detectionMethod);
        component.setComponentId(generateComponentId(host, port, serviceRole));
        component.setConfidence(80); // 默认置信度
        return component;
    }
    
    /**
     * 生成组件ID
     */
    protected String generateComponentId(String host, Integer port, String serviceRole) {
        return String.format("%s-%s-%s", host, serviceRole, port != null ? port : "0");
    }
    
    /**
     * 端口扫描任务
     */
    private static class PortScanTask implements Callable<PortScanResult> {
        private final String host;
        private final int port;
        private final int timeoutMs;
        
        PortScanTask(String host, int port, int timeoutMs) {
            this.host = host;
            this.port = port;
            this.timeoutMs = timeoutMs;
        }
        
        @Override
        public PortScanResult call() {
            boolean open = false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                open = true;
            } catch (Exception e) {
                // Port is closed or unreachable
            }
            return new PortScanResult(port, open);
        }
    }
    
    /**
     * 端口扫描结果
     */
    private static class PortScanResult {
        final int port;
        final boolean open;
        
        PortScanResult(int port, boolean open) {
            this.port = port;
            this.open = open;
        }
    }
}