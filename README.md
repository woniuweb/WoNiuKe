# WoNiuKe

<p align="center">
  <a href="http://152.136.161.128" target="_blank">
    <img src="https://img.shields.io/badge/Demo-在线预览-blue?style=flat-square" alt="Demo">
  </a>
  <img src="https://img.shields.io/badge/JDK-1.8+-green?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.2.7-brightgreen?style=flat-square" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-2.6.11-4FC08D?style=flat-square&logo=vue.js" alt="Vue">
  <img src="https://img.shields.io/badge/MySQL-5.7+-blue?style=flat-square&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-5.0+-red?style=flat-square&logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License">
</p>

> 🚀 一个基于 Spring Boot + Vue 的前后端分离个人博客系统

## 📖 简介

WoNiuKe 是一个前后端分离的个人博客系统，采用 Spring Boot + Vue 技术栈开发。前台界面简洁美观，后台管理功能完善，支持 Markdown 编辑器、多种图床配置、评论系统、访客统计等功能。基于NBlog（https://github.com/Naccl/NBlog）项目二开而来，进行了部分修改。

**在线预览：** [http://152.136.161.128](http://152.136.161.128)

## ✨ 功能特性

### 前台展示
- 📝 文章展示与搜索
- 🏷️ 分类与标签
- 📅 文章归档
- 💬 评论功能
- 🎯 动态说说
- 🔗 友情链接
- 👤 关于我页面

### 后台管理
- 📊 仪表盘数据统计
- 📝 文章管理（Markdown 编辑器）
- 💬 评论管理与审核
- 🏷️ 分类/标签管理
- 📢 动态管理
- 🔗 友链管理
- ⚙️ 站点设置
- 🖼️ 图床管理（GitHub / 又拍云 / 腾讯云）
- 📈 访客统计
- 📋 日志管理（登录/操作/异常/访问日志）
- ⏰ 定时任务

### 系统特性
- 🔐 JWT 身份认证
- 🛡️ Spring Security 权限控制
- 📧 评论邮件通知
- 🚦 接口访问限流
- 📊 访问量统计
- 🔍 IP 归属地解析
- 📱 响应式布局

## 🛠️ 技术栈

### 后端
| 技术 | 说明 | 版本 |
|------|------|------|
| Spring Boot | 核心框架 | 2.2.7 |
| Spring Security | 安全框架 | - |
| MyBatis | ORM 框架 | 2.1.3 |
| PageHelper | 分页插件 | 1.2.12 |
| MySQL | 数据库 | 5.7+ |
| Redis | 缓存 | 5.0+ |
| JWT | 身份认证 | 0.9.1 |
| Quartz | 定时任务 | - |
| Thymeleaf | 邮件模板 | - |
| ip2region | IP 解析 | 2.6.5 |

### 前端
| 技术 | 说明 | 版本 |
|------|------|------|
| Vue | 前端框架 | 2.6.11 |
| Vue Router | 路由管理 | 3.3.4 |
| Vuex | 状态管理 | 3.5.1 |
| Axios | HTTP 客户端 | 0.24.0 |
| Element UI | UI 组件库 | 2.13.2 |
| Semantic UI | UI 框架（前台） | 2.4.1 |
| ECharts | 图表库 | 4.9.0 |
| Mavon Editor | Markdown 编辑器 | 2.9.1 |

## 📁 项目结构

```
WoNiuKe
├── blog-api          # 后端 Spring Boot 项目
│   ├── src/main/java
│   │   └── top/naccl
│   │       ├── config        # 配置类
│   │       ├── controller    # 控制器
│   │       ├── entity        # 实体类
│   │       ├── mapper        # MyBatis Mapper
│   │       ├── service       # 业务逻辑
│   │       └── util          # 工具类
│   └── src/main/resources
│       ├── mapper            # MyBatis XML
│       └── application.yml   # 配置文件
├── blog-cms          # 后台管理 Vue 项目
│   └── src
│       ├── api               # API 接口
│       ├── components        # 公共组件
│       ├── router            # 路由配置
│       ├── store             # Vuex 状态管理
│       └── views             # 页面组件
└── blog-view         # 前台展示 Vue 项目
    └── src
        ├── api               # API 接口
        ├── components        # 公共组件
        ├── router            # 路由配置
        ├── store             # Vuex 状态管理
        └── views             # 页面组件
```

## 📄 开源协议

本项目基于 [MIT](LICENSE) 协议开源。

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element UI](https://element.eleme.io/)
- [Semantic UI](https://semantic-ui.com/)

---
