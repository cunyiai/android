# 🩺 村医AI - 离线AI问诊助手

专为农村老年人设计的、完全支持离线端侧运行的智能健康辅助 Android 应用。

⚠️ 重要开源申明 (Non-Profit & Open Source Statement)

本项目属于纯公益性质（Public Welfare Project），主要致力于为中国偏远乡村及医疗资源匮乏地区的留守老年人提供普惠的端侧 AI 医疗辅助服务。

严禁任何个人、企业或组织将本项目的源代码、UI 设计、预置模型资产用于任何商业化变现或盈利目的。 本代码仅作学习、交流与公益性捐助用途。



## ✨功能特性

| 功能 | 描述 |
|------|------|
| **🆘 一键求救** | 按下SOS按钮，拨打120并通知家人 |
| **💬 语音问诊** | 对着手机说话，AI分析症状并给出建议 |
| **📷 拍照识药** | 拍摄药盒，识别药品名称，查看用药说明 |
| **📊 慢病管理** | 记录血压、血糖、心率，趋势分析，异常预警 |
| **💊 药物冲突检测** | 自动检查药物相互作用，提前预警 |

## 📲技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material 3
- **AI引擎**: Google LiteRT-LM (Gemma 2/3/4) + ML Kit AICore
- **数据库**: Room
- **依赖注入**: Hilt
- **构建**: Gradle 8.13 + AGP 8.9.2
- **最低支持**: Android 8.0 (API 26)

## 🔧编译说明

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

## 🏗️项目结构

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

## 📦参考项目

基于 [coder-brzhang/gemma-ai](https://github.com/coder-brzhang/gemma-ai) 改建。

## ⚠️ 免责声明 / Disclaimer

## 医疗免责 / Medical Disclaimer

## 1.本应用不构成医疗诊断或治疗建议。
村医 AI 提供的所有内容（包括但不限于症状分析、用药建议、健康评估）均为 AI 模型生成的参考性信息，绝不替代专业医疗人员的诊断、处方或治疗方案。
## 2.AI 输出可能存在错误。 
端侧大语言模型存在幻觉（hallucination）风险，可能生成不准确、不完整或具有误导性的内容。用户不应仅依据本应用的输出做出任何医疗决策。
## 3.紧急情况请立即拨打 120 或前往最近医疗机构。
本应用的一键求救功能仅为辅助通知手段，不能保证救援及时到达，不承担任何因延迟就医导致的后果。
## 4.用药信息请以药品说明书及医嘱为准。
拍照识药功能受限于模型能力与本地数据库覆盖范围，可能存在识别错误或遗漏药物相互作用的情况。
## 5.使用本应用即表示您已知晓并自愿承担上述风险。

## 商业化禁止 / Commercial Use Prohibition

## 1.严禁商业用途。 
本项目及其全部衍生作品（包括但不限于源代码、UI 设计、预置模型资产、数据库）仅供学习、交流与公益性用途，不得用于任何形式的商业变现，包括但不限于：售卖、订阅收费、嵌入付费产品、广告变现、SaaS 服务等。
## 2.严禁医疗产品化。
未经相关国家药品监督管理部门审批，不得将本项目或其衍生作品作为医疗器械、诊断工具或任何受监管医疗产品进行生产、销售或推广。
## 3.责任自负。 
任何违反上述条款将本项目用于商业或医疗产品化用途的个人、企业或组织，由此产生的一切法律责任、经济损失及人身伤害后果，均由违规方自行承担，与本项目的原作者、贡献者及关联方无关。
## 4.原作者保留追究权利。
对于违反本声明的商业侵权行为，项目原作者保留采取法律手段追究的权利。

## 📄许可

Apache License 2.0
