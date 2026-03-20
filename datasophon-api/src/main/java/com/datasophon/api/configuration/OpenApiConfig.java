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

package com.datasophon.api.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 * 配置Swagger UI和API文档生成
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI datasophonOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DataSophon API Documentation")
                        .description("DataSophon大数据云原生平台API文档，包含服务组件接管、配置管理、运维管理等功能接口")
                        .version("1.2.2")
                        .contact(new Contact()
                                .name("DataSophon Team")
                                .url("https://github.com/datasophon/datasophon")
                                .email("datasophon@apache.org"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi operationsApi() {
        return GroupedOpenApi.builder()
                .group("operations")
                .pathsToMatch("/api/operations/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi configurationsApi() {
        return GroupedOpenApi.builder()
                .group("configurations")
                .pathsToMatch("/api/configurations/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi componentDiscoveryApi() {
        return GroupedOpenApi.builder()
                .group("component-discovery")
                .pathsToMatch("/api/component-discovery/**")
                .build();
    }
}