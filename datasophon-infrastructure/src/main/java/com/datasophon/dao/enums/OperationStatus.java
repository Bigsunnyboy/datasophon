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

public enum OperationStatus {
    
    PENDING(1, "待执行"),
    RUNNING(2, "执行中"),
    SUCCESS(3, "执行成功"),
    FAILED(4, "执行失败"),
    PARTIAL_SUCCESS(5, "部分成功"),
    CANCELLED(6, "已取消"),
    TIMEOUT(7, "执行超时"),
    RETRYING(8, "重试中"),
    SCHEDULED(9, "已调度"),
    ;
    
    @EnumValue
    private int value;
    
    private String desc;
    
    OperationStatus(int value, String desc) {
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