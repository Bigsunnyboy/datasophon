<template>
  <div class="operations-management-page">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <h2>运维控制台</h2>
      <div class="header-actions">
        <a-input-search
          placeholder="搜索组件名称、集群或主机"
          style="width: 300px; margin-right: 16px;"
          @search="handleSearch"
        />
        <a-button type="primary" @click="handleRefresh" :loading="loading">
          <a-icon type="sync" :spin="loading" />
          刷新
        </a-button>
        <a-button type="dashed" @click="handleRunHealthCheck" style="margin-left: 8px;">
          <a-icon type="heart" />
          健康检查
        </a-button>
        <a-button type="dashed" @click="handleShowBulkOperations" style="margin-left: 8px;">
          <a-icon type="tool" />
          批量操作
        </a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards" style="margin-bottom: 24px;">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card size="small" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #1890ff;">
                <a-icon type="check-circle" style="color: white; font-size: 24px;" />
              </div>
              <div class="stat-info">
                <div class="stat-title">健康服务</div>
                <div class="stat-value">{{ stats.healthy || 0 }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #ff4d4f;">
                <a-icon type="exclamation-circle" style="color: white; font-size: 24px;" />
              </div>
              <div class="stat-info">
                <div class="stat-title">异常服务</div>
                <div class="stat-value">{{ stats.unhealthy || 0 }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #faad14;">
                <a-icon type="loading" style="color: white; font-size: 24px;" />
              </div>
              <div class="stat-info">
                <div class="stat-title">运行中</div>
                <div class="stat-value">{{ stats.running || 0 }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #52c41a;">
                <a-icon type="deployment-unit" style="color: white; font-size: 24px;" />
              </div>
              <div class="stat-info">
                <div class="stat-title">已接管</div>
                <div class="stat-value">{{ stats.takenOver || 0 }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 服务状态表格 -->
    <div class="service-table-wrapper">
      <a-table
        :columns="columns"
        :dataSource="dataSource"
        :pagination="pagination"
        :loading="loading"
        rowKey="id"
        @change="handleTableChange"
        :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      >
        <!-- 服务名称列 -->
        <template slot="serviceName" slot-scope="text, record">
          <div class="service-name-cell">
            <a-icon :type="getServiceIcon(record.serviceType)" style="margin-right: 8px;" />
            <span>{{ text }}</span>
            <a-tag v-if="record.takenOver" color="green" style="margin-left: 8px;">已接管</a-tag>
          </div>
        </template>

        <!-- 集群列 -->
        <template slot="clusterName" slot-scope="text, record">
          <a-tag color="blue">{{ text }}</a-tag>
        </template>

        <!-- 状态列 -->
        <template slot="status" slot-scope="text, record">
          <a-badge :status="getStatusBadge(text)" :text="getStatusText(text)" />
        </template>

        <!-- 健康状态列 -->
        <template slot="healthStatus" slot-scope="text, record">
          <a-tag :color="getHealthColor(text)">
            {{ getHealthText(text) }}
          </a-tag>
        </template>

        <!-- 接管级别列 -->
        <template slot="takeoverLevel" slot-scope="text, record">
          <a-tag :color="getTakeoverLevelColor(text)">
            {{ getTakeoverLevelText(text) }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template slot="action" slot-scope="text, record">
          <a-dropdown :trigger="['click']">
            <a-button size="small">
              操作 <a-icon type="down" />
            </a-button>
            <a-menu slot="overlay">
              <a-menu-item @click="handleStartService(record)" :disabled="record.status === 'RUNNING'">
                <a-icon type="play-circle" />
                启动
              </a-menu-item>
              <a-menu-item @click="handleStopService(record)" :disabled="record.status !== 'RUNNING'">
                <a-icon type="pause-circle" />
                停止
              </a-menu-item>
              <a-menu-item @click="handleRestartService(record)">
                <a-icon type="reload" />
                重启
              </a-menu-item>
              <a-menu-item @click="handleReloadConfig(record)">
                <a-icon type="sync" />
                重载配置
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item @click="handleCheckHealth(record)">
                <a-icon type="heart" />
                健康检查
              </a-menu-item>
              <a-menu-item @click="handleViewLogs(record)">
                <a-icon type="file-text" />
                查看日志
              </a-menu-item>
              <a-menu-item @click="handleViewMetrics(record)">
                <a-icon type="dashboard" />
                查看指标
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item @click="handleChangeTakeoverLevel(record)">
                <a-icon type="swap" />
                变更接管级别
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </template>
      </a-table>
    </div>

    <!-- 批量操作模态框 -->
    <a-modal
      v-model="bulkOperationsModalVisible"
      title="批量操作"
      @ok="handleBulkOperationsOk"
      @cancel="handleBulkOperationsCancel"
    >
      <div class="bulk-operations-modal">
        <a-form layout="vertical">
          <a-form-item label="选择操作">
            <a-select v-model="bulkOperationType" placeholder="请选择操作类型">
              <a-select-option value="START">启动服务</a-select-option>
              <a-select-option value="STOP">停止服务</a-select-option>
              <a-select-option value="RESTART">重启服务</a-select-option>
              <a-select-option value="RELOAD_CONFIG">重载配置</a-select-option>
              <a-select-option value="HEALTH_CHECK">健康检查</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="目标服务">
            <div class="selected-services">
              <a-tag v-for="service in selectedServices" :key="service.id" closable @close="removeSelectedService(service.id)">
                {{ service.serviceName }} ({{ service.clusterName }})
              </a-tag>
              <div v-if="selectedServices.length === 0" style="color: #999;">
                已选择 {{ selectedRowKeys.length }} 个服务
              </div>
            </div>
          </a-form-item>
          <a-form-item v-if="bulkOperationType === 'HEALTH_CHECK'" label="检查类型">
            <a-select v-model="bulkHealthCheckType" placeholder="请选择检查类型">
              <a-select-option value="BASIC">基础检查</a-select-option>
              <a-select-option value="COMPREHENSIVE">全面检查</a-select-option>
              <a-select-option value="PERFORMANCE">性能检查</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 健康检查结果模态框 -->
    <a-modal
      v-model="healthCheckModalVisible"
      title="健康检查结果"
      width="70%"
      :footer="null"
    >
      <div v-if="healthCheckResult" class="health-check-modal">
        <a-descriptions title="检查概览" bordered :column="3">
          <a-descriptions-item label="服务">
            {{ healthCheckResult.serviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="集群">
            {{ healthCheckResult.clusterName }}
          </a-descriptions-item>
          <a-descriptions-item label="总体状态">
            <a-tag :color="healthCheckResult.overallStatus === 'HEALTHY' ? 'green' : 'red'">
              {{ healthCheckResult.overallStatus === 'HEALTHY' ? '健康' : '异常' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>

        <div class="health-check-details" style="margin-top: 20px;">
          <h3>检查详情</h3>
          <a-table
            :columns="healthCheckColumns"
            :dataSource="healthCheckResult.details || []"
            size="small"
            rowKey="checkItem"
          >
            <template slot="status" slot-scope="text">
              <a-tag :color="text === 'PASS' ? 'green' : text === 'WARNING' ? 'orange' : 'red'">
                {{ text === 'PASS' ? '通过' : text === 'WARNING' ? '警告' : '失败' }}
              </a-tag>
            </template>
          </a-table>
        </div>
      </div>
    </a-modal>

    <!-- 日志查看模态框 -->
    <a-modal
      v-model="logViewModalVisible"
      title="服务日志"
      width="80%"
      :footer="null"
    >
      <div v-if="selectedLogService" class="log-view-modal">
        <a-descriptions title="日志信息" bordered :column="3">
          <a-descriptions-item label="服务">
            {{ selectedLogService.serviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="集群">
            {{ selectedLogService.clusterName }}
          </a-descriptions-item>
          <a-descriptions-item label="日志文件">
            <a-select v-model="selectedLogFile" style="width: 200px;" @change="handleLogFileChange">
              <a-select-option value="application.log">应用日志</a-select-option>
              <a-select-option value="error.log">错误日志</a-select-option>
              <a-select-option value="access.log">访问日志</a-select-option>
              <a-select-option value="system.log">系统日志</a-select-option>
            </a-select>
          </a-descriptions-item>
        </a-descriptions>

        <div class="log-content" style="margin-top: 20px;">
          <div class="log-toolbar">
            <a-button size="small" @click="handleRefreshLogs">
              <a-icon type="sync" />
              刷新
            </a-button>
            <a-button size="small" @click="handleDownloadLogs" style="margin-left: 8px;">
              <a-icon type="download" />
              下载
            </a-button>
            <a-input-search
              placeholder="搜索日志内容"
              style="width: 200px; margin-left: 8px;"
              @search="handleSearchLogs"
            />
          </div>
          <pre class="log-text">{{ logContent || '加载中...' }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import OperationsManagementService from "@/api/services/OperationsManagementService";

export default {
  name: "OperationsManagement",
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
      selectedRowKeys: [],
      selectedServices: [],
      columns: [
        { title: "服务名称", dataIndex: "serviceName", key: "serviceName", scopedSlots: { customRender: "serviceName" } },
        { title: "集群", dataIndex: "clusterName", key: "clusterName", scopedSlots: { customRender: "clusterName" } },
        { title: "服务类型", dataIndex: "serviceType", key: "serviceType" },
        { title: "状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
        { title: "健康状态", dataIndex: "healthStatus", key: "healthStatus", scopedSlots: { customRender: "healthStatus" } },
        { title: "接管级别", dataIndex: "takeoverLevel", key: "takeoverLevel", scopedSlots: { customRender: "takeoverLevel" } },
        { title: "最后检查时间", dataIndex: "lastCheckTime", key: "lastCheckTime" },
        { title: "操作", key: "action", scopedSlots: { customRender: "action" }, width: 150 },
      ],
      stats: {
        healthy: 0,
        unhealthy: 0,
        running: 0,
        takenOver: 0,
      },
      bulkOperationsModalVisible: false,
      bulkOperationType: null,
      bulkHealthCheckType: "BASIC",
      healthCheckModalVisible: false,
      healthCheckResult: null,
      healthCheckColumns: [
        { title: "检查项", dataIndex: "checkItem", key: "checkItem" },
        { title: "描述", dataIndex: "description", key: "description" },
        { title: "状态", dataIndex: "status", key: "status", scopedSlots: { customRender: "status" } },
        { title: "详情", dataIndex: "details", key: "details" },
        { title: "建议", dataIndex: "suggestion", key: "suggestion" },
      ],
      logViewModalVisible: false,
      selectedLogService: null,
      selectedLogFile: "application.log",
      logContent: "",
      operationsManagementService: new OperationsManagementService(),
    };
  },
  created() {
    this.loadServices();
    this.loadStats();
  },
  methods: {
    // 加载服务列表
    async loadServices() {
      this.loading = true;
      try {
        const params = {
          ...this.searchParams,
          page: this.pagination.current,
          pageSize: this.pagination.pageSize,
        };
        const response = await this.operationsManagementService.listServices(params);
        this.dataSource = response.data || [];
        this.pagination.total = response.total || 0;
      } catch (error) {
        console.error("加载服务列表失败:", error);
        this.$message.error("加载服务列表失败");
      } finally {
        this.loading = false;
      }
    },

    // 加载统计信息
    async loadStats() {
      try {
        const response = await this.operationsManagementService.getStats();
        this.stats = response.data || {};
      } catch (error) {
        console.error("加载统计信息失败:", error);
      }
    },

    // 处理搜索
    handleSearch(value) {
      this.searchParams.keyword = value;
      this.pagination.current = 1;
      this.loadServices();
    },

    // 处理表格变化
    handleTableChange(pagination) {
      this.pagination = pagination;
      this.loadServices();
    },

    // 刷新数据
    handleRefresh() {
      this.loadServices();
      this.loadStats();
    },

    // 运行健康检查
    async handleRunHealthCheck() {
      try {
        await this.operationsManagementService.runHealthCheckAll();
        this.$message.success("健康检查已启动");
        this.loadServices();
        this.loadStats();
      } catch (error) {
        console.error("启动健康检查失败:", error);
        this.$message.error("启动健康检查失败");
      }
    },

    // 显示批量操作模态框
    handleShowBulkOperations() {
      if (this.selectedRowKeys.length === 0) {
        this.$message.warning("请至少选择一个服务");
        return;
      }
      this.selectedServices = this.dataSource.filter(item => 
        this.selectedRowKeys.includes(item.id)
      );
      this.bulkOperationsModalVisible = true;
    },

    // 批量操作确认
    async handleBulkOperationsOk() {
      if (!this.bulkOperationType) {
        this.$message.warning("请选择操作类型");
        return;
      }

      const serviceIds = this.selectedServices.map(service => service.id);
      try {
        let response;
        switch (this.bulkOperationType) {
          case 'START':
            response = await this.operationsManagementService.batchStartServices({ serviceIds });
            break;
          case 'STOP':
            response = await this.operationsManagementService.batchStopServices({ serviceIds });
            break;
          case 'RESTART':
            response = await this.operationsManagementService.batchRestartServices({ serviceIds });
            break;
          case 'RELOAD_CONFIG':
            response = await this.operationsManagementService.batchReloadConfigs({ serviceIds });
            break;
          case 'HEALTH_CHECK':
            response = await this.operationsManagementService.batchHealthCheck({ 
              serviceIds, 
              checkType: this.bulkHealthCheckType 
            });
            break;
        }
        this.$message.success("批量操作已启动");
        this.bulkOperationsModalVisible = false;
        this.loadServices();
        this.loadStats();
      } catch (error) {
        console.error("批量操作失败:", error);
        this.$message.error("批量操作失败");
      }
    },

    // 批量操作取消
    handleBulkOperationsCancel() {
      this.bulkOperationsModalVisible = false;
      this.bulkOperationType = null;
      this.selectedServices = [];
    },

    // 选择变化
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys;
    },

    // 移除选中的服务
    removeSelectedService(serviceId) {
      this.selectedServices = this.selectedServices.filter(service => service.id !== serviceId);
      this.selectedRowKeys = this.selectedRowKeys.filter(key => key !== serviceId);
    },

    // 启动服务
    async handleStartService(record) {
      try {
        await this.operationsManagementService.startService({ serviceId: record.id });
        this.$message.success("服务启动命令已发送");
        this.loadServices();
      } catch (error) {
        console.error("启动服务失败:", error);
        this.$message.error("启动服务失败");
      }
    },

    // 停止服务
    async handleStopService(record) {
      try {
        await this.operationsManagementService.stopService({ serviceId: record.id });
        this.$message.success("服务停止命令已发送");
        this.loadServices();
      } catch (error) {
        console.error("停止服务失败:", error);
        this.$message.error("停止服务失败");
      }
    },

    // 重启服务
    async handleRestartService(record) {
      try {
        await this.operationsManagementService.restartService({ serviceId: record.id });
        this.$message.success("服务重启命令已发送");
        this.loadServices();
      } catch (error) {
        console.error("重启服务失败:", error);
        this.$message.error("重启服务失败");
      }
    },

    // 重载配置
    async handleReloadConfig(record) {
      try {
        await this.operationsManagementService.reloadConfig({ serviceId: record.id });
        this.$message.success("配置重载命令已发送");
      } catch (error) {
        console.error("重载配置失败:", error);
        this.$message.error("重载配置失败");
      }
    },

    // 健康检查
    async handleCheckHealth(record) {
      try {
        const response = await this.operationsManagementService.checkHealth({ serviceId: record.id });
        this.healthCheckResult = response.data;
        this.healthCheckModalVisible = true;
      } catch (error) {
        console.error("健康检查失败:", error);
        this.$message.error("健康检查失败");
      }
    },

    // 查看日志
    async handleViewLogs(record) {
      this.selectedLogService = record;
      this.logViewModalVisible = true;
      await this.loadLogs();
    },

    // 加载日志
    async loadLogs() {
      try {
        const response = await this.operationsManagementService.getLogs({
          serviceId: this.selectedLogService.id,
          logFile: this.selectedLogFile,
        });
        this.logContent = response.data || "";
      } catch (error) {
        console.error("加载日志失败:", error);
        this.logContent = "加载日志失败";
      }
    },

    // 查看指标
    handleViewMetrics(record) {
      console.log("查看指标:", record);
      // TODO: 实现指标查看功能
    },

    // 变更接管级别
    handleChangeTakeoverLevel(record) {
      console.log("变更接管级别:", record);
      // TODO: 实现接管级别变更功能
    },

    // 刷新日志
    async handleRefreshLogs() {
      await this.loadLogs();
    },

    // 下载日志
    async handleDownloadLogs() {
      try {
        await this.operationsManagementService.downloadLogs({
          serviceId: this.selectedLogService.id,
          logFile: this.selectedLogFile,
        });
        this.$message.success("日志下载已开始");
      } catch (error) {
        console.error("下载日志失败:", error);
        this.$message.error("下载日志失败");
      }
    },

    // 搜索日志
    handleSearchLogs(value) {
      console.log("搜索日志:", value);
      // TODO: 实现日志搜索功能
    },

    // 日志文件变化
    handleLogFileChange(value) {
      this.selectedLogFile = value;
      this.loadLogs();
    },

    // 状态徽章
    getStatusBadge(status) {
      const statusMap = {
        RUNNING: 'success',
        STOPPED: 'default',
        STARTING: 'processing',
        STOPPING: 'processing',
        FAILED: 'error',
        UNKNOWN: 'warning',
      };
      return statusMap[status] || 'default';
    },

    getStatusText(status) {
      const textMap = {
        RUNNING: '运行中',
        STOPPED: '已停止',
        STARTING: '启动中',
        STOPPING: '停止中',
        FAILED: '失败',
        UNKNOWN: '未知',
      };
      return textMap[status] || status;
    },

    // 健康状态颜色
    getHealthColor(status) {
      const colors = {
        HEALTHY: 'green',
        UNHEALTHY: 'red',
        WARNING: 'orange',
        UNKNOWN: 'gray',
      };
      return colors[status] || 'gray';
    },

    getHealthText(status) {
      const texts = {
        HEALTHY: '健康',
        UNHEALTHY: '异常',
        WARNING: '警告',
        UNKNOWN: '未知',
      };
      return texts[status] || status;
    },

    // 接管级别颜色
    getTakeoverLevelColor(level) {
      const colors = {
        MONITOR: 'blue',
        CONFIGURE: 'orange',
        CONTROL: 'purple',
        FULL: 'green',
      };
      return colors[level] || 'gray';
    },

    getTakeoverLevelText(level) {
      const texts = {
        MONITOR: '监控',
        CONFIGURE: '配置',
        CONTROL: '控制',
        FULL: '完全',
      };
      return texts[level] || level;
    },

    // 服务图标
    getServiceIcon(serviceType) {
      const icons = {
        HDFS: 'database',
        YARN: 'cluster',
        SPARK: 'rocket',
        HIVE: 'database',
        ZOOKEEPER: 'cluster',
        KAFKA: 'message',
        HBASE: 'table',
        FLINK: 'code',
      };
      return icons[serviceType] || 'appstore';
    },
  },
};
</script>

<style scoped>
.operations-management-page {
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

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.service-table-wrapper {
  margin-top: 16px;
}

.service-name-cell {
  display: flex;
  align-items: center;
}

.selected-services {
  min-height: 32px;
  padding: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #fafafa;
}

.health-check-modal .health-check-details {
  margin-top: 20px;
}

.log-view-modal .log-content {
  margin-top: 20px;
}

.log-toolbar {
  display: flex;
  margin-bottom: 16px;
}

.log-text {
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
  max-height: 500px;
  overflow-y: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>