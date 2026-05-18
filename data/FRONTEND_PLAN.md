# 前端架构规划 — Next.js 个人博客

## 一、路由结构 (App Router)

```
app/
├── layout.tsx              # 根布局: 导航栏 + 音乐播放器 + 页脚
├── page.tsx                # 首页: 文章列表 + 数据看板
│
├── article/
│   ├── page.tsx            # 文章列表(分页+筛选+搜索)
│   └── [id]/
│       └── page.tsx        # 文章详情(Markdown渲染+TOC)
│
├── project/
│   ├── page.tsx            # 项目展示列表
│   └── [id]/
│       └── page.tsx        # 项目详情(Markdown+链接)
│
├── timeline/
│   └── page.tsx            # 学习时间线 + 技能图谱
│
├── about/
│   └── page.tsx            # 关于我
│
├── literature/
│   ├── page.tsx            # 文学作品列表
│   └── [id]/
│       └── page.tsx        # 作品详情
│
├── auth/
│   ├── login/
│   │   └── page.tsx        # 登录页(密码+GitHub)
│   ├── register/
│   │   └── page.tsx        # 注册页
│   └── callback/
│       └── page.tsx        # GitHub OAuth 回调处理
│
├── admin/                  # 管理端(需登录)
│   ├── layout.tsx          # 管理端侧边栏布局 + 鉴权守卫
│   ├── page.tsx            # 管理首页(快捷入口)
│   ├── article/
│   │   ├── page.tsx        # 文章列表(含草稿)
│   │   ├── new/
│   │   │   └── page.tsx    # 新建文章(Markdown编辑器)
│   │   └── [id]/
│   │       └── edit/
│   │           └── page.tsx # 编辑文章
│   ├── project/
│   │   ├── page.tsx        # 项目列表
│   │   ├── new/page.tsx
│   │   └── [id]/edit/page.tsx
│   ├── timeline/
│   │   └── page.tsx        # 时间线管理
│   ├── skill/
│   │   └── page.tsx        # 技能管理
│   ├── category/
│   │   └── page.tsx        # 分类管理
│   ├── tag/
│   │   └── page.tsx        # 标签管理
│   ├── media/
│   │   └── page.tsx        # 媒体库(上传+浏览+删除)
│   └── about/
│       └── page.tsx        # 编辑关于我
│
└── api/                    # Next.js API路由(可选)
    └── auth/
        └── route.ts        # 服务端代理(如需要)
```

## 二、组件树

### 公共组件
```
components/
├── layout/
│   ├── Header.tsx          # 顶栏(Logo+导航+主题切换)
│   ├── Footer.tsx          # 页脚
│   └── MusicPlayer.tsx     # 底部迷你播放器(固定)
│
├── article/
│   ├── ArticleCard.tsx     # 文章卡片(列表项)
│   ├── ArticleList.tsx     # 文章列表容器(分页+筛选)
│   ├── ArticleContent.tsx  # Markdown渲染组件
│   ├── ArticleTOC.tsx      # 目录导航(从h1-h3提取)
│   └── ArticleNav.tsx      # 上一篇/下一篇导航
│
├── project/
│   ├── ProjectCard.tsx     # 项目卡片
│   └── ProjectList.tsx     # 项目列表容器
│
├── comment/
│   ├── CommentList.tsx     # 评论列表
│   ├── CommentItem.tsx     # 单条评论
│   └── CommentForm.tsx     # 评论表单(需登录/未登录提示)
│
├── timeline/
│   ├── Timeline.tsx        # 时间轴组件
│   └── SkillBar.tsx        # 技能进度条
│
├── common/
│   ├── Pagination.tsx      # 分页器
│   ├── TagCloud.tsx        # 标签云
│   ├── CategoryNav.tsx     # 分类导航
│   ├── SearchBar.tsx       # 搜索框
│   ├── ThemeToggle.tsx     # 主题切换按钮
│   ├── LoginModal.tsx      # 登录弹窗
│   ├── Loading.tsx         # 加载态
│   └── ErrorBoundary.tsx   # 错误边界
│
├── admin/
│   ├── AdminLayout.tsx     # 管理端侧边栏
│   ├── MarkdownEditor.tsx  # Markdown编辑器(编辑+预览)
│   ├── ImagePicker.tsx     # 图片选择器(调媒体库)
│   ├── MediaUpload.tsx     # 文件上传组件
│   └── Dashboard.tsx       # 管理首页统计卡片
│
└── auth/
    ├── LoginForm.tsx       # 密码登录表单
    └── GitHubButton.tsx    # GitHub登录按钮
```

## 三、状态管理 (Zustand)

