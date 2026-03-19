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

package com.datasophon.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigMergeStrategy {
    
    PRESERVE_EXISTING(1, "PRESERVE_EXISTING"),
    OVERRIDE_WITH_PLATFORM(2, "OVERRIDE_WITH_PLATFORM"),
    SMART_MERGE(3, "SMART_MERGE");
    
    private Integer code;
    private String name;
    
    ConfigMergeStrategy(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    @JsonValue
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}