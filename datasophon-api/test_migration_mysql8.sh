#!/bin/bash
# MySQL 8.0兼容性测试脚本
# 测试修复后的迁移脚本在MySQL 8.0中的语法兼容性

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}MySQL 8.0迁移脚本兼容性测试${NC}"
echo -e "${GREEN}========================================${NC}"

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker守护进程未运行。请启动Docker并重试。${NC}"
    exit 1
fi

# 临时目录
TEMP_DIR=$(mktemp -d)
echo -e "${YELLOW}临时目录: ${TEMP_DIR}${NC}"

# 清理函数
cleanup() {
    echo -e "${YELLOW}清理临时容器...${NC}"
    docker rm -f mysql-test-migration > /dev/null 2>&1 || true
    rm -rf "${TEMP_DIR}"
    echo -e "${GREEN}清理完成。${NC}"
}

# 注册清理函数
trap cleanup EXIT

# 创建测试数据库初始化脚本
cat > "${TEMP_DIR}/init.sql" << 'EOF'
-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS datasophon_test;
USE datasophon_test;

-- 创建基础表结构（模拟1.3.0版本状态）
CREATE TABLE IF NOT EXISTS t_ddh_cluster_info (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  cluster_name varchar(128) DEFAULT NULL COMMENT '集群名称',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_ddh_cluster_existing_component (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  cluster_id int(11) NOT NULL COMMENT '集群ID',
  PRIMARY KEY (id),
  KEY idx_cluster_id (cluster_id),
  CONSTRAINT fk_existing_component_cluster FOREIGN KEY (cluster_id) 
    REFERENCES t_ddh_cluster_info (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_ddh_cluster_service_instance (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  cluster_id int(11) DEFAULT NULL COMMENT '集群id',
  service_name varchar(32) DEFAULT NULL COMMENT '服务名称',
  service_state int(11) DEFAULT NULL COMMENT '服务状态',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群服务表';

CREATE TABLE IF NOT EXISTS t_ddh_cluster_service_role_instance (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  service_role_name varchar(32) DEFAULT NULL COMMENT '服务角色名称',
  hostname varchar(255) DEFAULT NULL COMMENT '主机',
  service_id int(11) DEFAULT NULL COMMENT '服务id',
  cluster_id int(10) DEFAULT NULL COMMENT '集群id',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群服务角色实例表';

CREATE TABLE IF NOT EXISTS t_ddh_cluster_variable (
  id int(10) NOT NULL AUTO_INCREMENT,
  cluster_id int(10) DEFAULT NULL,
  variable_name varchar(255) DEFAULT NULL,
  variable_value varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- 设置SQL模式以匹配MySQL 8.0默认设置
SET SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
EOF

# 复制迁移脚本到临时目录
MIGRATION_DIR="src/main/resources/db/migration/1.3.0"
echo -e "${YELLOW}复制迁移脚本...${NC}"
cp "${MIGRATION_DIR}/V1.3.1__DDL.sql" "${TEMP_DIR}/V1.3.1__DDL.sql"
cp "${MIGRATION_DIR}/V1.3.2__DDL.sql" "${TEMP_DIR}/V1.3.2__DDL.sql"
cp "${MIGRATION_DIR}/V1.3.3__DDL.sql" "${TEMP_DIR}/V1.3.3__DDL.sql"
cp "${MIGRATION_DIR}/V1.3.1__DML.sql" "${TEMP_DIR}/V1.3.1__DML.sql" 2>/dev/null || true
cp "${MIGRATION_DIR}/V1.3.2__DML.sql" "${TEMP_DIR}/V1.3.2__DML.sql" 2>/dev/null || true
cp "${MIGRATION_DIR}/V1.3.3__DML.sql" "${TEMP_DIR}/V1.3.3__DML.sql" 2>/dev/null || true

# 启动MySQL 8.0容器
echo -e "${YELLOW}启动MySQL 8.0容器...${NC}"
docker run -d \
  --name mysql-test-migration \
  -e MYSQL_ROOT_PASSWORD=testpassword \
  -e MYSQL_DATABASE=datasophon_test \
  -p 3307:3306 \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --sql-mode="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION" > /dev/null 2>&1

# 等待MySQL启动
echo -e "${YELLOW}等待MySQL启动（最多30秒）...${NC}"
for i in {1..30}; do
    if docker exec mysql-test-migration mysqladmin ping -h localhost --silent; then
        echo -e "${GREEN}MySQL已启动。${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}错误: MySQL容器启动超时。${NC}"
        docker logs mysql-test-migration
        exit 1
    fi
    sleep 1
done

# 执行初始化脚本
echo -e "${YELLOW}初始化测试数据库...${NC}"
docker exec -i mysql-test-migration mysql -uroot -ptestpassword < "${TEMP_DIR}/init.sql"

# 测试函数：执行SQL文件并检查错误
test_sql_file() {
    local file_name="$1"
    local file_path="${TEMP_DIR}/${file_name}"
    
    if [ ! -f "$file_path" ]; then
        echo -e "${YELLOW}跳过: ${file_name} (文件不存在)${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}测试: ${file_name}...${NC}"
    
    # 使用mysql客户端执行SQL文件
    if docker exec -i mysql-test-migration mysql -uroot -ptestpassword datasophon_test < "$file_path" 2>&1 | grep -i "error"; then
        echo -e "${RED}失败: ${file_name} 包含SQL错误${NC}"
        docker exec -i mysql-test-migration mysql -uroot -ptestpassword datasophon_test < "$file_path" 2>&1 | head -20
        return 1
    else
        echo -e "${GREEN}通过: ${file_name} 语法正确${NC}"
        return 0
    fi
}

# 测试所有迁移脚本
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}开始测试迁移脚本...${NC}"
echo -e "${GREEN}========================================${NC}"

FAILED=0

# 测试DDL脚本
test_sql_file "V1.3.1__DDL.sql" || FAILED=1
test_sql_file "V1.3.2__DDL.sql" || FAILED=1
test_sql_file "V1.3.3__DDL.sql" || FAILED=1

# 测试DML脚本（如果存在）
test_sql_file "V1.3.1__DML.sql" || FAILED=1
test_sql_file "V1.3.2__DML.sql" || FAILED=1
test_sql_file "V1.3.3__DML.sql" || FAILED=1

# 验证表结构是否创建成功
echo -e "${YELLOW}验证表结构...${NC}"
if docker exec mysql-test-migration mysql -uroot -ptestpassword datasophon_test -e "SHOW TABLES;" | grep -q "t_ddh_takeover_audit_log"; then
    echo -e "${GREEN}通过: t_ddh_takeover_audit_log 表创建成功${NC}"
else
    echo -e "${RED}警告: t_ddh_takeover_audit_log 表可能未创建${NC}"
    FAILED=1
fi

if docker exec mysql-test-migration mysql -uroot -ptestpassword datasophon_test -e "SHOW TABLES;" | grep -q "t_ddh_configuration_management"; then
    echo -e "${GREEN}通过: t_ddh_configuration_management 表创建成功${NC}"
else
    echo -e "${RED}警告: t_ddh_configuration_management 表可能未创建${NC}"
    FAILED=1
fi

if docker exec mysql-test-migration mysql -uroot -ptestpassword datasophon_test -e "SHOW TABLES;" | grep -q "t_ddh_operations_management"; then
    echo -e "${GREEN}通过: t_ddh_operations_management 表创建成功${NC}"
else
    echo -e "${RED}警告: t_ddh_operations_management 表可能未创建${NC}"
    FAILED=1
fi

# 检查新增的列
echo -e "${YELLOW}检查新增列...${NC}"
if docker exec mysql-test-migration mysql -uroot -ptestpassword datasophon_test -e "SHOW COLUMNS FROM t_ddh_cluster_service_instance LIKE 'managed_by';" | grep -q "managed_by"; then
    echo -e "${GREEN}通过: managed_by 列添加成功${NC}"
else
    echo -e "${RED}警告: managed_by 列可能未添加${NC}"
    FAILED=1
fi

# 最终结果
echo -e "${GREEN}========================================${NC}"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}所有迁移脚本测试通过！${NC}"
    echo -e "${GREEN}MySQL 8.0兼容性验证成功完成。${NC}"
else
    echo -e "${RED}部分测试失败。请检查迁移脚本。${NC}"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}测试完成。临时容器将在清理时自动删除。${NC}"