<template>
  <div class="configuration-management-page">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <h2>配置管理</h2>
      <div class="header-actions">
        <a-input-search
          placeholder="搜索组件名称、集群或配置项"
          style="width: 300px; margin-right: 16px;"
          @search="handleSearch"
        />
        <a-button type="primary" @click="handleRefresh">
          <a-icon type="sync" :spin="loading" />
          刷新
        </a-button>
      </div>
    </div>

    <!-- 配置对比表格 -->
    <div class="config-table-wrapper">
      <a-table
        :columns="columns"
        :dataSource="dataSource"
        :pagination="pagination"
        :loading="loading"
        rowKey="id"
        @change="handleTableChange"
      >
        <!-- 组件名称列 -->
        <template slot="componentName" slot-scope="text, record">
          <a-tooltip :title="`组件ID: ${record.componentId}`">
            <span>{{ text }}</span>
          </a-tooltip>
        </template>

        <!-- 集群名称列 -->
        <template slot="clusterName" slot-scope="text, record">
          <a-tag color="blue">{{ text }}</a-tag>
        </template>

        <!-- 配置状态列 -->
        <template slot="configStatus" slot-scope="text, record">
          <a-tag :color="getStatusColor(text)">
            {{ getStatusText(text) }}
          </a-tag>
        </template>

        <!-- 同步状态列 -->
        <template slot="syncStatus" slot-scope="text, record">
          <a-tag :color="getSyncStatusColor(text)">
            {{ getSyncStatusText(text) }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template slot="action" slot-scope="text, record">
          <a-button-group>
            <a-button size="small" @click="handleViewConfig(record)">
              <a-icon type="eye" />
              查看
            </a-button>
            <a-button size="small" type="primary" @click="handleCompareConfig(record)" :disabled="record.configStatus !== 'DIFFERENT'">
              <a-icon type="swap" />
              对比
            </a-button>
            <a-button size="small" type="danger" @click="handleSyncConfig(record)" :disabled="record.syncStatus === 'SYNCING'">
              <a-icon type="sync" :spin="record.syncStatus === 'SYNCING'" />
              同步
            </a-button>
            <a-button size="small" type="dashed" @click="handleViewHistory(record)">
              <a-icon type="history" />
              历史
            </a-button>
          </a-button-group>
        </template>
      </a-table>
    </div>

    <!-- 配置对比模态框 -->
    <a-modal
      v-model="compareModalVisible"
      title="配置对比"
      width="90%"
      :footer="null"
      @cancel="handleCompareModalCancel"
    >
      <div v-if="selectedConfig" class="config-compare-modal">
        <div class="config-compare-header">
          <a-descriptions title="配置信息" bordered :column="3">
            <a-descriptions-item label="组件">
              {{ selectedConfig.componentName }}
            </a-descriptions-item>
            <a-descriptions-item label="集群">
              {{ selectedConfig.clusterName }}
            </a-descriptions-item>
            <a-descriptions-item label="配置状态">
              <a-tag :color="getStatusColor(selectedConfig.configStatus)">
                {{ getStatusText(selectedConfig.configStatus) }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="config-compare-content">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="config-section">
                <h3>现有组件配置</h3>
                <pre class="config-content">{{ selectedConfig.existingConfig || '无配置信息' }}</pre>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="config-section">
                <h3>DataSophon配置</h3>
                <pre class="config-content">{{ selectedConfig.datasophonConfig || '无配置信息' }}</pre>
              </div>
            </a-col>
          </a-row>

          <div class="config-actions" style="margin-top: 20px; text-align: center;">
            <a-button type="primary" @click="handleSyncToDatasophon" style="margin-right: 16px;">
              同步到DataSophon
            </a-button>
            <a-button type="danger" @click="handleSyncToExisting">
              同步到现有组件
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 配置历史模态框 -->
    <a-modal
      v-model="historyModalVisible"
      title="配置历史"
      width="70%"
      :footer="null"
    >
      <div v-if="selectedConfig" class="config-history-modal">
        <a-timeline>
          <a-timeline-item v-for="history in configHistory" :key="history.id">
            <span slot="dot" :style="{ color: history.type === 'SYNC' ? '#52c41a' : '#1890ff' }">
              <a-icon :type="history.type === 'SYNC' ? 'sync' : 'edit'" />
            </span>
            <a-card size="small">
              <template slot="title">
                <span>{{ history.operator }}</span>
                <a-tag :color="history.type === 'SYNC' ? 'green' : 'blue'" style="margin-left: 8px;">
                  {{ history.type === 'SYNC' ? '同步操作' : '修改操作' }}
                </a-tag>
                <span style="float: right; color: #999; font-size: 12px;">
                  {{ history.timestamp }}
                </span>
              </template>
              <div class="history-content">
                <p><strong>操作描述:</strong> {{ history.description }}</p>
                <p><strong>配置变更:</strong></p>
                <pre class="history-config">{{ history.configChange || '无变更详情' }}</pre>
              </div>
            </a-card>
          </a-timeline-item>
        </a-timeline>
      </div>
    </a-modal>
  </div>
</template>

<script>
import ConfigurationManagementService from "@/api/services/ConfigurationManagementService";

export default {
  name: "ConfigurationManagement",
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
        { title: "组件名称", dataIndex: "componentName", key: "componentName", scopedSlots: { customRender: "componentName" } },
        { title: "集群", dataIndex: "clusterName", key: "clusterName", scopedSlots: { customRender: "clusterName" } },
        { title: "服务类型", dataIndex: "serviceType", key: "serviceType" },
        { title: "配置状态", dataIndex: "configStatus", key: "configStatus", scopedSlots: { customRender: "configStatus" } },
        { title: "同步状态", dataIndex: "syncStatus", key: "syncStatus", scopedSlots: { customRender: "syncStatus" } },
        { title: "最后同步时间", dataIndex: "lastSyncTime", key: "lastSyncTime" },
        { title: "操作", key: "action", scopedSlots: { customRender: "action" }, width: 300 },
      ],
      compareModalVisible: false,
      historyModalVisible: false,
      selectedConfig: null,
      configHistory: [],
      configurationManagementService: new ConfigurationManagementService(),
    };
  },
  created() {
    this.loadConfigurations();
  },
  methods: {
    // 加载配置列表
    async loadConfigurations() {
      this.loading = true;
      try {
        const params = {
          ...this.searchParams,
          page: this.pagination.current,
          pageSize: this.pagination.pageSize,
        };
        const response = await this.configurationManagementService.listConfigurations(params);
        this.dataSource = response.data || [];
        this.pagination.total = response.total || 0;
      } catch (error) {
        console.error("加载配置列表失败:", error);
        this.$message.error("加载配置列表失败");
      } finally {
        this.loading = false;
      }
    },

    // 处理搜索
    handleSearch(value) {
      this.searchParams.keyword = value;
      this.pagination.current = 1;
      this.loadConfigurations();
    },

    // 处理表格变化
    handleTableChange(pagination) {
      this.pagination = pagination;
      this.loadConfigurations();
    },

    // 刷新数据
    handleRefresh() {
      this.loadConfigurations();
    },

    // 查看配置
    handleViewConfig(record) {
      console.log("查看配置:", record);
      // TODO: 实现查看配置详情功能
    },

    // 对比配置
    handleCompareConfig(record) {
      this.selectedConfig = record;
      this.compareModalVisible = true;
      // TODO: 加载详细的配置对比信息
    },

    // 同步配置
    async handleSyncConfig(record) {
      this.$confirm({
        title: "确认同步配置",
        content: `确定要同步 ${record.componentName} 的配置吗？`,
        onOk: async () => {
          try {
            record.syncStatus = "SYNCING";
            await this.configurationManagementService.syncConfiguration({
              configId: record.id,
              syncDirection: "BIDIRECTIONAL",
            });
            this.$message.success("配置同步成功");
            this.loadConfigurations();
          } catch (error) {
            console.error("配置同步失败:", error);
            this.$message.error("配置同步失败");
          } finally {
            record.syncStatus = "IDLE";
          }
        },
      });
    },

    // 查看历史
    async handleViewHistory(record) {
      this.selectedConfig = record;
      try {
        const response = await this.configurationManagementService.getConfigHistory({
          configId: record.id,
        });
        this.configHistory = response.data || [];
        this.historyModalVisible = true;
      } catch (error) {
        console.error("加载配置历史失败:", error);
        this.$message.error("加载配置历史失败");
      }
    },

    // 状态颜色
    getStatusColor(status) {
      const colors = {
        IDENTICAL: "green",
        DIFFERENT: "orange",
        UNKNOWN: "gray",
        ERROR: "red",
      };
      return colors[status] || "gray";
    },

    getStatusText(status) {
      const texts = {
        IDENTICAL: "一致",
        DIFFERENT: "不一致",
        UNKNOWN: "未知",
        ERROR: "错误",
      };
      return texts[status] || status;
    },

    // 同步状态颜色
    getSyncStatusColor(status) {
      const colors = {
        IDLE: "blue",
        SYNCING: "purple",
        SUCCESS: "green",
        FAILED: "red",
      };
      return colors[status] || "gray";
    },

    getSyncStatusText(status) {
      const texts = {
        IDLE: "未同步",
        SYNCING: "同步中",
        SUCCESS: "同步成功",
        FAILED: "同步失败",
      };
      return texts[status] || status;
    },

    // 同步到DataSophon
    async handleSyncToDatasophon() {
      if (!this.selectedConfig) return;
      try {
        await this.configurationManagementService.syncConfiguration({
          configId: this.selectedConfig.id,
          syncDirection: "TO_DATASOPHON",
        });
        this.$message.success("配置已同步到DataSophon");
        this.compareModalVisible = false;
        this.loadConfigurations();
      } catch (error) {
        console.error("同步到DataSophon失败:", error);
        this.$message.error("同步到DataSophon失败");
      }
    },

    // 同步到现有组件
    async handleSyncToExisting() {
      if (!this.selectedConfig) return;
      try {
        await this.configurationManagementService.syncConfiguration({
          configId: this.selectedConfig.id,
          syncDirection: "TO_EXISTING",
        });
        this.$message.success("配置已同步到现有组件");
        this.compareModalVisible = false;
        this.loadConfigurations();
      } catch (error) {
        console.error("同步到现有组件失败:", error);
        this.$message.error("同步到现有组件失败");
      }
    },

    // 关闭对比模态框
    handleCompareModalCancel() {
      this.compareModalVisible = false;
      this.selectedConfig = null;
    },
  },
};
</script>

<style scoped>
.configuration-management-page {
  padding: 24px;
  background: #fff;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
}

.config-table-wrapper {
  margin-top: 16px;
}

.config-compare-modal .config-section {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 16px;
  height: 500px;
  overflow-y: auto;
}

.config-compare-modal .config-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.config-compare-modal .config-content {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.history-content {
  font-size: 14px;
}

.history-config {
  margin: 8px 0;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>