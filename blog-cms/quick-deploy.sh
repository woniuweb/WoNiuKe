#!/bin/bash
# NBlog 后台管理系统一键部署脚本（Windows Git Bash / Linux / Mac）
# 使用方法：./quick-deploy.sh

echo "================================"
echo "  NBlog 后台管理系统一键部署脚本"
echo "================================"
echo ""

# 配置区域 - 请根据实际情况修改
SERVER_USER="ubuntu"                          # 服务器用户名
SERVER_HOST="152.136.161.128"                # 服务器IP（修改为你的服务器IP）
SERVER_PATH="/home/ubuntu/blog"              # 服务器部署路径
API_URL="/api/admin/"                        # 使用 Nginx 代理，相对路径

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否在正确的目录
if [ ! -f "package.json" ]; then
    echo -e "${RED}错误: 请在 blog-cms 目录下运行此脚本${NC}"
    exit 1
fi

echo -e "${YELLOW}当前配置:${NC}"
echo "服务器: ${SERVER_USER}@${SERVER_HOST}"
echo "部署路径: ${SERVER_PATH}"
echo "API地址: ${API_URL}"
echo ""

read -p "确认配置正确？(y/n): " confirm
if [ "$confirm" != "y" ]; then
    echo -e "${RED}部署已取消${NC}"
    exit 0
fi

# Step 1: 备份原配置
echo ""
echo -e "${GREEN}[1/6] 备份 API 配置...${NC}"
if [ -f "src/util/request.js.bak" ]; then
    rm src/util/request.js.bak
fi
cp src/util/request.js src/util/request.js.bak

# Step 2: 修改 API 地址
echo -e "${GREEN}[2/6] 修改 API 地址...${NC}"
# 使用 sed 命令替换 baseURL
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s|baseURL:.*|baseURL: '${API_URL}',|g" src/util/request.js
else
    # Linux / Git Bash
    sed -i "s|baseURL:.*|baseURL: '${API_URL}',|g" src/util/request.js
fi

echo -e "${YELLOW}修改后的配置：${NC}"
grep "baseURL" src/util/request.js

# Step 3: 构建项目
echo ""
echo -e "${GREEN}[3/6] 开始构建项目...${NC}"
npm run build

if [ $? -ne 0 ]; then
    echo -e "${RED}构建失败，请检查错误信息${NC}"
    # 恢复配置
    mv src/util/request.js.bak src/util/request.js
    exit 1
fi

# Step 4: 恢复原配置
echo ""
echo -e "${GREEN}[4/6] 恢复原配置...${NC}"
mv src/util/request.js.bak src/util/request.js

# Step 5: 上传到服务器
echo ""
echo -e "${GREEN}[5/6] 上传到服务器...${NC}"
echo "正在创建远程目录并清理旧文件..."
ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${SERVER_PATH}/admin && rm -rf ${SERVER_PATH}/admin/dist"

echo "正在上传 dist 文件..."
scp -r dist ${SERVER_USER}@${SERVER_HOST}:${SERVER_PATH}/admin/

if [ $? -ne 0 ]; then
    echo -e "${RED}上传失败，请检查 SSH 连接${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}  后台部署完成！${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "访问地址: http://${SERVER_HOST}/admin"
echo ""
echo "如遇问题，请检查："
echo "1. Nginx 配置是否正确"
echo "2. 后端 API 服务是否正常运行"
echo "3. 浏览器控制台是否有错误"
echo ""
