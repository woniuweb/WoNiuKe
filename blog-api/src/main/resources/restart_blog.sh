#!/bin/bash

# --- 请根据你的实际情况修改以下配置 ---
# JAR包的完整名称
JAR_NAME="blog-api-0.0.1.jar"
# JAR包所在的目录
JAR_PATH="/home/ubuntu/blog/api" # 假设你的jar包放在这里
# 日志文件路径
LOG_FILE="/dev/null"
# --- 配置结束 ---

# 切换到JAR包所在目录
cd $JAR_PATH || exit

# 查找正在运行的JAR包的进程ID (PID)
PID=$(pgrep -f "$JAR_NAME")

if [ -n "$PID" ]; then
    echo "发现正在运行的博客后端进程 (PID: $PID)，正在停止..."
    kill -9 "$PID"
    # 等待几秒钟，确保进程已终止且端口已释放
    sleep 5
    echo "进程已停止。"
else
    echo "未找到正在运行的博客后端进程。"
fi

echo "正在启动 $JAR_NAME ..."

# 使用nohup在后台启动JAR包，并将日志输出到指定文件
# 如果你需要指定生产环境的配置文件，可以加上 --spring.profiles.active=prod
nohup java -jar "$JAR_NAME" > "$LOG_FILE" 2>&1 &

# 等待几秒钟让应用启动
sleep 5

# 再次检查进程是否已成功启动
NEW_PID=$(pgrep -f "$JAR_NAME")

if [ -n "$NEW_PID" ]; then
    echo "$JAR_NAME 已成功启动，新的进程ID为: $NEW_PID."
    echo "你可以使用以下命令查看实时日志："
    echo "tail -f $LOG_FILE"
else
    echo "启动 $JAR_NAME 失败，请检查日志文件获取错误信息："
    echo "cat $LOG_FILE"
fi

exit 0