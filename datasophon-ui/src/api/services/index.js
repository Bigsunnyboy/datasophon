import userService from './user'
import dataSource from './dataSource'
import ComponentDiscoveryService from './ComponentDiscoveryService'

// 创建服务实例
const componentDiscoveryService = new ComponentDiscoveryService()

export {
  userService,
  dataSource,
  componentDiscoveryService,
  ComponentDiscoveryService
}
