#!/bin/bash

# 定义公共参数
JAVA_OPTS="-DSECRET=WCdTBej2ZRhIBXafQbALbAwpJ5A+v1PR4A4IN6+OhnM= -DMYSQL_PASSWORD=wwq_2021 -DEMAIL=275018723@qq.com -DEMAIL_PASSWORD=tquazwwauhwubgjf"

# 启动所有JAR文件（后台运行）
nohup java $JAVA_OPTS -jar user-0.0.1-SNAPSHOT.jar > user.log 2>&1 &
nohup java $JAVA_OPTS -jar chat-0.0.1-SNAPSHOT.jar > chat.log 2>&1 &
nohup java $JAVA_OPTS -jar circle-0.0.1-SNAPSHOT.jar > circle.log 2>&1 &
nohup java $JAVA_OPTS -jar social-0.0.1-SNAPSHOT.jar > social.log 2>&1 &
nohup java $JAVA_OPTS -jar movie-0.0.1-SNAPSHOT.jar > movie.log 2>&1 &
nohup java $JAVA_OPTS -jar music-0.0.1-SNAPSHOT.jar > music.log 2>&1 &
nohup java $JAVA_OPTS -jar prompt-0.0.1-SNAPSHOT.jar > prompt.log 2>&1 &
nohup java $JAVA_OPTS -jar tenant-0.0.1-SNAPSHOT.jar > tenant.log 2>&1 &

# 等待所有进程
wait