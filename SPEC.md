# 简易社交分享平台 - 项目规格说明书

> **快速启动**: 双击 `start_project.bat` 或分别运行后端(9090)和前端(5173)
> - 后端: `cd backend && mvn spring-boot:run`
> - 前端: `cd frontend && npm run dev`
> - 测试账号: aaa / aaa

## 1. 项目概述

### 1.1 项目名称
PCC2 社交分享平台 (SimpleSocial)

### 1.2 项目类型
前后端分离的全栈 Web 应用

### 1.3 核心功能
类似微博的社交分享平台，支持用户注册登录、内容发布（文字+图片+视频）、信息流浏览、评论、点赞、转发、关注、收藏等社交互动功能。

### 1.4 目标用户
普通互联网用户，用于日常社交分享

---

## 2. 技术架构

### 2.1 技术选型

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 | 3.4+ |
| 前端路由 | Vue Router | 4.x |
| 网络请求 | Axios | 1.6+ |
| 构建工具 | Vite | 5.x |
| 后端框架 | Spring Boot | 3.2.x |
| 持久层 | MyBatis | 3.0.x |
| 数据库 | MySQL | 8.x |
| 认证 | JWT | - |

### 2.2 端口配置
- 后端服务：http://localhost:9090
- 前端服务：http://localhost:5173
- 图片/视频访问：http://localhost:9090/uploads/**

### 2.3 项目结构

```
pcc2/
├── SPEC.md                    # 项目规格说明书
├── database.sql               # 数据库脚本
├── backend/                   # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/pcc2/social/
│       ├── SocialApplication.java
│       ├── config/            # 配置类
│       ├── controller/        # 控制器
│       ├── service/           # 业务逻辑
│       ├── mapper/           # 数据访问
│       ├── entity/           # 实体类
│       ├── dto/              # 数据传输对象
│       ├── common/           # 公共组件
│       ├── interceptor/      # JWT拦截器
│       └── exception/        # 异常处理
│   └── src/main/resources/
│       ├── mapper/           # MyBatis XML
│       ├── application.yml  # 配置文件
│       └── logback-spring.xml
│
└── frontend/                  # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/             # API 接口
        ├── router/          # 路由配置
        ├── views/           # 页面视图
        ├── components/      # 公共组件
        └── utils/           # 工具函数
```

---

## 3. 数据库设计

### 3.1 数据库名称
`social_platform`

### 3.2 数据表

#### 3.2.1 用户表 (user)
```sql
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
    `followers_count` INT DEFAULT 0 COMMENT '粉丝数',
    `following_count` INT DEFAULT 0 COMMENT '关注数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3.2.2 内容表 (post)
```sql
CREATE TABLE `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `content` TEXT NOT NULL COMMENT '文字内容',
    `media_url` VARCHAR(255) DEFAULT NULL COMMENT '媒体URL(图片/视频)',
    `media_type` VARCHAR(20) DEFAULT NULL COMMENT '媒体类型(image/video)',
    `repost_id` BIGINT DEFAULT NULL COMMENT '原动态ID(转发)',
    `original_content` TEXT DEFAULT NULL COMMENT '原动态内容(转发)',
    `like_count` INT DEFAULT 0 COMMENT '点赞数量',
    `comment_count` INT DEFAULT 0 COMMENT '评论数量',
    `repost_count` INT DEFAULT 0 COMMENT '转发数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    foreign KEY (`repost_id`) REFERENCES `post`(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3.2.3 评论表 (comment)
```sql
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '所属内容ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3.2.4 点赞表 (like_record)
```sql
CREATE TABLE `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '被点赞内容ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞者ID',
    `type` VARCHAR(20) DEFAULT 'post' COMMENT '类型(post/comment)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3.2.5 关注表 (follow)
```sql
CREATE TABLE `follow` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `follower_id` BIGINT NOT NULL COMMENT '粉丝ID',
    `following_id` BIGINT NOT NULL COMMENT '关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`follower_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3.2.6 收藏表 (favorite)
```sql
CREATE TABLE `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '收藏者ID',
    `post_id` BIGINT NOT NULL COMMENT '被收藏内容ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 4. API 设计