```
stores/
├── authStore.ts      # { user, token, isLoggedIn, login(), logout(), register() }
├── themeStore.ts     # { theme: 'light'|'dark', toggle() }
├── musicStore.ts     # { currentTrack, isPlaying, toggle(), fetchRandom() }
└── uiStore.ts        # { sidebarOpen, loginModalOpen, ... }
```

## 四、API 层 (Axios)

```
lib/
├── axios.ts           # Axios实例: baseURL, 自动挂JWT, 401拦截跳登录
├── api/
│   ├── auth.ts        # login, register, githubLogin, getMe
│   ├── article.ts     # getPublicList, getPublicDetail, getAdminList, create, update, delete, publish, unpublish, pin, addView
│   ├── project.ts     # getPublicList, getPublicDetail, getAdminList, create, update, delete, publish, unpublish
│   ├── comment.ts     # getList, create
│   ├── timeline.ts    # getList, create, update, delete
│   ├── skill.ts       # getList, create, update, delete
│   ├── category.ts    # getList, create, update, delete
│   ├── tag.ts         # getList, create, update, delete
│   ├── about.ts       # get, update
│   ├── media.ts       # upload, getList, download, delete, batchDelete
│   ├── dashboard.ts   # get
│   └── op.ts          # getCategories, getArticleList, getMusic
└── types/
    └── index.ts       # TypeScript 类型定义(对应所有VO)
```

## 五、关键交互流程

### 5.1 登录流程
```
用户点击登录 → LoginModal弹出
  ├── 密码登录: 输入用户名密码 → POST /api/auth/login → 存token到authStore+localStorage
  └── GitHub: GET /api/auth/github → window.location跳转 → 回调/callback?token=xxx → 存token
登录后: CommentForm可用, 管理入口显示
```

### 5.2 文章阅读流程
```
首页/列表页 → 滚动加载 ArticleCard → 点击
  → /article/[id] → GET /api/article/public/{id}
  → ArticleContent渲染Markdown + ArticleTOC目录 + ArticleNav上下篇
  → 页面底部 CommentList + CommentForm(需登录)
  → 自动 PUT /api/article/{id}/view (阅读量+1)
```

### 5.3 Markdown编辑器(管理端)
```
MarkdownEditor 分屏: 左侧编辑 | 右侧预览
  ├── 工具栏: 粗体/斜体/标题/代码块/链接/图片
  ├── 图片按钮 → ImagePicker → 调 POST /api/media/upload → 插入 ![](url)
  └── 保存按钮 → POST|PUT /api/article
```

### 5.4 音乐播放器
```
MusicPlayer (固定在 layout 底部)
  GET /op/music → { 曲名, 歌手, 封面URL, 音频URL }
  <audio src={MinIO_URL}> → 播放/暂停按钮
  下一首 → 重新 GET /op/music (随机)
```

## 六、响应式布局

| 断点 | 宽度 | 布局 |
|------|------|------|
| 移动端 | < 768px | 单列, 侧边栏隐藏, 卡片全宽 |
| 平板 | 768-1024px | 两列, 侧边栏可折叠 |
| 桌面 | > 1024px | 主内容区 + 侧边栏(TOC/标签云) |

## 七、主题切换

CSS Variables 方案:
```css
:root { --bg: #fff; --text: #1a1a1a; --card: #f5f5f5; ... }
[data-theme="dark"] { --bg: #1a1a1a; --text: #e5e5e5; --card: #2a2a2a; ... }
```
Zustand themeStore 管理, localStorage 持久化。

## 八、技术要点

| 功能 | 实现 |
|------|------|
| Markdown渲染 | react-markdown + rehype-highlight + remark-gfm |
| TOC目录 | 解析Markdown h1-h3, 生成锚点导航 |
| 代码高亮 | Prism.js (Java/TypeScript/Python/SQL/Bash) |
| 图片懒加载 | Next.js Image 组件 |
| SEO | 不做SEO, 纯客户端渲染 |
| 部署 | 静态导出或 Node.js 服务, 与后端同机 |

## 九、开发顺序建议

```
Phase 1: 公共基础
  layout/Header/Footer, ThemeToggle, Axios实例, Zustand stores, 类型定义

Phase 2: 访客端核心
  首页(文章列表+看板) → 文章详情+评论 → 项目列表+详情

Phase 3: 访客端辅助
  时间线+技能 → 关于我 → 文学+音乐

Phase 4: 认证
  注册+密码登录+GitHub登录 → 评论表单联动

Phase 5: 管理端
  文章CRUD+Markdown编辑器 → 项目管理 → 媒体库
  → 分类标签 → 时间线技能 → 关于我编辑
```
