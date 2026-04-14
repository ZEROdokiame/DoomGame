[toc]
# Ratel 客户端开发文档

## 介绍

### 什么是 Ratel

Ratel 是一个在命令行中运行的斗地主游戏项目，基于 Java 开发，使用 [Netty 4.x](https://github.com/netty/netty) 网络框架搭配 [Protobuf](https://github.com/protocolbuffers/protobuf) 数据协议。

本版本为二次开发的纯本地单机版，专注于 PVE 人机对战。

### 技术架构

- **网络协议**：TCP/IP（Protobuf 序列化）、Websocket
- **事件驱动**：Server-Client 通过事件码（CODE）进行通讯
- **客户端架构**：每个事件码对应一个 `ClientEventListener` 实现类

### 数据结构

客户端与服务端交互的数据由三个字段组成：

| 字段 | 说明 |
|------|------|
| CODE | 对应的事件码 |
| DATA | 传递的数据（JSON 格式） |
| INFO | 附加信息（暂未使用） |

Protobuf 协议定义文件位于 `protoc-resource/` 目录。

## 核心事件说明

### 连接成功 — `CODE_CLIENT_CONNECT`

- DATA: 客户端被分配的 ID（文本）

### 设置昵称 — `CODE_CLIENT_NICKNAME_SET`

- DATA: `{"invalidLength": 10}`（昵称超长时返回）

### 抢地主决策 — `CODE_GAME_LANDLORD_ELECT`

- DATA 字段：

| 字段 | 说明 |
|------|------|
| roomId | 房间 ID |
| preClientNickname | 上一个玩家昵称 |
| nextClientNickname | 下一个玩家昵称 |
| nextClientId | 下一个玩家 ID |

### 地主确认 — `CODE_GAME_LANDLORD_CONFIRM`

- DATA 字段：

| 字段 | 说明 |
|------|------|
| landlordNickname | 地主昵称 |
| landlordId | 地主 ID |
| additionalPokers | 额外的三张底牌 |

### 无人抢地主 — `CODE_GAME_LANDLORD_CYCLE`

- DATA: 空。触发后重新发牌。

### 游戏结束 — `CODE_GAME_OVER`

- DATA 字段：

| 字段 | 说明 |
|------|------|
| winnerNickname | 获胜者昵称 |
| winnerType | 获胜者类型（LANDLORD / PEASANT） |

### 退出房间 — `CODE_CLIENT_EXIT`

- DATA: `{"roomId": 14, "exitClientId": 64330, "exitClientNickname": "nico"}`

### 踢出事件 — `CODE_CLIENT_KICK`

- DATA: 空