### 4.1 接口规范
- 基础路径：`/api`
- 请求格式：JSON
- 认证方式：Bearer Token (JWT)
- 跨域支持：CORS

### 4.2 用户接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user/register | 用户注册 | 否 |
| POST | /api/user/login | 用户登录 | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |
| PUT | /api/user/info | 更新用户信息 | 是 |
| GET | /api/user/{id} | 获取指定用户信息 | 否 |
| GET | /api/user/{id}/posts | 获取用户发布的内容 | 否 |

### 4.3 内容接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/post/list | 获取信息流(分页) | 否 |
| GET | /api/post/user/{userId} | 获取用户发布的内容 | 否 |
| GET | /api/post/{id} | 获取单条内容详情 | 否 |
| POST | /api/post | 发布新内容 | 是 |
| DELETE | /api/post/{id} | 删除内容 | 是(仅Own) |
| POST | /api/post/upload | 上传媒体(图片/视频) | 是 |
| GET | /api/post/following | 获取关注的人动态 | 是 |
| GET | /api/post/favorites | 获取收藏内容列表 | 是 |

### 4.4 评论接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/comment/post/{postId} | 获取评论列表 | 否 |
| GET | /api/comment/post/{postId}?sort={sort} | 获取评论列表(排序) | 否 |
| POST | /api/comment | 发表评论 | 是 |
| DELETE | /api/comment/{id} | 删除评论 | 是(仅Own) |

### 4.5 互动接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/like/{postId} | 点赞/取消点赞 | 是 |
| GET | /api/like/status/{postId} | 获取点赞状态 | 是 |
| POST | /api/repost/{postId} | 转发内容 | 是 |

### 4.6 关注接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/follow/{userId} | 关注/取消关注 | 是 |
| GET | /api/follow/status/{userId} | 获取关注状态 | 是 |
| GET | /api/follow/followers/{userId} | 获取粉丝列表 | 否 |
| GET | /api/follow/following/{userId} | 获取关注列表 | 否 |

### 4.7 收藏接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/favorite/{postId} | 收藏/取消收藏 | 是 |
| GET | /api/favorite/status/{postId} | 获取收藏状态 | 是 |
| GET | /api/favorite/list | 获取收藏列表 | 是 |

### 4.8 响应格式

成功响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

失败响应：
```json
{
    "code": 400,
    "message": "错误信息",
    "data": null
}
```

---

## 5. 前端结构

### 5.1 路由配置

| 路径 | 组件 | 说明 | 权限 |
|------|------|------|------|
| / | HomeView | 主页/信息流 | 公开 |
| /login | LoginView | 登录页 | 公开 |
| /register | RegisterView | 注册页 | 公开 |
| /publish | PublishView | 发布页 | 登录 |
| /profile | ProfileView | 个人主页 | 登录 |
| /profile/:userId | ProfileView | 他人主页 | 公开 |
| /post/:id | PostDetailView | 内容详情 | 公开 |
| /favorites | FavoritesView | 收藏列表 | 登录 |

### 5.2 状态管理
使用 Vue 3 Composition API + provide/inject 进行状态管理：
- 用户信息状态
- 登录状态
- 令牌存储

### 5.3 UI 设计(微博风格)

#### 5.3.1 整体布局
- 顶部导航栏：固定定位，高度60px，包含Logo、搜索、发布入口、个人入口
- 左侧边栏：固定宽度220px，包含导航菜单（首页、发现、收藏等）
- 中央信息流：Flex-grow，宽度600-700px，最大宽度700px
- 右侧区域：移除，相关信息放到顶部导航或左侧边栏下方

#### 5.3.2 响应式布局
- PC端：两栏布局 (220px + 剩余空间，信息流居中)
- 平板：单栏布局（左侧边栏可收起）
- 手机：单栏，信息密度高

### 5.4 Feed流设计
- 无限滚动(Infinite Scroll)
- 信息密度高
- 卡片式设计：头像、用户名、认证标识、内容、配图/视频、底部操作栏

---

## 6. 功能模块

