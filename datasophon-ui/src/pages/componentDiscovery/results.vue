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


 * @Date: 2026-03-17 02:10:00
 * @LastEditTime: 2026-03-17 02:10:00
 * @FilePath: \datasophon-ui\src\pages\componentDiscovery\results.vue
-->
<template>
  <div class="component-discovery-results card-shadow">
    <a-card class="mgb16 card-shadow">
      <a-row type="flex" align="middle">
        <a-col :span="12">
          <a-button icon="arrow-left" @click="goBack">返回详情</a-button>
          <span class="mgl16 task-title">组件发现结果 - {{ taskData.taskName }}</span>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-button type="primary" @click="refreshResults" icon="reload" :loading="loading">刷新</a-button>
          <a-dropdown class="mgl12">
            <a-menu slot="overlay" @click="handleBatchMenuClick">
              <a-menu-item key="batchValidate" :disabled="selectedRowKeys.length === 0">批量验证</a-menu-item>
              <a-menu-item key="batchImport" :disabled="!hasValidatedComponents">批量导入</a-menu-item>
              <a-menu-item key="batchDelete" :disabled="selectedRowKeys.length === 0">批量删除</a-menu-item>
            </a-menu>
            <a-button type="primary">
              批量操作
              <a-icon type="down" />
            </a-button>
          </a-dropdown>
          <a-button class="mgl12" type="primary" @click="exportResults" icon="download">导出结果</a-button>
        </a-col>
      </a-row>
    </a-card>
    
    <a-card class="card-shadow mgb16">
      <a-row type="flex" align="middle">
        <a-col :span="18">
          <a-input placeholder="请输入主机名" class="w180 mgr12" @change="(value) => getVal(value, 'hostname')" allowClear />
          <a-select placeholder="请选择组件类型" class="w180 mgr12" :allowClear="true" @change="(value) => getVal(value, 'componentType')">
            <a-select-option value="HDFS">HDFS</a-select-option>
            <a-select-option value="YARN">YARN</a-select-option>
            <a-select-option value="SPARK">Spark</a-select-option>
            <a-select-option value="HIVE">Hive</a-select-option>
            <a-select-option value="HBASE">HBase</a-select-option>
            <a-select-option value="ZOOKEEPER">Zookeeper</a-select-option>
            <a-select-option value="KAFKA">Kafka</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
          <a-select placeholder="请选择验证状态" class="w180 mgr12" :allowClear="true" @change="(value) => getVal(value, 'validated')">
            <a-select-option value="true">已验证</a-select-option>
            <a-select-option value="false">待验证</a-select-option>
          </a-select>
          <a-button type="primary" icon="search" @click="onSearch">搜索</a-button>
          <a-button class="mgl12" @click="resetSearch">重置</a-button>
        </a-col>
        <a-col :span="6" style="text-align: right">
          <a-badge :count="discoveryStats.validatedCount" :number-style="{ backgroundColor: '#52c41a' }" class="mgr16">
            <span class="stat-item">已验证</span>
          </a-badge>
          <a-badge :count="discoveryStats.pendingCount" :number-style="{ backgroundColor: '#faad14' }" class="mgr16">
            <span class="stat-item">待验证</span>
          </a-badge>
          <a-badge :count="discoveryStats.totalCount" :number-style="{ backgroundColor: '#1890ff' }">
            <span class="stat-item">总数</span>
          </a-badge>
        </a-col>
      </a-row>
    </a-card>
    
    <a-card class="card-shadow">
      <div class="table-info">
        <AdvanceTable
          :columns="columns"
          :dataSource="dataSource"
          :loading="loading"
          :pagination="pagination"
          :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
          rowKey="id"
          @search="handleSearch"
          @refresh="refreshResults"
          @change="handleTableChange"
        >
          <template slot="status" slot-scope="text, record">
            <a-tag :color="record.validated ? 'green' : 'orange'">
              {{ record.validated ? '已验证' : '待验证' }}
            </a-tag>
          </template>
          
          <template slot="discoveryStatus" slot-scope="text, record">
            <a-tag :color="getDiscoveryStatusColor(record.discoveryStatus)">
              {{ record.discoveryStatus }}
            </a-tag>
          </template>
          
          <template slot="configStatus" slot-scope="text, record">
            <span v-if="record.configStatus === 'COMPLETE'">
              <a-icon type="check-circle" theme="filled" style="color: #52c41a" />
              完整
            </span>
            <span v-else-if="record.configStatus === 'PARTIAL'">
              <a-icon type="exclamation-circle" theme="filled" style="color: #faad14" />
              部分
            </span>
            <span v-else>
              <a-icon type="close-circle" theme="filled" style="color: #ff4d4f" />
              缺失
            </span>
          </template>
          
          <template slot="action" slot-scope="text, record">
            <a-button type="link" @click="viewComponentDetail(record)" size="small">查看详情</a-button>
            <a-button type="link" @click="validateComponent(record)" size="small" v-if="!record.validated">验证</a-button>
            <a-button type="link" @click="importComponent(record)" size="small" v-if="record.validated">导入</a-button>
            <a-button type="link" @click="editComponent(record)" size="small">编辑</a-button>
            <a-button type="link" @click="deleteComponent(record)" size="small" danger>删除</a-button>
          </template>
        </AdvanceTable>
      </div>
    </a-card>
    
    <!-- 组件详情Modal -->
    <a-modal
      v-if="componentModalVisible"
      :title="currentComponent ? `组件详情 - ${currentComponent.componentType}` : '组件详情'"
      :visible="componentModalVisible"
      :maskClosable="false"
      :width="900"
      :footer="null"
      @cancel="closeComponentModal"
    >
      <div v-if="currentComponent">
        <a-tabs v-model="detailTabKey">
          <a-tab-pane key="basic" tab="基本信息">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="组件ID">{{ currentComponent.id }}</a-descriptions-item>
              <a-descriptions-item label="组件类型">{{ currentComponent.componentType }}</a-descriptions-item>
              <a-descriptions-item label="主机">{{ currentComponent.hostname }}</a-descriptions-item>
              <a-descriptions-item label="IP">{{ currentComponent.ip }}</a-descriptions-item>
              <a-descriptions-item label="版本">{{ currentComponent.version }}</a-descriptions-item>
              <a-descriptions-item label="端口">{{ currentComponent.port }}</a-descriptions-item>
              <a-descriptions-item label="安装路径">{{ currentComponent.installPath }}</a-descriptions-item>
              <a-descriptions-item label="配置文件路径">{{ currentComponent.configPath }}</a-descriptions-item>
              <a-descriptions-item label="验证状态">
                <a-tag :color="currentComponent.validated ? 'green' : 'orange'">
                  {{ currentComponent.validated ? '已验证' : '待验证' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="发现状态">{{ currentComponent.discoveryStatus }}</a-descriptions-item>
              <a-descriptions-item label="配置状态">{{ currentComponent.configStatus }}</a-descriptions-item>
              <a-descriptions-item label="发现时间">{{ currentComponent.discoveryTime }}</a-descriptions-item>
              <a-descriptions-item label="验证时间">{{ currentComponent.validationTime }}</a-descriptions-item>
            </a-descriptions>
          </a-tab-pane>
          <a-tab-pane key="config" tab="配置信息">
            <div v-if="currentComponent.configurations && currentComponent.configurations.length > 0">
              <a-table
                :columns="configColumns"
                :data-source="currentComponent.configurations"
                rowKey="key"
                size="small"
                :pagination="{ pageSize: 10 }"
              >
                <template slot="value" slot-scope="text, record">
                  <code>{{ record.value }}</code>
                </template>
              </a-table>
            </div>
            <div v-else class="empty-config">
              <a-empty description="暂无配置信息" />
            </div>
          </a-tab-pane>
          <a-tab-pane key="process" tab="进程信息">
            <div v-if="currentComponent.processes && currentComponent.processes.length > 0">
              <a-table
                :columns="processColumns"
                :data-source="currentComponent.processes"
                rowKey="pid"
                size="small"
                :pagination="{ pageSize: 10 }"
              >
                <template slot="status" slot-scope="text, record">
                  <a-tag :color="record.status === 'RUNNING' ? 'green' : 'red'">
                    {{ record.status }}
                  </a-tag>
                </template>
              </a-table>
            </div>
            <div v-else class="empty-process">
              <a-empty description="暂无进程信息" />
            </div>
          </a-tab-pane>
          <a-tab-pane key="validation" tab="验证结果">
            <div v-if="currentComponent.validationResult">
              <a-descriptions :column="1" bordered>
                <a-descriptions-item label="验证状态">
                  <a-tag :color="currentComponent.validationResult.success ? 'green' : 'red'">
                    {{ currentComponent.validationResult.success ? '成功' : '失败' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="验证时间">{{ currentComponent.validationResult.validationTime }}</a-descriptions-item>
                <a-descriptions-item label="验证消息">{{ currentComponent.validationResult.message }}</a-descriptions-item>
                <a-descriptions-item label="详细信息" v-if="currentComponent.validationResult.details">
                  <pre class="validation-details">{{ JSON.stringify(currentComponent.validationResult.details, null, 2) }}</pre>
                </a-descriptions-item>
              </a-descriptions>
            </div>
            <div v-else class="empty-validation">
              <a-empty description="未验证" />
            </div>
          </a-tab-pane>
        </a-tabs>
        
        <div class="detail-actions mgt16" style="text-align: right">
          <a-button @click="closeComponentModal">关闭</a-button>
          <a-button type="primary" @click="validateCurrentComponent" class="mgl8" v-if="!currentComponent.validated">验证</a-button>
          <a-button type="primary" @click="importCurrentComponent" class="mgl8" v-if="currentComponent.validated">导入</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { mapState } from "vuex";
import AdvanceTable from "@/components/table/advance/AdvanceTable";

export default {
  name: "ComponentDiscoveryResults",
  components: { AdvanceTable },
  data() {
    return {
      loading: false,
      taskId: null,
      taskData: {
        taskName: "",
      },
      dataSource: [],
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`,
      },
      searchParams: {},
      selectedRowKeys: [],
      columns: [
        { title: "ID", dataIndex: "id", key: "id", width: 80 },
        { title: "主机", dataIndex: "hostname", key: "hostname" },
        { title: "IP", dataIndex: "ip", key: "ip" },
        { title: "组件类型", dataIndex: "componentType", key: "componentType" },
        { title: "版本", dataIndex: "version", key: "version" },
        { title: "端口", dataIndex: "port", key: "port" },
        { title: "安装路径", dataIndex: "installPath", key: "installPath" },
        { title: "验证状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
        { title: "发现状态", dataIndex: "discoveryStatus", key: "discoveryStatus", scopedSlots: { customRender: "discoveryStatus" } },
        { title: "配置状态", dataIndex: "configStatus", key: "configStatus", scopedSlots: { customRender: "configStatus" } },
        { title: "操作", key: "action", scopedSlots: { customRender: "action" }, width: 200 },
      ],
      discoveryStats: {
        totalCount: 0,
        validatedCount: 0,
        pendingCount: 0,
      },
      componentModalVisible: false,
      currentComponent: null,
      detailTabKey: "basic",
      configColumns: [
        { title: "配置项", dataIndex: "key", key: "key" },
        { title: "配置值", dataIndex: "value", key: "value", scopedSlots: { customRender: "value" } },
        { title: "来源", dataIndex: "source", key: "source" },
        { title: "是否必需", dataIndex: "required", key: "required" },
      ],
      processColumns: [
        { title: "PID", dataIndex: "pid", key: "pid" },
        { title: "进程名", dataIndex: "name", key: "name" },
        { title: "启动命令", dataIndex: "command", key: "command" },
        { title: "状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
        { title: "启动时间", dataIndex: "startTime", key: "startTime" },
        { title: "CPU使用率", dataIndex: "cpuUsage", key: "cpuUsage" },
        { title: "内存使用率", dataIndex: "memoryUsage", key: "memoryUsage" },
      ],
    };
  },
  computed: {
    ...mapState({
      setting: (state) => state.setting,
    }),
    hasValidatedComponents() {
      return this.dataSource.some(item => item.validated);
    },
  },
  mounted() {
    this.taskId = this.$route.params.taskId;
    if (this.taskId) {
      this.loadTaskInfo();
      this.loadResults();
      this.loadDiscoveryStats();
    } else {
      this.$message.error("任务ID不存在");
      this.goBack();
    }
  },
  methods: {
    // 加载任务信息
    loadTaskInfo() {
      this.$axiosPost(global.API.componentDiscovery.getTaskDetail, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            this.taskData = res.data || {};
          }
        })
        .catch((error) => {
          console.error("加载任务信息失败:", error);
        });
    },
    
    // 加载发现结果
    loadResults() {
      this.loading = true;
      const params = {
        taskId: this.taskId,
        page: this.pagination.current,
        pageSize: this.pagination.pageSize,
        ...this.searchParams,
      };
      
      this.$axiosPost(global.API.componentDiscovery.getTaskResults, params)
        .then((res) => {
          if (res.code === 200) {
            this.dataSource = res.data.records || [];
            this.pagination.total = res.data.total || 0;
          } else {
            this.$message.error(res.message || "加载结果失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    // 加载发现统计
    loadDiscoveryStats() {
      this.$axiosPost(global.API.componentDiscovery.getTaskStats, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200) {
            this.discoveryStats = res.data || {};
          }
        })
        .catch((error) => {
          console.error("加载统计信息失败:", error);
        });
    },
    
    // 搜索处理
    getVal(value, field) {
      if (value === undefined || value === null || value === "") {
        delete this.searchParams[field];
      } else {
        this.searchParams[field] = value;
      }
    },
    
    onSearch() {
      this.pagination.current = 1;
      this.loadResults();
    },
    
    resetSearch() {
      this.searchParams = {};
      this.pagination.current = 1;
      this.loadResults();
    },
    
    // 表格变化处理
    handleTableChange(pagination, filters, sorter) {
      this.pagination.current = pagination.current;
      this.pagination.pageSize = pagination.pageSize;
      this.loadResults();
    },
    
    handleSearch(conditions) {
      this.searchParams = { ...this.searchParams, ...conditions };
      this.pagination.current = 1;
      this.loadResults();
    },
    
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys;
    },
    
    // 状态显示处理
    getDiscoveryStatusColor(status) {
      const colorMap = {
        DISCOVERED: "green",
        PARTIAL: "orange",
        FAILED: "red",
      };
      return colorMap[status] || "default";
    },
    
    // 操作按钮处理
    goBack() {
      this.$router.push(`/service-manage/component-discovery-detail/${this.taskId}`);
    },
    
    refreshResults() {
      this.loadResults();
      this.loadDiscoveryStats();
    },
    
    // 批量操作处理
    handleBatchMenuClick({ key }) {
      switch (key) {
        case "batchValidate":
          this.batchValidate();
          break;
        case "batchImport":
          this.batchImport();
          break;
        case "batchDelete":
          this.batchDelete();
          break;
      }
    },
    
    batchValidate() {
      if (this.selectedRowKeys.length === 0) {
        this.$message.warning("请先选择要验证的组件");
        return;
      }
      
      this.$confirm({
        title: "批量验证",
        content: `确定要验证选中的 ${this.selectedRowKeys.length} 个组件吗？`,
        onOk: () => {
          this.loading = true;
          this.$axiosPost(global.API.componentDiscovery.batchValidate, {
            taskId: this.taskId,
            resultIds: this.selectedRowKeys,
          })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success(`成功验证 ${res.data.successCount} 个组件`);
                this.selectedRowKeys = [];
                this.refreshResults();
              } else {
                this.$message.error(res.message || "批量验证失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            })
            .finally(() => {
              this.loading = false;
            });
        },
      });
    },
    
    batchImport() {
      // 获取已验证的组件ID
      const validatedIds = this.dataSource
        .filter(item => item.validated && this.selectedRowKeys.includes(item.id))
        .map(item => item.id);
      
      if (validatedIds.length === 0) {
        this.$message.warning("请先选择已验证的组件进行导入");
        return;
      }
      
      this.$confirm({
        title: "批量导入",
        content: `确定要导入选中的 ${validatedIds.length} 个已验证组件吗？`,
        onOk: () => {
          this.loading = true;
          this.$axiosPost(global.API.componentDiscovery.batchImport, {
            taskId: this.taskId,
            resultIds: validatedIds,
          })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success(`成功导入 ${res.data.importedCount} 个组件`);
                this.selectedRowKeys = [];
                this.refreshResults();
              } else {
                this.$message.error(res.message || "批量导入失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            })
            .finally(() => {
              this.loading = false;
            });
        },
      });
    },
    
    batchDelete() {
      if (this.selectedRowKeys.length === 0) {
        this.$message.warning("请先选择要删除的组件");
        return;
      }
      
      this.$confirm({
        title: "批量删除",
        content: `确定要删除选中的 ${this.selectedRowKeys.length} 个组件发现结果吗？此操作不可恢复。`,
        okType: "danger",
        onOk: () => {
          this.loading = true;
          this.$axiosPost(global.API.componentDiscovery.batchDelete, {
            taskId: this.taskId,
            resultIds: this.selectedRowKeys,
          })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success(`成功删除 ${res.data.deletedCount} 个组件`);
                this.selectedRowKeys = [];
                this.refreshResults();
              } else {
                this.$message.error(res.message || "批量删除失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            })
            .finally(() => {
              this.loading = false;
            });
        },
      });
    },
    
    exportResults() {
      this.$axiosPost(global.API.componentDiscovery.exportResults, { taskId: this.taskId })
        .then((res) => {
          if (res.code === 200 && res.data) {
            // 创建下载链接
            const link = document.createElement("a");
            link.href = res.data;
            link.download = `discovery-task-${this.taskId}-results.json`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            this.$message.success("结果导出成功");
          } else {
            this.$message.error(res.message || "导出结果失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        });
    },
    
    // 单个组件操作
    viewComponentDetail(record) {
      this.currentComponent = record;
      this.detailTabKey = "basic";
      this.componentModalVisible = true;
    },
    
    closeComponentModal() {
      this.componentModalVisible = false;
      this.currentComponent = null;
    },
    
    validateComponent(record) {
      this.$axiosPost(global.API.componentDiscovery.validateComponent, { resultId: record.id })
        .then((res) => {
          if (res.code === 200) {
            this.$message.success("组件验证成功");
            this.refreshResults();
          } else {
            this.$message.error(res.message || "组件验证失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        });
    },
    
    validateCurrentComponent() {
      if (this.currentComponent) {
        this.validateComponent(this.currentComponent);
        this.closeComponentModal();
      }
    },
    
    importComponent(record) {
      if (!record.validated) {
        this.$message.warning("请先验证组件再导入");
        return;
      }
      
      this.$confirm({
        title: "导入组件",
        content: `确定要将组件 ${record.componentType} (${record.hostname}:${record.port}) 导入到平台吗？`,
        onOk: () => {
          this.$axiosPost(global.API.componentDiscovery.importComponent, { resultId: record.id })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("组件导入成功");
                this.refreshResults();
              } else {
                this.$message.error(res.message || "组件导入失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },
    
    importCurrentComponent() {
      if (this.currentComponent) {
        this.importComponent(this.currentComponent);
        this.closeComponentModal();
      }
    },
    
    editComponent(record) {
      // 打开编辑组件对话框
      this.$message.info("编辑功能开发中");
    },
    
    deleteComponent(record) {
      this.$confirm({
        title: "删除组件",
        content: `确定要删除组件 ${record.componentType} (${record.hostname}) 的发现结果吗？`,
        okType: "danger",
        onOk: () => {
          this.$axiosPost(global.API.componentDiscovery.deleteResult, { resultId: record.id })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("组件删除成功");
                this.refreshResults();
              } else {
                this.$message.error(res.message || "组件删除失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },
  },
};
</script>

<style lang="less" scoped>
.component-discovery-results {
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
  
  .mgr12 {
    margin-right: 12px;
  }
  
  .mgr16 {
    margin-right: 16px;
  }
  
  .mgl12 {
    margin-left: 12px;
  }
  
  .mgt16 {
    margin-top: 16px;
  }
  
  .mgb16 {
    margin-bottom: 16px;
  }
  
  .w180 {
    width: 180px;
  }
  
  .stat-item {
    padding: 8px 16px;
    background: #fafafa;
    border-radius: 4px;
    border: 1px solid #e8e8e8;
    font-size: 14px;
  }
  
  .empty-config,
  .empty-process,
  .empty-validation {
    text-align: center;
    padding: 40px 0;
  }
  
  .validation-details {
    background: #f6f8fa;
    padding: 12px;
    border-radius: 4px;
    border: 1px solid #e1e4e8;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 12px;
    line-height: 1.5;
    max-height: 300px;
    overflow: auto;
    margin: 0;
  }
  
  .detail-actions {
    border-top: 1px solid #e8e8e8;
    padding-top: 16px;
  }
}
</style>