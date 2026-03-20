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


 * @Date: 2026-03-17 01:50:00
 * @LastEditTime: 2026-03-17 01:50:00
 * @FilePath: \datasophon-ui\src\pages\componentDiscovery\index.vue
-->
<template>
  <div class="component-discovery card-shadow">
    <a-card class="mgb16 card-shadow">
      <a-row type="flex" align="middle">
        <a-col :span="16">
          <a-input placeholder="请输入任务名称" class="w180 mgr12" @change="(value) => getVal(value, 'taskName')" allowClear />
          <a-select placeholder="请选择任务状态" class="w180 mgr12" :allowClear="true" @change="(value) => getVal(value, 'status')">
            <a-select-option value="RUNNING">运行中</a-select-option>
            <a-select-option value="SUCCESS">成功</a-select-option>
            <a-select-option value="FAILED">失败</a-select-option>
            <a-select-option value="PENDING">待执行</a-select-option>
          </a-select>
          <a-select placeholder="请选择组件类型" class="w180 mgr12" :allowClear="true" @change="(value) => getVal(value, 'componentType')">
            <a-select-option value="HDFS">HDFS</a-select-option>
            <a-select-option value="YARN">YARN</a-select-option>
            <a-select-option value="SPARK">Spark</a-select-option>
            <a-select-option value="HIVE">Hive</a-select-option>
            <a-select-option value="HBASE">HBase</a-select-option>
            <a-select-option value="ZOOKEEPER">Zookeeper</a-select-option>
            <a-select-option value="KAFKA">Kafka</a-select-option>
          </a-select>
          <a-button type="primary" icon="search" @click="onSearch">搜索</a-button>
          <a-button class="mgl12" @click="resetSearch">重置</a-button>
        </a-col>
        <a-col :span="8" style="text-align: right">
          <a-button type="primary" @click="startNewDiscovery" icon="plus">新建发现任务</a-button>
          <a-button class="mgl12" @click="refreshList" icon="reload">刷新</a-button>
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
          rowKey="id"
          @search="handleSearch"
          @refresh="refreshList"
          @change="handleTableChange"
        >
          <template slot="status" slot-scope="text, record">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          
          <template slot="action" slot-scope="text, record">
            <a-button type="link" @click="viewDetails(record)" size="small">查看详情</a-button>
            <a-button type="link" @click="viewResults(record)" size="small" v-if="record.status === 'SUCCESS'">查看结果</a-button>
            <a-button type="link" @click="stopDiscovery(record)" size="small" v-if="record.status === 'RUNNING'">停止</a-button>
            <a-button type="link" @click="retryDiscovery(record)" size="small" v-if="record.status === 'FAILED'">重试</a-button>
            <a-button type="link" @click="deleteDiscovery(record)" size="small" danger>删除</a-button>
          </template>
        </AdvanceTable>
      </div>
    </a-card>
    
    <!-- 新建发现任务Modal -->
    <a-modal
      v-if="discoveryModalVisible"
      title="新建组件发现任务"
      :visible="discoveryModalVisible"
      :maskClosable="false"
      :width="800"
      :confirm-loading="modalConfirmLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :form="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="任务名称">
          <a-input
            v-decorator="['taskName', { rules: [{ required: true, message: '请输入任务名称' }] }]"
            placeholder="请输入任务名称"
          />
        </a-form-item>
        <a-form-item label="目标集群">
          <a-select
            v-decorator="['clusterId', { rules: [{ required: true, message: '请选择目标集群' }] }]"
            placeholder="请选择目标集群"
            @change="handleClusterChange"
          >
            <a-select-option v-for="cluster in clusterList" :key="cluster.id" :value="cluster.id">
              {{ cluster.clusterName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="发现策略">
          <a-select
            v-decorator="['discoveryStrategy', { rules: [{ required: true, message: '请选择发现策略' }] }]"
            placeholder="请选择发现策略"
          >
            <a-select-option value="SSH_SCAN">SSH扫描</a-select-option>
            <a-select-option value="PORT_SCAN">端口扫描</a-select-option>
            <a-select-option value="CONFIG_SCAN">配置扫描</a-select-option>
            <a-select-option value="METADATA_QUERY">元数据查询</a-select-option>
            <a-select-option value="COMPREHENSIVE">综合发现</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标主机">
          <a-select
            v-decorator="['targetHosts', { rules: [{ required: true, message: '请选择目标主机' }] }]"
            placeholder="请选择目标主机"
            mode="multiple"
            :disabled="!selectedClusterId"
          >
            <a-select-option v-for="host in hostList" :key="host.id" :value="host.hostname">
              {{ host.hostname }} ({{ host.ip }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="超时时间(秒)">
          <a-input-number
            v-decorator="['timeoutSeconds', { initialValue: 300 }]"
            :min="60"
            :max="3600"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="并发数">
          <a-input-number
            v-decorator="['concurrentThreads', { initialValue: 5 }]"
            :min="1"
            :max="20"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 组件发现向导 -->
    <a-modal
      v-if="wizardVisible"
      title="新建组件发现任务"
      :visible="wizardVisible"
      :maskClosable="false"
      :width="1000"
      :footer="null"
      @cancel="handleWizardCancel"
      :destroyOnClose="true"
    >
      <DiscoveryWizard
        :visible="wizardVisible"
        @cancel="handleWizardCancel"
        @success="handleWizardSuccess"
      />
    </a-modal>
  </div>
</template>

<script>
import { mapActions, mapState } from "vuex";
import AdvanceTable from "@/components/table/advance/AdvanceTable";
import DiscoveryWizard from "./wizard/index.vue";
import ComponentDiscoveryService from "@/api/services/ComponentDiscoveryService";

export default {
  name: "ComponentDiscovery",
  components: { AdvanceTable, DiscoveryWizard },
  data() {
    return {
      loading: false,
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
    columns: [
      { title: "任务ID", dataIndex: "id", key: "id", width: 100 },
      { title: "任务名称", dataIndex: "taskName", key: "taskName" },
      { title: "集群", dataIndex: "clusterName", key: "clusterName" },
      { title: "组件类型", dataIndex: "componentType", key: "componentType" },
      { title: "发现策略", dataIndex: "discoveryStrategy", key: "discoveryStrategy" },
      { title: "状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
      { title: "创建时间", dataIndex: "createTime", key: "createTime" },
      { title: "完成时间", dataIndex: "completeTime", key: "completeTime" },
      { title: "操作", key: "action", scopedSlots: { customRender: "action" }, width: 200 },
    ],
    discoveryModalVisible: false,
    wizardVisible: false,
    modalConfirmLoading: false,
    form: this.$form.createForm(this),
    clusterList: [],
    hostList: [],
    selectedClusterId: null,
    componentDiscoveryService: new ComponentDiscoveryService(),
  };
  },
  computed: {
    ...mapState({
      setting: (state) => state.setting,
    }),
  },
  mounted() {
    this.loadDiscoveryTasks();
    this.loadClusterList();
  },
  methods: {
    // 加载发现任务列表
    loadDiscoveryTasks() {
      this.loading = true;
      const params = {
        page: this.pagination.current,
        pageSize: this.pagination.pageSize,
        ...this.searchParams,
      };
      
      this.componentDiscoveryService.listTasks(params)
        .then((res) => {
          if (res.code === 200) {
            this.dataSource = res.data.records || [];
            this.pagination.total = res.data.total || 0;
          } else {
            this.$message.error(res.message || "加载任务列表失败");
          }
        })
        .catch((error) => {
          this.$message.error("请求失败: " + error.message);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    // 加载集群列表
    loadClusterList() {
       this.$axiosJsonPost(global.API.cluster.list, {})
        .then((res) => {
          if (res.code === 200) {
            this.clusterList = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载集群列表失败:", error);
        });
    },
    
    // 加载主机列表
    loadHostList(clusterId) {
      if (!clusterId) {
        this.hostList = [];
        return;
      }
      
       this.$axiosJsonPost(global.API.host.getHostListByClusterId, { clusterId })
        .then((res) => {
          if (res.code === 200) {
            this.hostList = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载主机列表失败:", error);
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
      this.loadDiscoveryTasks();
    },
    
    resetSearch() {
      this.searchParams = {};
      this.pagination.current = 1;
      this.loadDiscoveryTasks();
    },
    
    // 表格变化处理
    handleTableChange(pagination, filters, sorter) {
      this.pagination.current = pagination.current;
      this.pagination.pageSize = pagination.pageSize;
      this.loadDiscoveryTasks();
    },
    
    handleSearch(conditions) {
      this.searchParams = { ...this.searchParams, ...conditions };
      this.pagination.current = 1;
      this.loadDiscoveryTasks();
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
    startNewDiscovery() {
      this.wizardVisible = true;
    },
    
    handleClusterChange(clusterId) {
      this.selectedClusterId = clusterId;
      this.loadHostList(clusterId);
    },
    
    handleModalOk() {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.modalConfirmLoading = true;
          
          // 转换主机名为主机ID列表
          const hostIds = this.hostList
            .filter(host => values.targetHosts.includes(host.hostname))
            .map(host => host.id);
          
          const params = {
            ...values,
            targetHostIds: hostIds,
          };
          
          this.componentDiscoveryService.startTask(params)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("发现任务创建成功");
                this.discoveryModalVisible = false;
                this.loadDiscoveryTasks();
              } else {
                this.$message.error(res.message || "创建任务失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            })
            .finally(() => {
              this.modalConfirmLoading = false;
            });
        }
      });
    },
    
    handleModalCancel() {
      this.discoveryModalVisible = false;
    },
    
    refreshList() {
      this.loadDiscoveryTasks();
    },
    
    viewDetails(record) {
      this.$router.push({
        path: `/service-manage/component-discovery-detail/${record.id}`,
      });
    },
    
    viewResults(record) {
      this.$router.push({
        path: `/service-manage/component-discovery-results/${record.id}`,
      });
    },
    
    stopDiscovery(record) {
      this.$confirm({
        title: "确认停止",
        content: `确定要停止发现任务 "${record.taskName}" 吗？`,
        onOk: () => {
          this.componentDiscoveryService.stopTask({ taskId: record.id })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("任务已停止");
                this.loadDiscoveryTasks();
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

    retryDiscovery(record) {
      this.$confirm({
        title: "确认重试",
        content: `确定要重试发现任务 "${record.taskName}" 吗？`,
        onOk: () => {
          this.componentDiscoveryService.retryTask({ taskId: record.id })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("任务已重试");
                this.loadDiscoveryTasks();
              } else {
                this.$message.error(res.message || "重试任务失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },

    deleteDiscovery(record) {
      this.$confirm({
        title: "确认删除",
        content: `确定要删除发现任务 "${record.taskName}" 吗？删除后无法恢复。`,
        onOk: () => {
          this.componentDiscoveryService.deleteTask({ taskId: record.id })
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("任务已删除");
                this.loadDiscoveryTasks();
              } else {
                this.$message.error(res.message || "删除任务失败");
              }
            })
            .catch((error) => {
              this.$message.error("请求失败: " + error.message);
            });
        },
      });
    },
  
  handleWizardSuccess(taskData) {
      this.wizardVisible = false;
      this.$message.success("发现任务创建成功");
      this.loadDiscoveryTasks();
    },
    
    handleWizardCancel() {
      this.wizardVisible = false;
    },
  },
};
</script>

<style lang="less" scoped>
.component-discovery {
  background: #fff;
  padding: 20px;
  
  .w180 {
    width: 180px;
  }
  
  .mgr12 {
    margin-right: 12px;
  }
  
  .mgl12 {
    margin-left: 12px;
  }
  
  .mgb16 {
    margin-bottom: 16px;
  }
}
</style>