### 6.1 用户模块
- [x] 用户注册（账号、密码、表单验证）
- [x] 用户登录（JWT 令牌）
- [x] 登录状态保持（localStorage）
- [x] 退出登录
- [x] 当前用户信息获取

### 6.2 内容模块
- [x] 发布内容（文字+图片）
- [x] 媒体上传与预览（图片、视频）
- [x] 信息流展示（分页、倒序）
- [x] 个人内容列表
- [x] 内容删除（仅Own）
- [x] 转发功能
- [x] 视频发布支持

### 6.3 互动模块
- [x] 发表评论
- [x] 评论列表展示
- [x] 评论删除（仅Own）
- [x] 点赞/取消点赞
- [x] 点赞数量实时更新
- [x] 当前用户点赞状态
- [x] 评论排序（按时间正序、倒序、按热度）
- [x] 热门评论置顶

### 6.4 关注模块
- [x] 关注/取消关注
- [x] 粉丝列表
- [x] 关注列表
- [x] 获取关注的人动态

### 6.5 收藏模块
- [x] 收藏/取消收藏
- [x] 收藏列表展示

### 6.6 权��控制
- [x] 未登录拦截（发布、评论、点赞）
- [x] 操作权限验证（后端）
- [x] 前端路由守卫

---

## 7. 安全措施

### 7.1 认证安全
- JWT 令牌过期时间：7 天
- 密码使用 BCrypt 加密
- 令牌存储在 localStorage

### 7.2 接口安全
- 所有写操作需要登录认证
- 删除操作验证Ownership
- SQL 注入防护（MyBatis）
- XSS 防护（前端转义）

### 7.3 文件安全
- 仅允许图片/视频格式（jpg, png, gif, webp, mp4）
- 文件大小限制：10MB
- 随机文件名生成

---

## 8. 实施模块

### 模块一：基础搭建
1. 创建 Spring Boot 项目结构
2. 配置 MySQL 数据库连接
3. 配置 MyBatis
4. 创建数据库表
5. 创建 Vue 3 项目结构
6. 配置 Vite 代理

### 模块二：用户模块
1. 后端：User 实体、Mapper、Service、Controller
2. 前端：登录、注册页面
3. JWT 认证配置
4. 路由守卫

### 模块三：内容模块
1. 后端：Post 实体、Mapper、Service、Controller
2. 媒体上传功能（图片、视频）
3. 前端：信息流页面
4. 前端：发布页面

### 模块四：互动模块
1. 后端：Comment 实体、Mapper、Service、Controller
2. 后端：Like 实体、Mapper、Service、Controller
3. 前端：评论功能
4. 前端：点赞功能

### 模块五：关注与收藏
1. 后端：Follow、Favorite 实体、Mapper、Service、Controller
2. 前端：关注功能
3. 前端：收藏功能
4. 前端：个人主页优化

### 模块六：UI重构
1. 顶部导航栏
2. 左侧边栏
3. 右侧边栏（发现/推荐）
4. 中央Feed流
5. 无限滚动
6. 响应式适配

### 模块七：完善优化
1. 转发功能
2. 评论排序
3. 视频播放
4. 错误处理
5. 性能优化

---

## 9. 配置文件

### 9.1 后端 application.yml
```yaml
server:
  port: 9090

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/social_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: hp197027
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.pcc2.social.entity
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: pcc2-social-platform-secret-key-2024
  expiration: 604800000

upload:
  path: ./uploads/
  image-path: ./uploads/images/
  video-path: ./uploads/videos/
```

### 9.2 前端 vite.config.js
```javascript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  }
})
```

---

## 10. 验收标准

### 一、功能验收
- [ ] 用户可注册、登录、退出
- [ ] 登录后可发布内容（文字+图片）
- [ ] 支持视频发布
- [ ] 首页展示信息流（无限滚动、倒序）
- [ ] 可评论、点赞/取消点赞
- [ ] 可删除自己发布的内容和评论
- [ ] 未登录无法操作（发布、评论、点赞）
- [ ] 个人主页展示用户发布的内容
- [ ] 可关注/取消关注
- [ ] 可收藏/取消收藏
- [ ] 可转发内容
- [ ] 评论可排序（时间正序、倒序、按热度）
- [ ] 热门评论可置顶

