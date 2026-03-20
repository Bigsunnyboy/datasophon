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


 * @Date: 2026-03-17 02:20:00
 * @LastEditTime: 2026-03-17 02:20:00
 * @FilePath: \datasophon-ui\src\pages\componentDiscovery\wizard\index.vue
-->
<template>
  <div class="discovery-wizard">
    <a-steps :current="currentStep" class="wizard-steps">
      <a-step title="选择集群" />
      <a-step title="配置参数" />
      <a-step title="选择主机" />
      <a-step title="确认启动" />
    </a-steps>
    
    <div class="wizard-content">
      <!-- 步骤1: 选择集群 -->
      <div v-if="currentStep === 0" class="step-content">
        <a-form :form="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
          <a-form-item label="任务名称" required>
            <a-input
              v-decorator="[
                'taskName',
                { rules: [{ required: true, message: '请输入任务名称' }] }
              ]"
              placeholder="请输入发现任务名称"
              :maxLength="50"
            />
          </a-form-item>
          
          <a-form-item label="目标集群" required>
            <a-select
              v-decorator="[
                'clusterId',
                { rules: [{ required: true, message: '请选择目标集群' }] }
              ]"
              placeholder="请选择目标集群"
              @change="handleClusterChange"
              :loading="clusterLoading"
            >
              <a-select-option v-for="cluster in clusterList" :key="cluster.id" :value="cluster.id">
                {{ cluster.clusterName }} (ID: {{ cluster.id }})
              </a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="组件类型" required>
            <a-select
              v-decorator="[
                'componentType',
                { rules: [{ required: true, message: '请选择组件类型' }] }
              ]"
              placeholder="请选择要发现的组件类型"
              mode="multiple"
            >
              <a-select-option value="HDFS">HDFS</a-select-option>
              <a-select-option value="YARN">YARN</a-select-option>
              <a-select-option value="SPARK">Spark</a-select-option>
              <a-select-option value="HIVE">Hive</a-select-option>
              <a-select-option value="HBASE">HBase</a-select-option>
              <a-select-option value="ZOOKEEPER">Zookeeper</a-select-option>
              <a-select-option value="KAFKA">Kafka</a-select-option>
              <a-select-option value="ALL">所有类型</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="发现策略" required>
            <a-select
              v-decorator="[
                'discoveryStrategy',
                { 
                  initialValue: 'COMPREHENSIVE',
                  rules: [{ required: true, message: '请选择发现策略' }] 
                }
              ]"
              placeholder="请选择发现策略"
            >
              <a-select-option value="SSH_SCAN">SSH扫描</a-select-option>
              <a-select-option value="PORT_SCAN">端口扫描</a-select-option>
              <a-select-option value="CONFIG_SCAN">配置扫描</a-select-option>
              <a-select-option value="METADATA_QUERY">元数据查询</a-select-option>
              <a-select-option value="COMPREHENSIVE">综合发现</a-select-option>
            </a-select>
            <div class="strategy-description">
              <p><strong>SSH扫描</strong>: 通过SSH连接检查进程和安装目录</p>
              <p><strong>端口扫描</strong>: 扫描常见大数据服务端口</p>
              <p><strong>配置扫描</strong>: 查找配置文件路径和内容</p>
              <p><strong>元数据查询</strong>: 查询集群元数据服务</p>
              <p><strong>综合发现</strong>: 组合多种策略进行全面发现</p>
            </div>
          </a-form-item>
          
          <a-form-item label="任务描述">
            <a-textarea
              v-decorator="['description']"
              placeholder="请输入任务描述（可选）"
              :rows="3"
              :maxLength="200"
            />
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 步骤2: 配置参数 -->
      <div v-else-if="currentStep === 1" class="step-content">
        <a-form :form="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
          <a-form-item label="超时时间(秒)" required>
            <a-input-number
              v-decorator="[
                'timeoutSeconds',
                { 
                  initialValue: 300,
                  rules: [{ required: true, message: '请输入超时时间' }] 
                }
              ]"
              :min="60"
              :max="3600"
              style="width: 200px"
            />
            <span class="mgl8">建议值: 300-600秒</span>
          </a-form-item>
          
          <a-form-item label="并发线程数" required>
            <a-input-number
              v-decorator="[
                'concurrentThreads',
                { 
                  initialValue: 5,
                  rules: [{ required: true, message: '请输入并发线程数' }] 
                }
              ]"
              :min="1"
              :max="20"
              style="width: 200px"
            />
            <span class="mgl8">建议值: 3-10个线程</span>
          </a-form-item>
          
          <a-form-item label="SSH端口">
            <a-input-number
              v-decorator="[
                'sshPort',
                { initialValue: 22 }
              ]"
              :min="1"
              :max="65535"
              style="width: 200px"
            />
          </a-form-item>
          
          <a-form-item label="端口扫描范围">
            <a-input
              v-decorator="[
                'portRange',
                { initialValue: '9000-10000' }
              ]"
              placeholder="例如: 9000-10000"
              style="width: 200px"
            />
            <span class="mgl8">默认: 大数据服务常用端口</span>
          </a-form-item>
          
          <a-form-item label="深度扫描">
            <a-switch
              v-decorator="[
                'deepScan',
                { valuePropName: 'checked', initialValue: false }
              ]"
              checked-children="开启"
              un-checked-children="关闭"
            />
            <div class="scan-description">
              <p>开启深度扫描将检查更详细的配置信息，但会增加扫描时间</p>
            </div>
          </a-form-item>
          
          <a-form-item label="自动验证">
            <a-switch
              v-decorator="[
                'autoValidate',
                { valuePropName: 'checked', initialValue: true }
              ]"
              checked-children="开启"
              un-checked-children="关闭"
            />
            <div class="scan-description">
              <p>发现完成后自动验证组件可用性</p>
            </div>
          </a-form-item>
          
          <a-form-item label="默认接管级别">
            <a-select
              v-decorator="[
                'defaultTakeoverLevel',
                { initialValue: 'MONITOR_ONLY' }
              ]"
              style="width: 200px"
            >
              <a-select-option value="MONITOR_ONLY">只读监控</a-select-option>
              <a-select-option value="CONFIGURATION">配置管理</a-select-option>
              <a-select-option value="CONTROL">操作控制</a-select-option>
              <a-select-option value="FULL">完全接管</a-select-option>
            </a-select>
            <div class="scan-description">
              <p>设置发现组件的默认接管级别，可在发现后调整</p>
            </div>
          </a-form-item>
          
          <a-form-item label="自动配置同步">
            <a-switch
              v-decorator="[
                'autoConfigSync',
                { valuePropName: 'checked', initialValue: false }
              ]"
              checked-children="开启"
              un-checked-children="关闭"
            />
            <div class="scan-description">
              <p>发现完成后自动同步配置差异（需要接管级别为配置管理或更高）</p>
            </div>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 步骤3: 选择主机 -->
      <div v-else-if="currentStep === 2" class="step-content">
        <div class="host-selection">
          <div class="host-filter mgb16">
            <a-input placeholder="搜索主机名或IP" class="w200 mgr12" v-model="hostFilter" allowClear />
            <a-select placeholder="按架构筛选" class="w150 mgr12" :allowClear="true" v-model="architectureFilter">
              <a-select-option value="x86_64">x86_64</a-select-option>
              <a-select-option value="aarch64">aarch64</a-select-option>
            </a-select>
            <a-button @click="clearFilters" icon="close">清除筛选</a-button>
          </div>
          
          <div class="host-list-container">
            <a-checkbox-group v-model="selectedHostIds" class="host-list">
              <a-row :gutter="16">
                <a-col :span="8" v-for="host in filteredHosts" :key="host.id" class="mgt8">
                  <a-checkbox :value="host.id">
                    <div class="host-item">
                      <div class="host-info">
                        <strong>{{ host.hostname }}</strong>
                        <span class="host-ip">({{ host.ip }})</span>
                      </div>
                      <div class="host-details">
                        <span class="host-arch">{{ host.cpuArchitecture }}</span>
                        <span class="host-state" :class="`state-${host.hostState}`">{{ getHostStateText(host.hostState) }}</span>
                      </div>
                    </div>
                  </a-checkbox>
                </a-col>
              </a-row>
            </a-checkbox-group>
            
            <div v-if="filteredHosts.length === 0" class="empty-hosts">
              <a-empty description="暂无可用主机" />
            </div>
            
            <div class="host-summary mgt16">
              <span>已选择: {{ selectedHostIds.length }} / {{ hostList.length }} 台主机</span>
              <a-button type="link" @click="selectAllHosts" class="mgl16">全选</a-button>
              <a-button type="link" @click="clearSelection" class="mgl8">清空</a-button>
            </div>
          </div>
        </div>
        
        <div class="host-tips mgt16">
          <a-alert message="提示" type="info" show-icon>
            <div slot="description">
              <p>1. 建议选择状态为"运行中"的主机进行发现</p>
              <p>2. 选择的主机需要有SSH访问权限和适当的权限</p>
              <p>3. 主机数量越多，发现时间越长</p>
            </div>
          </a-alert>
        </div>
      </div>
      
      <!-- 步骤4: 确认启动 -->
      <div v-else-if="currentStep === 3" class="step-content">
        <div class="confirmation-summary">
          <a-descriptions title="任务配置摘要" :column="1" bordered>
            <a-descriptions-item label="任务名称">{{ formValues.taskName }}</a-descriptions-item>
            <a-descriptions-item label="目标集群">{{ selectedClusterName }}</a-descriptions-item>
            <a-descriptions-item label="组件类型">
              <a-tag v-for="type in formValues.componentType" :key="type" class="mgr8">
                {{ type }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="发现策略">{{ getStrategyText(formValues.discoveryStrategy) }}</a-descriptions-item>
            <a-descriptions-item label="目标主机数">{{ selectedHostIds.length }}</a-descriptions-item>
            <a-descriptions-item label="超时时间">{{ formValues.timeoutSeconds }} 秒</a-descriptions-item>
            <a-descriptions-item label="并发线程数">{{ formValues.concurrentThreads }}</a-descriptions-item>
            <a-descriptions-item label="深度扫描">{{ formValues.deepScan ? '开启' : '关闭' }}</a-descriptions-item>
            <a-descriptions-item label="自动验证">{{ formValues.autoValidate ? '开启' : '关闭' }}</a-descriptions-item>
            <a-descriptions-item label="默认接管级别">{{ getTakeoverLevelText(formValues.defaultTakeoverLevel) }}</a-descriptions-item>
            <a-descriptions-item label="自动配置同步">{{ formValues.autoConfigSync ? '开启' : '关闭' }}</a-descriptions-item>
            <a-descriptions-item label="任务描述">{{ formValues.description || '无' }}</a-descriptions-item>
          </a-descriptions>
          
          <div class="confirmation-tips mgt16">
            <a-alert message="确认信息" type="warning" show-icon>
              <div slot="description">
                <p>1. 确认配置无误后点击"开始发现"启动任务</p>
                <p>2. 发现过程可能需要几分钟到几十分钟，具体时间取决于主机数量和网络状况</p>
                <p>3. 您可以在任务列表页面查看进度和结果</p>
              </div>
            </a-alert>
          </div>
        </div>
      </div>
    </div>
    
    <div class="wizard-footer">
      <a-button @click="handleCancel" v-if="currentStep === 0">取消</a-button>
      <a-button @click="prevStep" v-if="currentStep > 0" class="mgl8">上一步</a-button>
      <a-button type="primary" @click="nextStep" :loading="loading" class="mgl8">
        {{ currentStep === 3 ? '开始发现' : '下一步' }}
      </a-button>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "DiscoveryWizard",
  props: {
    visible: Boolean,
    onCancel: Function,
    onSuccess: Function,
  },
  data() {
    return {
      currentStep: 0,
      loading: false,
      form: this.$form.createForm(this),
      clusterList: [],
      clusterLoading: false,
      hostList: [],
      selectedHostIds: [],
      hostFilter: "",
      architectureFilter: null,
      defaultTakeoverLevel: "MONITOR_ONLY",
      autoConfigSync: false,
      formValues: {},
    };
  },
  computed: {
    ...mapState({
      setting: (state) => state.setting,
    }),
    selectedClusterName() {
      const cluster = this.clusterList.find(c => c.id === this.formValues.clusterId);
      return cluster ? cluster.clusterName : "";
    },
    filteredHosts() {
      let hosts = this.hostList;
      
      // 按文本筛选
      if (this.hostFilter) {
        const filter = this.hostFilter.toLowerCase();
        hosts = hosts.filter(host => 
          host.hostname.toLowerCase().includes(filter) || 
          host.ip.includes(filter)
        );
      }
      
      // 按架构筛选
      if (this.architectureFilter) {
        hosts = hosts.filter(host => host.cpuArchitecture === this.architectureFilter);
      }
      
      return hosts;
    },
  },
  mounted() {
    this.loadClusterList();
  },
  methods: {
    // 加载集群列表
    loadClusterList() {
      this.clusterLoading = true;
      this.$axiosJsonPost(global.API.cluster.list, {})
        .then((res) => {
          if (res.code === 200) {
            this.clusterList = res.data || [];
          }
        })
        .catch((error) => {
          console.error("加载集群列表失败:", error);
          this.$message.error("加载集群列表失败");
        })
        .finally(() => {
          this.clusterLoading = false;
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
            // 默认选择所有运行中的主机
            this.selectedHostIds = this.hostList
              .filter(host => host.hostState === 1)
              .map(host => host.id);
          }
        })
        .catch((error) => {
          console.error("加载主机列表失败:", error);
          this.$message.error("加载主机列表失败");
        });
    },
    
    // 集群变更处理
    handleClusterChange(clusterId) {
      this.formValues.clusterId = clusterId;
      this.loadHostList(clusterId);
    },
    
    // 主机状态文本
    getHostStateText(state) {
      const stateMap = {
        0: "未知",
        1: "运行中",
        2: "已停止",
        3: "安装中",
        4: "安装失败",
      };
      return stateMap[state] || "未知";
    },
    
    // 发现策略文本
    getStrategyText(strategy) {
      const strategyMap = {
        SSH_SCAN: "SSH扫描",
        PORT_SCAN: "端口扫描",
        CONFIG_SCAN: "配置扫描",
        METADATA_QUERY: "元数据查询",
        COMPREHENSIVE: "综合发现",
      };
      return strategyMap[strategy] || strategy;
    },
    
    // 接管级别文本
    getTakeoverLevelText(takeoverLevel) {
      const levelMap = {
        MONITOR_ONLY: "只读监控",
        CONFIGURATION: "配置管理", 
        CONTROL: "操作控制",
        FULL: "完全接管"
      };
      return levelMap[takeoverLevel] || takeoverLevel;
    },
    
    // 主机筛选
    clearFilters() {
      this.hostFilter = "";
      this.architectureFilter = null;
    },
    
    selectAllHosts() {
      this.selectedHostIds = this.filteredHosts.map(host => host.id);
    },
    
    clearSelection() {
      this.selectedHostIds = [];
    },
    
    // 步骤导航
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--;
      }
    },
    
    async nextStep() {
      this.loading = true;
      
      try {
        // 验证当前步骤
        if (this.currentStep < 3) {
          await this.validateCurrentStep();
          
          // 收集表单值
          const values = await this.form.validateFields();
          this.formValues = { ...this.formValues, ...values };
          
          // 如果是第二步，需要确保选择了主机
          if (this.currentStep === 2 && this.selectedHostIds.length === 0) {
            this.$message.warning("请至少选择一台主机");
            this.loading = false;
            return;
          }
          
          this.currentStep++;
        } else {
          // 最后一步：提交任务
          await this.submitDiscoveryTask();
        }
      } catch (error) {
        console.error("步骤验证失败:", error);
        // 表单验证失败会在这里被捕获
      } finally {
        this.loading = false;
      }
    },
    
    // 验证当前步骤
    validateCurrentStep() {
      return new Promise((resolve, reject) => {
        this.form.validateFields((err, values) => {
          if (err) {
            reject(err);
          } else {
            resolve(values);
          }
        });
      });
    },
    
    // 提交发现任务
    async submitDiscoveryTask() {
      const params = {
        ...this.formValues,
        targetHostIds: this.selectedHostIds,
      };
      
      this.loading = true;
      try {
         const res = await this.$axiosJsonPost(global.API.componentDiscovery.startTask, params);
        
        if (res.code === 200) {
          this.$message.success("发现任务创建成功");
          this.$emit('success', res.data);
          if (this.onSuccess) {
            this.onSuccess(res.data);
          }
          this.handleCancel();
        } else {
          this.$message.error(res.message || "创建任务失败");
        }
      } catch (error) {
        this.$message.error("请求失败: " + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    // 取消处理
    handleCancel() {
      this.$emit('cancel');
      if (this.onCancel) {
        this.onCancel();
      }
    },
    
    // 重置向导
    resetWizard() {
      this.currentStep = 0;
      this.selectedHostIds = [];
      this.form.resetFields();
      this.formValues = {};
      this.loadClusterList();
    },
  },
};
</script>

<style lang="less" scoped>
.discovery-wizard {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .wizard-steps {
    margin-bottom: 24px;
  }
  
  .wizard-content {
    flex: 1;
    overflow-y: auto;
    padding: 0 16px;
    
    .step-content {
      min-height: 400px;
    }
    
    .strategy-description,
    .scan-description {
      margin-top: 8px;
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;
      border: 1px solid #e8e8e8;
      font-size: 12px;
      color: #666;
      
      p {
        margin: 4px 0;
      }
    }
    
    .host-selection {
      .host-filter {
        display: flex;
        align-items: center;
        
        .w200 {
          width: 200px;
        }
        
        .w150 {
          width: 150px;
        }
      }
      
      .host-list-container {
        max-height: 300px;
        overflow-y: auto;
        padding: 12px;
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        background: #fafafa;
        
        .host-item {
          padding: 8px;
          border: 1px solid #e8e8e8;
          border-radius: 4px;
          background: #fff;
          
          .host-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
            
            .host-ip {
              color: #666;
              font-size: 12px;
            }
          }
          
          .host-details {
            display: flex;
            justify-content: space-between;
            font-size: 12px;
            color: #999;
            
            .host-arch {
              background: #e6f7ff;
              padding: 1px 4px;
              border-radius: 2px;
            }
            
            .host-state {
              padding: 1px 4px;
              border-radius: 2px;
              
              &.state-1 {
                background: #f6ffed;
                color: #52c41a;
              }
              
              &.state-2 {
                background: #fff2f0;
                color: #ff4d4f;
              }
              
              &.state-3, &.state-4 {
                background: #fff7e6;
                color: #faad14;
              }
            }
          }
        }
      }
      
      .empty-hosts {
        text-align: center;
        padding: 40px 0;
      }
      
      .host-summary {
        text-align: center;
        color: #666;
        font-size: 14px;
      }
    }
    
    .host-tips {
      margin-top: 16px;
    }
    
    .confirmation-summary {
      .confirmation-tips {
        margin-top: 16px;
      }
    }
  }
  
  .wizard-footer {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #e8e8e8;
    text-align: right;
  }
  
  .mgl8 {
    margin-left: 8px;
  }
  
  .mgr8 {
    margin-right: 8px;
  }
  
  .mgr12 {
    margin-right: 12px;
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
  
  .mgb16 {
    margin-bottom: 16px;
  }
}
</style>