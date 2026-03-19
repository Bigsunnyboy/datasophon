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


 * @describe: 
 * @Date: 2022-06-23 15:24:29
 * @LastEditTime: 2022-10-25 20:09:13
 * @FilePath: \ddh-ui\src\components\menu\serviceOption.vue
-->
<template>
  <div @click.stop>
    <a-popover ref="serviceOptionPopover" :visible="popoverVisible" trigger="click" placement="rightTop" class="popover-service" overlayClassName="popover-service" :content="()=> getMoreOptions()" @visibleChange="handlePopoverVisibleChange">
      <a-icon type="more" class="cluster-more" style="top: -28px" />
    </a-popover>
    <!-- 配置集群的modal -->
    <a-modal v-if="visible" title :visible="visible" class="service-option-modal" :maskClosable="false" :closable="false" :width="1576" :confirm-loading="confirmLoading" @cancel="handleCancel" :footer="null">
      <Steps :clusterId="clusterId" stepsType="addService" />
    </a-modal>
  </div>
</template>
<script>
import Steps from "@/components/steps";
import { mapMutations, mapState } from 'vuex'

export default {
  provide() {
    return {
      handleCancel: this.handleCancel,
      onSearch: () => {},
    };
  },
  components: { Steps },
  data() {
    return {
      visible: false,
      confirmLoading: false,
      clusterId: Number(localStorage.getItem("clusterId") || -1),
      popoverVisible: false,
    };
  },
  computed: {
    ...mapState({
      setting: (state) => state.setting, //深拷贝的意义在于watch里面可以在Watch里面监听他的newval和oldVal的变化
    }),
  },
  methods: {
    ...mapMutations("setting", ["showClusterSetting"]),
    handleCancel(e) {
      this.visible = false;
    },
    getMoreOptions() {
      return (
        <a-menu mode="vertical" onClick={this.handleMenuClick} class="service-option-menu">
          <a-sub-menu key="componentManage" title="组件管理">
            <a-menu-item key="componentDiscovery">组件发现</a-menu-item>
            <a-menu-item key="componentDiscoveryDetail">组件发现详情</a-menu-item>
            <a-menu-item key="componentDiscoveryResults">组件发现结果</a-menu-item>
          </a-sub-menu>
          <a-menu-item key="addService">添加服务</a-menu-item>
          <a-menu-item key="startAll">启动所有</a-menu-item>
          <a-menu-item key="stopAll">停止所有</a-menu-item>
          <a-menu-item key="restartAll">重启所有需要重启的服务</a-menu-item>
        </a-menu>
      );
    },
    handleMenuClick({ key }) {
      // 关闭popover
      this.popoverVisible = false;
      
      if (key === "addService") {
        this.addService();
      } else if (key === "componentDiscovery") {
        this.$router.push('/service-manage/component-discovery');
      } else if (key === "componentDiscoveryDetail") {
        this.goToComponentDiscoveryDetail();
      } else if (key === "componentDiscoveryResults") {
        this.goToComponentDiscoveryResults();
      } else if (['startAll', 'stopAll', 'restartAll'].includes(key)) {
        this.optServices({ key });
      }
    },
    handlePopoverVisibleChange(visible) {
      this.popoverVisible = visible;
    },
    goToComponentDiscoveryList() {
      // 跳转到组件发现列表页
      this.$router.push('/service-manage/component-discovery');
      this.$message.info('请从列表中选择任务查看详情或结果');
    },
    // 添加服务
    addService() {
      this.visible = true;
    },
    // 跳转到组件管理
    goToComponentManage() {
      this.$router.push('/service-manage/component-discovery');
    },
    // 跳转到组件发现详情页
    goToComponentDiscoveryDetail() {
      const lastTaskId = localStorage.getItem('lastComponentDiscoveryTaskId');
      if (lastTaskId) {
        this.$router.push(`/service-manage/component-discovery-detail/${lastTaskId}`);
      } else {
        this.$router.push('/service-manage/component-discovery');
        this.$message.info('请先选择组件发现任务查看详情');
      }
    },
    // 跳转到组件发现结果页
    goToComponentDiscoveryResults() {
      const lastTaskId = localStorage.getItem('lastComponentDiscoveryTaskId');
      if (lastTaskId) {
        this.$router.push(`/service-manage/component-discovery-results/${lastTaskId}`);
      } else {
        this.$router.push('/service-manage/component-discovery');
        this.$message.info('请先选择组件发现任务查看结果');
      }
    },
    optServices(item) {
      this.$confirm({
        width: 450,
        title: () => {
          return (
            <div style="font-size: 22px;">
              <a-icon
                type="question-circle"
                style="color:#2F7FD1 !important;margin-right:10px"
              />
              提示
            </div>
          );
        },
        content: (
          <div style="margin-top:20px">
            <div style="padding:0 65px;font-size: 16px;color: #555555;">
              {'确认' + (item.key=='startAll'?'启动所有':item.key=='stopAll'?'停止所有':item.key=='restartAll'?'重启所有需要重启的服务':"") +'吗？'}
            </div>
            <div style="margin-top:20px;text-align:right;padding:0 30px 30px 30px">
              <a-button
                style="margin-right:10px;"
                type="primary"
                onClick={() => this.openServices(item)}
              >
                确定
              </a-button>
              <a-button
                style="margin-right:10px;"
                onClick={() => this.$destroyAll()}
              >
                取消
              </a-button>
            </div>
          </div>
        ),
        icon: () => {
          return <div />;
        },
        closable: true,
      });
    
    },
    openServices(item) {
      let params = {
        clusterId: this.setting.clusterId,
        commandType: item.key === "stopAll" ? "STOP_SERVICE" : item.key === "startAll" ? "START_SERVICE" : "RESTART_SERVICE",
        serviceInstanceIds: "",
      };
      let serviceInstanceIds = [];
      const menuData = JSON.parse(localStorage.getItem("menuData")) || [];
      const arr =
        menuData.filter((item) => item.path === "service-manage") || [];
      if (arr.length > 0) {
        arr[0].children.map((child) => {
          if (item.key === "restartAll") {
            if (child.meta.obj.needRestart) {
              serviceInstanceIds.push(child.meta.obj.id);
            }
          } else {
            serviceInstanceIds.push(child.meta.obj.id);
          }
        });
      }
      params.serviceInstanceIds = serviceInstanceIds.join(",");
      this.$axiosPost(global.API.generateServiceCommand, params).then((res) => {
        if (res.code === 200) {
          this.$message.success("操作成功");
          // todo: 打开头部那个setting栏
          this.$destroyAll()
          this.showClusterSetting(true)
        }
      });
    },
  },
};
</script>
<style lang="less" scoped>
.popover-service {
  // margin-left: 31px;
  .more-menu-btn {
    font-size: 14px;
    color: #555555;
    letter-spacing: 0.39px;
    line-height: 32px;
    font-weight: 400;
    &:hover {
      color: @primary-color;
    }
  }
  /deep/ .ant-popover-inner-content {
    text-align: left;
    padding: 12px 16px;
  }
}
.service-option-modal {
  /deep/ .ant-modal {
    top: 61px;
    .ant-modal-body {
      padding: 0;
    }
  }
  /deep/ .ant-modal-content {
    border-radius: 4px;
  }
}

.service-option-menu {
  /deep/ .ant-menu-vertical {
    border: none;
    box-shadow: none;
  }
  /deep/ .ant-menu-submenu-title {
    padding: 0 16px !important;
    height: 32px !important;
    line-height: 32px !important;
  }
  /deep/ .ant-menu-item {
    padding: 0 16px !important;
    height: 32px !important;
    line-height: 32px !important;
    margin: 0 !important;
  }
  /deep/ .ant-menu-submenu-arrow {
    right: 16px !important;
  }
}
</style>