### 二、体验验收
- [ ] 表单验证完善（空内容、格式错误提示）
- [ ] 加载动画流畅
- [ ] 响应式布局正常（PC/手机适配）
- [ ] 操作反馈及时（成功/失败提示 Toast）
- [ ] 图片/视频预览正常
- [ ] 无刷新页面跳转
- [ ] 顶部导航栏正常显示
- [ ] 左侧边栏导航可用
- [ ] 右侧边栏（发现）正常
- [ ] 信息流无限滚动正常
- [ ] 视频播放正常

### 三、代码质量验收
- [ ] 代码结构清晰，模块划分合理
- [ ] 命名规范统一
- [ ] 异常处理完善（后端统一异常捕获）
- [ ] 安全防护到位（密码加密、SQL注入防护、XSS过滤）
- [ ] 接口响应格式统一
- [ ] 前后端接口匹配

### 四、性能验收
- [ ] 数据库查询效率合理（索引优化）
- [ ] 媒体上传响应正常
- [ ] 前端页面加载速度可接受
- [ ] 并发操作无数据异常

---

## 11. 测试指南

### 11.1 后端测试 (API 测试)

#### 用户接口测试
```bash
# 注册
curl -X POST http://localhost:9090/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","nickname":"测试用户"}'

# 登录
curl -X POST http://localhost:9090/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 获取用户信息
curl -X GET http://localhost:9090/api/user/info \
  -H "Authorization: Bearer <token>"
```

#### 内容接口测试
```bash
# 获取信息流
curl "http://localhost:9090/api/post/list?page=1&size=10"

# 发布内容
curl -X POST http://localhost:9090/api/post \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"content":"测试内容"}'
```

#### 互动接口测试
```bash
# 点赞
curl -X POST http://localhost:9090/api/like/1 \
  -H "Authorization: Bearer <token>"

# 评论
curl -X POST http://localhost:9090/api/comment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"postId":1,"content":"测试评论"}'
```

### 11.2 前端功能测试清单

#### 基础功能测试
| 功能 | 测试步骤 | 预期结果 |
|------|----------|----------|
| 用户注册 | 输入用户名、密码、昵称，点击注册 | 注册成功跳转登录 |
| 用户登录 | 输入正确用户名密码，点击登录 | 登录成功跳转首页 |
| 发布文字 | 在发布页输入文字，点击发布 | 内容出现在首页 |
| 发布图片 | 选择图片文件，点击发布 | 图片显示在内容中 |
| 点赞 | 点击内容点赞按钮 | 点赞数+1，按钮变红 |
| 取消点赞 | 再次点击点赞按钮 | 点赞数-1，按钮恢复 |
| 评论 | 在内容下方输入评论，点击发表 | 评论显示在列表中 |
| 删除评论 | 点击自己评论的删除按钮 | 评论被删除 |
| 关注 | 点击关注按钮 | 按钮显示已关注 |
| 取消关注 | 再次点击关注按钮 | 按钮显示+关注 |
| 收藏 | 点击收藏按钮 | 按钮变星标 |
| 取消收藏 | 再次点击收藏按钮 | 按钮恢复 |
| 转发 | 点击转发按钮，输入转发语 | 转发成功显示原文 |

#### 边界情况测试
| 场景 | 测试步骤 | 预期结果 |
|------|----------|----------|
| 空内容发布 | 不输入内容直接发布 | 提示"内容不能为空" |
| 未登录点赞 | 退出登录后点击点赞 | 提示"请先登录" |
| 删除他人内容 | 登录A，尝试删除B的内容 | 提示"无权限" |
| 刷新页面 | 手动刷新浏览器 | 内容正常显示 |
| 无限滚动 | 滚动到页面底部 | 自动加载更多内容 |

### 11.3 手动测试脚本

