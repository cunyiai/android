# 村医AI - 离线AI问诊助手

专为农村老年人设计的、完全支持离线端侧运行的智能健康辅助 Android 应用。

## 功能特性

- **离线AI问诊** - 基于 Google LiteRT-LM (Gemma) 端侧推理，无需网络即可解答日常疾病疑问
- **AI双引擎** - 优先使用 ML Kit AICore，兜底使用 LiteRT-LM 离线引擎
- **智能用药指导** - 本地药物数据库，支持药物相克检测、用法用量查询
- **拍照识药** - CameraX 拍照 + ML Kit 中文 OCR，识别药品说明书
- **语音交互** - 本地语音识别输入 + TTS 语音播报，方便老年人使用
- **适老化UI** - 大字体、高对比度、简化操作流程的 Jetpack Compose 界面
- **健康档案** - 本地存储个人健康信息、血压血糖记录
- **一键SOS** - 紧急求助功能，发送位置信息给紧急联系人
- **周报生成** - WorkManager 定时生成健康周报

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material 3
- **AI引擎**: Google LiteRT-LM (Gemma 2/3/4) + ML Kit AICore
- **数据库**: Room
- **依赖注入**: Hilt
- **构建**: Gradle 8.13 + AGP 8.9.2
- **最低支持**: Android 8.0 (API 26)

## 编译说明

### 环境要求

1. Android Studio Hedgehog (2023.1.1) 或更高版本
2. Android SDK: compileSdk 36
3. JDK 17

### 构建步骤

1. 用 Android Studio 打开本项目根目录
2. 等待 Gradle Sync 完成（首次会自动下载依赖和 Gradle Wrapper）
3. 连接 Android 设备或启动模拟器
4. 点击 Run > Run 'app'

### 命令行构建

```bash
# 确保 Android SDK 已配置
./gradlew assembleDebug
```

生成的 APK 位于: `app/build/outputs/apk/debug/app-debug.apk`

## 项目结构

```
app/src/main/java/com/brzhang/gemma_ai/
├── MainActivity.kt          # 主 Activity，导航入口
├── VillageDocApp.kt         # Application 类
├── ai/
│   ├── GemmaManager.kt      # AI 推理管理（LiteRT-LM + AICore 双引擎）
│   └── PromptTemplates.kt   # 中文提示词模板
├── data/
│   ├── RulesEngine.kt       # 药物相克规则引擎
│   ├── db/                  # Room 数据库（药物、聊天记录、健康档案、用户）
│   ├── entity/              # 数据实体
│   └── model/               # 数据模型（诊断、药物）
├── network/
│   └── ModelDownloader.kt   # AI 模型下载管理
├── service/
│   ├── AudioRecorderService.kt  # 录音服务
│   └── WeeklyReportWorker.kt    # 周报定时任务
├── ui/
│   ├── components/          # 公共 UI 组件（聊天消息、录音按钮）
│   ├── theme/               # 主题配色
│   ├── health/              # 健康档案页面
│   ├── home/                # 首页
│   ├── medicine/            # 用药指导页面
│   ├── setup/               # 初始化设置页面
│   ├── sos/                 # SOS 紧急求助页面
│   └── voice/               # 语音对话页面
└── util/
    ├── JsonParser.kt        # JSON 解析工具
    └── TtsHelper.kt         # 文字转语音工具
```

## 参考项目

基于 [coder-brzhang/gemma-ai](https://github.com/coder-brzhang/gemma-ai) 改建。

## 许可

Apache License 2.0
