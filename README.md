# 在线团队协作任务板 (Online Team Collaboration Kanban)

类似 Trello 的简化版看板工具，支持创建看板、管理列表和卡片、拖拽排序、团队协作。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vite + Element Plus + vuedraggable + Pinia |
| 后端 | Spring Boot 3 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 实时通信 | Spring WebSocket + STOMP / @stomp/stompjs + SockJS |

## 快速开始

### 1. 数据库

```bash
mysql -u root -p < database.sql
```

### 2. 后端启动

```bash
cd kanban-backend

# 修改 src/main/resources/application.yml 中的数据库连接信息

# 编译运行
mvn clean package -DskipTests
java -jar target/kanban-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
cd kanban-frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev
```

前端默认运行在 `http://localhost:5173`

## 项目结构

```
kanban-project/
├── database.sql                    # 数据库初始化脚本（9张表）
├── kanban-backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/kanban/
│       ├── KanbanApplication.java
│       ├── config/                 # Security, WebSocket, MyBatis-Plus, WebMvc
│       ├── security/               # JWT认证（TokenProvider, Filter, Utils）
│       ├── controller/             # 9个 Controller
│       ├── service/                # 9个 Service（业务逻辑）
│       ├── entity/                 # 9个实体类
│       ├── dto/request/            # 请求 DTO
│       ├── mapper/                 # MyBatis-Plus Mapper
│       ├── enums/                  # MemberRole, CardPriority
│       ├── websocket/              # STOMP拦截器 + 事件监听
│       ├── exception/              # 全局异常处理
│       └── util/                   # PositionUtil（浮点数中点算法）
└── kanban-frontend/                # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── router/index.js         # 9条路由
        ├── stores/                 # Pinia (auth, board)
        ├── api/                    # Axios封装 + 7个API模块
        ├── composables/            # useWebSocket, usePermission
        ├── views/                  # 9个页面
        └── components/             # CardDetailDialog
```

## 核心功能

- 用户注册/登录（JWT认证）
- 看板 CRUD
- 列表 CRUD（在看板内创建列）
- 卡片 CRUD + **拖拽移动**（跨列表 + 同列表排序）
- 卡片详情（Markdown描述、评论、附件、标签、优先级、截止日期、负责人）
- 成员管理（所有者/编辑者/查看者 三级权限）
- WebSocket 实时同步
- 活动日志
- 统计看板
- 搜索

## API 接口

### 认证
- `POST /api/auth/register` — 注册
- `POST /api/auth/login` — 登录

### 看板
- `GET /api/boards` — 我的看板列表
- `POST /api/boards` — 创建看板
- `GET/PUT/DELETE /api/boards/{id}` — 看板详情/更新/删除

### 列表
- `GET/POST /api/boards/{boardId}/lists` — 获取/创建列表
- `PUT/DELETE /api/lists/{listId}` — 更新/删除列表

### 卡片
- `GET/POST /api/lists/{listId}/cards` — 获取/创建卡片
- `GET/PUT/DELETE /api/cards/{cardId}` — 卡片详情/更新/删除
- `PUT /api/cards/{cardId}/move` — **拖拽移动**（{targetListId, position}）

### 评论/附件/标签
- `GET/POST /api/cards/{cardId}/comments`
- `GET/POST /api/cards/{cardId}/attachments`
- `GET/POST /api/cards/{cardId}/labels`

### 成员/活动/统计
- `GET/POST/DELETE /api/boards/{boardId}/members`
- `GET /api/boards/{boardId}/activities`
- `GET /api/search?q=keyword`

## WebSocket 端点

- 连接: `ws://localhost:8080/ws` (SockJS)
- 看板实时更新: 订阅 `/topic/board/{boardId}`
- 个人通知: 订阅 `/user/queue/notifications`

## 关键技术点

1. **拖拽排序**: 浮点数中点算法（DOUBLE position），避免 O(n) 批量更新
2. **实时同步**: STOMP over WebSocket，操作即时广播到同看板所有在线用户
3. **权限控制**: 前端 `usePermission` composable + 后端 Service 层双重校验
4. **JWT 认证**: REST API 和 WebSocket 统一使用 Bearer Token

## 部署

```nginx
# Nginx 配置示例
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/kanban-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
    }

    location /ws {
        proxy_pass http://localhost:8080/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```