```javascript
// 在浏览器控制台执行测试
// 1. 测试API调用
fetch('/api/post/list?page=1&size=10')
  .then(r => r.json())
  .then(console.log)

// 2. 测试登录
fetch('/api/user/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({username: 'aaa', password: 'aaa'})
})
  .then(r => r.json())
  .then(console.log)
```

### 11.4 已知问题

1. ~~**403 Forbidden**: 登录/注册接口返回403~~ - ✅ 已修复
   - 原因: CORS 配置问题
   - 状态: 已解决

2. **刷新页面无内容**: 手动刷新浏览器后内容不显示
   - 原因: Vue 组件加载问题
   - 状态: 待修复

3. **LSP 报错**: VSCode 显示 Lombok 相关错误
   - 原因: IDE 解析问题，实际编译正常
   - 状态: 可忽略

### 11.5 自动化测试

使用 `test.html` 进行自动化测试：

```bash
# 直接在浏览器中打开
E:\vscode\code\pcc2\test.html
```

测试页面功能：
1. 点击"测试 /api/post/list" - 验证获取帖子列表
2. 输入用户名/密码，点击"测试登录" - 验证登录功能
3. 点击"测试 /api/user/info" - 验证获取用户信息
4. 点击"测试 /api/post/user/2" - 验证获取用户帖子

---

## 12. 启动脚本

### 一键启动 (推荐)
```bash
E:\vscode\code\pcc2\start_project.bat
```

### 手动启动
```bash
# 终端1: 后端
cd E:\vscode\code\pcc2\backend
C:\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run

# 终端2: 前端
cd E:\vscode\code\pcc2\frontend
E:\Node\npm.cmd run dev
```

### 端口检查
```bash
netstat -ano | findstr "9090"  # 后端
netstat -ano | findstr "5173"  # 前端
```

### 关闭进程
```bash
taskkill /F /PID <进程ID>
```

---

## 13. 测试验证记录

### 服务启动验证
| 服务 | 端口 | 状态 | 验证时间 |
|------|------|------|----------|
| 后端 | 9090 | ✅ 运行中 | 2026-04-17 |
| 前端 | 5173 | ✅ 运行中 | 2026-04-17 |

### API 测试结果
| 接口 | 路径 | 状态 | 备注 |
|------|------|------|------|
| 获取帖子列表 | GET /api/post/list | ✅ 正常 | 返回帖子数据 |
| 前端代理 | GET /api/post/list (via 5173) | ✅ 正常 | 代理工作正常 |
| 登录接口 | POST /api/user/login | ✅ 正常 | 需要通过前端测试 |

### 测试步骤
1. 打开浏览器访问 http://localhost:5173
2. 使用 test.html 进行API测试，或直接在页面操作
3. 验证登录、发布、点赞、评论等功能

### 测试账号
- 用户名: aaa
- 密码: aaa

---

## 14. 当前状态

### 服务状态 (2026-04-17)
- ✅ 后端运行中: http://localhost:9090
- ✅ 前端运行中: http://localhost:5173

### 已验证的API
| API | 状态 | 说明 |
|-----|------|------|
| GET /api/post/list | ✅ 正常 | 返回帖子列表 |
| POST /api/user/register | ✅ 正常 | 需要username/password/nickname |
| POST /api/user/login | ✅ 正常 | 需要username/password |
| GET /api/user/info | ✅ 正常 | 需要Bearer token |

### 已知限制
- PowerShell curl测试存在编码问题，建议使用浏览器或test.html测试

---

## 15. 开发注意事项

### 端口与启动
- 后端端口: 9090 | 前端端口: 5173
- 如端口被占用: `netstat -ano | findstr "9090"` 然后 `taskkill /F /PID <进程ID>`

### 常见问题
- **403 Forbidden**: 检查 SecurityConfig 中的 CORS 配置
- **Lombok 报错**: VSCode 显示错误但编译正常，忽略 IDE 报错
- **刷新页面无内容**: 检查 Vue 组件加载和状态管理
- **前端缓存**: 修改代码后需 Ctrl+Shift+R 强制刷新

### 开发流程
1. 每次新功能开发前使用 brainstorming skill
2. 实现后使用 verification-before-completion skill 验证
3. 定期更新本规格说明书