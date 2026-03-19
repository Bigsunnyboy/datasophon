<!--
/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


 * @Date: 2026-03-17 02:00:00
 * @LastEditTime: 2026-03-17 02:00:00
 * @FilePath: \datasophon-ui\src\pages\componentDiscovery\detail.vue
-->
<template>
  <div class="component-discovery-detail card-shadow">
    <a-card class="mgb16 card-shadow">
      <a-row type="flex" align="middle">
        <a-col :span="12">
          <a-button icon="arrow-left" @click="goBack">返回列表</a-button>
          <span class="mgl16 task-title">组件发现任务详情</span>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-button type="primary" @click="refreshDetail" icon="reload" :loading="loading">刷新</a-button>
          <a-button class="mgl12" @click="viewResults" :disabled="!canViewResults">查看结果</a-button>
          <a-button class="mgl12" type="danger" @click="stopTask" :disabled="!canStopTask" v-if="taskData.status === 'RUNNING'">停止任务</a-button>
          <a-button class="mgl12" type="primary" @click="retryTask" :disabled="!canRetryTask" v-if="taskData.status === 'FAILED'">重试任务</a-button>
        </a-col>
      </a-row>
    </a-card>
    
    <a-row :gutter="16">
      <!-- 左侧任务信息 -->
      <a-col :span="8">
        <a-card title="任务信息" class="card-shadow mgb16">
          <a-descriptions :column="1" bordered>
            <a-descriptions-item label="任务ID">{{ taskData.id }}</a-descriptions-item>
            <a-descriptions-item label="任务名称">{{ taskData.taskName }}</a-descriptions-item>
            <a-descriptions-item label="集群">{{ taskData.clusterName }}</a-descriptions-item>
            <a-descriptions-item label="组件类型">{{ taskData.componentType }}</a-descriptions-item>
            <a-descriptions-item label="发现策略">{{ taskData.discoveryStrategy }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(taskData.status)">
                {{ getStatusText(taskData.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ taskData.createTime }}</a-descriptions-item>
            <a-descriptions-item label="开始时间">{{ taskData.startTime }}</a-descriptions-item>
            <a-descriptions-item label="完成时间">{{ taskData.completeTime }}</a-descriptions-item>
            <a-descriptions-item label="执行主机数">{{ taskData.targetHostCount }}</a-descriptions-item>
            <a-descriptions-item label="超时时间">{{ taskData.timeoutSeconds }}秒</a-descriptions-item>
            <a-descriptions-item label="并发数">{{ taskData.concurrentThreads }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
        
        <a-card title="目标主机" class="card-shadow" v-if="targetHosts.length > 0">
          <a-list item-layout="horizontal" :data-source="targetHosts" size="small">
            <a-list-item slot="renderItem" slot-scope="item">
              <a-list-item-meta>
                <div slot="title">
                  <a-icon type="desktop" class="mgr8" />
                  {{ item.hostname }}
                </div>
                <div slot="description">
                  <span class="mgr12">IP: {{ item.ip }}</span>
                  <span>架构: {{ item.cpuArchitecture }}</span>
                </div>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
      
      <!-- 中间进度和日志 -->
      <a-col :span="16">
        <a-card title="执行进度" class="card-shadow mgb16">
          <div v-if="taskData.status === 'RUNNING'">
            <a-progress :percent="progressPercent" :status="progressStatus" />
            <div class="progress-info mgt8">
              <span>已发现: {{ discoveredComponentsCount }} / {{ totalHosts }}</span>
              <span class="mgl16">成功率: {{ successRate }}%</span>
              <span class="mgl16">耗时: {{ elapsedTime }}</span>
            </div>
          </div>
          <div v-else-if="taskData.status === 'SUCCESS'">
            <a-alert message="任务执行成功" type="success" show-icon />
            <div class="progress-info mgt8">
              <span>共发现: {{ discoveredComponentsCount }} 个组件</span>
              <span class="mgl16">成功率: {{ successRate }}%</span>
              <span class="mgl16">总耗时: {{ elapsedTime }}</span>
            </div>
          </div>
          <div v-else-if="taskData.status === 'FAILED'">
            <a-alert message="任务执行失败" type="error" show-icon />
            <div class="progress-info mgt8">
              <span>失败原因: {{ failureReason }}</span>
            </div>
          </div>
          <div v-else>
            <a-alert message="任务待执行" type="info" show-icon />
          </div>
        </a-card>
        
        <a-card title="执行日志" class="card-shadow">
          <a-tabs v-model="logTabKey">
            <a-tab-pane key="summary" tab="摘要">
              <div class="log-container">
                <div v-for="(log, index) in summaryLogs" :key="index" class="log-line">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-level" :class="`log-level-${log.level.toLowerCase()}`">{{ log.level }}</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
                <div v-if="summaryLogs.length === 0" class="empty-log">
                  暂无日志
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="detail" tab="详细日志">
              <div class="log-container">
                <div v-for="(log, index) in detailLogs" :key="index" class="log-line">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-level" :class="`log-level-${log.level.toLowerCase()}`">{{ log.level }}</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
                <div v-if="detailLogs.length === 0" class="empty-log">
                  暂无详细日志
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="error" tab="错误日志" v-if="errorLogs.length > 0">
              <div class="log-container">
                <div v-for="(log, index) in errorLogs" :key="index" class="log-line">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-level log-level-error">ERROR</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
          
          <div class="log-controls mgt16">
            <a-button @click="refreshLogs" icon="reload" size="small">刷新日志</a-button>
            <a-button @click="clearLogs" icon="delete" size="small" class="mgl8">清空日志</a-button>
            <a-button @click="exportLogs" icon="download" size="small" class="mgl8">导出日志</a-button>
            <a-switch v-model="autoRefresh" class="mgl16" size="small" checked-children="自动刷新" un-checked-children="手动刷新" />
            <span class="mgl8">刷新间隔:</span>
            <a-select v-model="refreshInterval" size="small" style="width: 100px" class="mgl8">
              <a-select-option value="5">5秒</a-select-option>
              <a-select-option value="10">10秒</a-select-option>
              <a-select-option value="30">30秒</a-select-option>
              <a-select-option value="60">60秒</a-select-option>
            </a-select>
          </div>
        </a-card>
        
        <!-- 发现结果预览 -->
        <a-card title="发现结果预览" class="card-shadow mgt16" v-if="previewResults.length > 0">
          <a-table
            :columns="previewColumns"
            :data-source="previewResults"
            rowKey="id"
            size="small"
            :pagination="{ pageSize: 5 }"
          >
            <template slot="status" slot-scope="text, record">
              <a-tag :color="record.validated ? 'green' : 'orange'">
                {{ record.validated ? '已验证' : '待验证' }}
              </a-tag>
            </template>
            
            <template slot="action" slot-scope="text, record">
              <a-button type="link" @click="viewComponentDetail(record)" size="small">查看</a-button>
              <a-button type="link" @click="validateComponent(record)" size="small" v-if="!record.validated">验证</a-button>
            </template>
          </a-table>
          <div class="preview-footer mgt8">
            <a-button type="link" @click="viewResults">查看全部结果</a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "ComponentDiscoveryDetail",
  data() {
    return {
      loading: false,
      taskId: null,
      taskData: {
        id: "",
        taskName: "",
        clusterName: "",
        componentType: "",
        discoveryStrategy: "",
        status: "",
        createTime: "",
        startTime: "",
        completeTime: "",
        targetHostCount: 0,
        timeoutSeconds: 300,
        concurrentThreads: 5,
      },
      targetHosts: [],
      progressPercent: 0,
      discoveredComponentsCount: 0,
      totalHosts: 0,
      successRate: 100,
      elapsedTime: "0秒",
      failureReason: "",
      logTabKey: "summary",
      summaryLogs: [],
      detailLogs: [],
      errorLogs: [],
      autoRefresh: false,
      refreshInterval: "10",
      refreshTimer: null,
      previewResults: [],
      previewColumns: [
        { title: "主机", dataIndex: "hostname", key: "hostname" },
        { title: "组件类型", dataIndex: "componentType", key: "componentType" },
        { title: "版本", dataIndex: "version", key: "version" },
        { title: "端口", dataIndex: "port", key: "port" },
        { title: "状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
        { title: "操作", key: "action", scopedSlots: { customRender: "action" }, width: 120 },
      ],
    };
  },
  computed: {
    ...mapState({
      setting: (state) => state.setting,
    }),
    progressStatus() {
      if (this.taskData.status === "FAILED") return "exception";
      if (this.taskData.status === "SUCCESS") return "success";
      return "active";
    },
    canViewResults() {
      return this.taskData.status === "SUCCESS" || this.taskData.status === "FAILED";
    },
    canStopTask() {
      return this.taskData.status === "RUNNING";
    },
    canRetryTask() {
      return this.taskData.status === "FAILED";
    },
  },
  watch: {
    autoRefresh(val) {
      if (val) {
        this.startAutoRefresh();
      } else {
        this.stopAutoRefresh();
      }
    },
    refreshInterval() {
      if (this.autoRefresh) {
        this.stopAutoRefresh();
        this.startAutoRefresh();
      }
    },
  },
  mounted() {
    this.taskId = this.$route.params.taskId;
    if (this.taskId) {
      this.loadTaskDetail();
      this.loadTaskProgress();
      this.loadTaskLogs();
      this.loadPreviewResults();
    } else {
      this.$message.error("任务ID不存在");
      this.goBack();
    }
  },
  beforeDestroy() {
    this.stopAutoRefresh();
  },
  methods: {
    // 加载任务详情
    loadTaskDetail() {
      this.loading = true;
      this.$axiosPost(global.API.componentDiscovery.getTaskDetail, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            this.taskData = res.data || {};
            this.loadTargetHosts();
          } else {
            this.$message.error(res.message || "加载任务详情失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    // 加载目标主机
    loadTargetHosts() {
      if (!this.taskData.clusterId) return;
      
      this.$axiosPost(global.API.componentDiscovery.getTaskHosts, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            this.targetHosts = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载目标主机失败:", error);
        });
    },
    
    // 加载任务进度
    loadTaskProgress() {
      this.$axiosPost(global.API.componentDiscovery.getTaskProgress, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            const progress = res.data || {};
            this.progressPercent = progress.percent || 0;
            this.discoveredComponentsCount = progress.discoveredCount || 0;
            this.totalHosts = progress.totalHosts || 0;
            this.successRate = progress.successRate || 100;
            this.elapsedTime = progress.elapsedTime || "0秒";
            this.failureReason = progress.failureReason || "";
            
            // 如果任务还在运行，设置自动刷新
            if (this.taskData.status === "RUNNING" && !this.autoRefresh) {
              this.autoRefresh = true;
            }
          }
        })
        .catch((error) => {
          console.error("加载任务进度失败:", error);
        });
    },
    
    // 加载任务日志
    loadTaskLogs() {
      this.$axiosPost(global.API.componentDiscovery.getTaskLogs, { taskId: this.taskId, logType: "SUMMARY" })
        .then((res) => {
          if (res.code === 200) {
            this.summaryLogs = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载摘要日志失败:", error);
        });
      
      this.$axiosPost(global.API.componentDiscovery.getTaskLogs, { taskId: this.taskId, logType: "DETAIL" })
        .then((res) => {
          if (res.code === 200) {
            this.detailLogs = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载详细日志失败:", error);
        });
      
      this.$axiosPost(global.API.componentDiscovery.getTaskLogs, { taskId: this.taskId, logType: "ERROR" })
        .then((res) => {
          if (res.code === 200) {
            this.errorLogs = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载错误日志失败:", error);
        });
    },
    
    // 加载预览结果
    loadPreviewResults() {
      this.$axiosPost(global.API.componentDiscovery.getPreviewResults, { taskId: this.taskId, limit: 5 })
        .then((res) => {
          if (res.code === 200) {
            this.previewResults = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载预览结果失败:", error);
        });
    },
    
    // 开始自动刷新
    startAutoRefresh() {
      this.stopAutoRefresh();
      const interval = parseInt(this.refreshInterval) * 1000;
      this.refreshTimer = setInterval(() => {
        this.loadTaskProgress();
        this.loadTaskLogs();
      }, interval);
    },
    
    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    },
    
    // 状态显示处理
    getStatusColor(status) {
      const colorMap = {
        PENDING: "blue",
        RUNNING: "orange",
        SUCCESS: "green",
        FAILED: "red",
      };
      return colorMap[status] || "default";
    },
    
    getStatusText(status) {
      const textMap = {
        PENDING: "待执行",
        RUNNING: "运行中",
        SUCCESS: "成功",
        FAILED: "失败",
      };
      return textMap[status] || status;
    },
    
    // 操作按钮处理
    goBack() {
      this.$router.push("/service-manage/component-discovery");
    },
    
    refreshDetail() {
      this.loadTaskDetail();
      this.loadTaskProgress();
      this.loadTaskLogs();
      this.loadPreviewResults();
    },
    
    refreshLogs() {
      this.loadTaskLogs();
    },
    
    clearLogs() {
      this.$confirm({
        title: "确认清空",
        content: "确定要清空当前任务的所有日志吗？此操作不可恢复。",
        okType: "danger",
        onOk: () => {
          this.$axiosPost(global.API.componentDiscovery.clearTaskLogs, { taskId: this.taskId })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("日志已清空");
                this.summaryLogs = [];
                this.detailLogs = [];
                this.errorLogs = [];
              } else {
                this.$message.error(res.message || "清空日志失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },
    
    exportLogs() {
      this.$axiosPost(global.API.componentDiscovery.exportTaskLogs, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200 && res.data) {
            // 创建下载链接
            const link = document.createElement("a");
            link.href = res.data;
            link.download = `discovery-task-${this.taskId}-logs.json`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            this.$message.success("日志导出成功");
          } else {
            this.$message.error(res.message || "导出日志失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        });
    },
    
    viewResults() {
      if (this.canViewResults) {
        this.$router.push(`/service-manage/component-discovery-results/${this.taskId}`);
      }
    },
    
    stopTask() {
      this.$confirm({
        title: "确认停止",
        content: `确定要停止发现任务 "${this.taskData.taskName}" 吗？`,
        onOk: () => {
          this.$axiosPost(global.API.componentDiscovery.stopTask, { taskId: this.taskId })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("任务已停止");
                this.refreshDetail();
              } else {
                this.$message.error(res.message || "停止任务失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },
    
    retryTask() {
      this.$axiosPost(global.API.componentDiscovery.retryTask, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            this.$message.success("任务已重新执行");
            this.refreshDetail();
          } else {
            this.$message.error(res.message || "重试任务失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        });
    },
    
    viewComponentDetail(record) {
      this.$router.push({
        path: `/service-manage/component-discovery-component/${record.id}`,
        query: { taskId: this.taskId },
      });
    },
    
    validateComponent(record) {
      this.$axiosPost(global.API.componentDiscovery.validateComponent, { resultId: record.id })
        .then((res) => {
          if (res.code === 200) {
            this.$message.success("组件验证成功");
            this.loadPreviewResults();
          } else {
            this.$message.error(res.message || "组件验证失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        });
    },
  },
};
</script>

<style lang="less" scoped>
.component-discovery-detail {
  background: #fff;
  padding: 20px;
  
  .task-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }
  
  .mgl16 {
    margin-left: 16px;
  }
  
  .mgt8 {
    margin-top: 8px;
  }
  
  .mgt16 {
    margin-top: 16px;
  }
  
  .mgr8 {
    margin-right: 8px;
  }
  
  .mgb16 {
    margin-bottom: 16px;
  }
  
  .progress-info {
    color: #666;
    font-size: 14px;
  }
  
  .log-container {
    max-height: 300px;
    overflow-y: auto;
    padding: 8px;
    background: #fafafa;
    border-radius: 4px;
    border: 1px solid #e8e8e8;
    
    .log-line {
      padding: 4px 0;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 12px;
      line-height: 1.5;
      border-bottom: 1px solid #eee;
      
      &:last-child {
        border-bottom: none;
      }
      
      .log-time {
        color: #999;
        margin-right: 12px;
      }
      
      .log-level {
        padding: 1px 4px;
        border-radius: 2px;
        margin-right: 8px;
        font-size: 10px;
        font-weight: bold;
        
        &.log-level-info {
          background: #e6f7ff;
          color: #1890ff;
        }
        
        &.log-level-warn {
          background: #fff7e6;
          color: #faad14;
        }
        
        &.log-level-error {
          background: #fff2f0;
          color: #ff4d4f;
        }
        
        &.log-level-success {
          background: #f6ffed;
          color: #52c41a;
        }
      }
      
      .log-message {
        color: #333;
      }
    }
    
    .empty-log {
      text-align: center;
      color: #999;
      padding: 20px;
    }
  }
  
  .log-controls {
    display: flex;
    align-items: center;
  }
  
  .preview-footer {
    text-align: center;
  }
}
</style>