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

package com.datasophon.dao.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {
    
    SERVICE_CONTROL(1, "服务控制"),
    HEALTH_CHECK(2, "健康检查"),
    LOG_MANAGEMENT(3, "日志管理"),
    CONFIG_SYNC(4, "配置同步"),
    PERFORMANCE_MONITORING(5, "性能监控"),
    BACKUP_RECOVERY(6, "备份恢复"),
    UPGRADE_ROLLBACK(7, "升级回滚"),
    SECURITY_MANAGEMENT(8, "安全管理"),
    CUSTOM_OPERATION(9, "自定义操作"),
    ;
    
    @EnumValue
    private int value;
    
    private String desc;
    
    OperationType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    @JsonValue
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString() {
        return this.desc;
    }
}