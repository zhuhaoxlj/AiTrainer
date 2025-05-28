<p align="center"><img width="180" src="https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041430838.png" alt="logo"></p>



<h2 align="center">AiTrainer</h2>

README English | [Chinese](https://github.com/zhuhaoxlj/AiTrainer/blob/hot_fix/README-CN.md)

## Status

> The current project has stopped maintenance, and the latest version is hot_ fix branch.

## Introduction

AiTrainer is an intelligent fitness system combining artificial intelligence machine vision, using Google's open-source mobile machine learning kit [ML Kit](https://developers.google.cn/ml-kit ).The BlazePose neural network model is used to detect the key points of the human body in real time, and the pose of the human body is estimated through Mediapipe. Through the intelligent fitness system, you can obtain your fitness status (movement standard, movement completion, calorie consumption, etc.). At the same time, the intelligent fitness system provides users with specific fitness programs and evaluation of fitness actions to help users exercise more efficiently.

### System module structure diagram

![image-20230104145059649](https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041450714.png)

### Project preview

<p align="center">
	<img src="https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041459061.png" width="30%" />
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	<img src="https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041457593.png" width="30%" />
	<br/>
	<font color="AAAAAA">001.jpg</font>
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	<font color="AAAAAA">002.jpg</font>
</p>

<p align="center">
	<img src="https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041458017.png" width="30%" />
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	<img src="https://markgosling.oss-cn-beijing.aliyuncs.com/img/202301041458521.png" width="30%" />
	<br/>
	<font color="AAAAAA">003.jpg</font>
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
	<font color="AAAAAA">004.jpg</font>
</p>

### Project demo

[Android AiTrainer Demo Video_bilibili](https://www.bilibili.com/video/BV1JZ4y1177W/?spm_id_from=333.999.0.0&vd_source=2560cad70be7bb6cc853f08dd5ef5e6d)

## Installation package

https://github.com/zhuhaoxlj/AiTrainer/blob/hot_fix/AiTrainer.apk

## Documentation

For detailed documents, please visit https://zhuhaoxlj.github.io/.

## Reference documents

Grishchenko, Ivan, Valentin Bazarevsky, Andrei Zanfir, Eduard Gabriel Bazavan, Mihai Zanfir, Richard Yee, Karthik Raveendran, Matsvei Zhdanovich, Matthias Grundmann and Cristian Sminchisescu. 《BlazePose GHUM Holistic: Real-Time 3D Human Landmarks and Pose Estimation》. arXiv, June 23, 2022. http://arxiv.org/abs/2206.11678.

## License

Copyright 2022-present MarkGosling.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## 自动发布配置

本项目使用 GitHub Actions 自动构建和发布 Android 应用。当推送带有 `v` 前缀的标签（如 `v1.0.0`）时，会自动触发构建并创建 GitHub Release。

### 设置步骤

1. **创建签名密钥** (如果你还没有):

   ```bash
   keytool -genkey -v -keystore release-key.keystore -alias aitrainer -keyalg RSA -keysize 2048 -validity 10000
   ```
   
   按照提示输入信息，可以使用以下默认值：
   - 密钥库密码：aitrainer
   - 密钥别名：aitrainer
   - 密钥密码：aitrainer

2. **将签名密钥转换为 Base64 格式**:

   ```bash
   # macOS
   base64 -i release-key.keystore | pbcopy
   
   # Linux
   base64 -w 0 release-key.keystore | xclip -selection clipboard
   
   # Windows (PowerShell)
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.keystore")) | Set-Clipboard
   ```

3. **在 GitHub 仓库中设置 Secrets**:

   - 打开仓库 -> Settings -> Secrets and variables -> Actions
   - 添加以下 Secrets:
     - `SIGNING_KEY`: 上一步生成的 Base64 编码字符串
     - `ALIAS`: 密钥别名 (例如: `aitrainer`)
     - `KEY_STORE_PASSWORD`: 密钥库密码 (例如: `aitrainer`)
     - `KEY_PASSWORD`: 密钥密码 (例如: `aitrainer`)
     - `PERSONAL_ACCESS_TOKEN`: 具有仓库写入权限的个人访问令牌（用于版本更新工作流）

### 发布新版本

有两种方式发布新版本：

#### 方式一：手动创建标签

```bash
git tag v1.0.0
git push origin v1.0.0
```

推送标签后，GitHub Actions 会自动构建并发布新版本。

#### 方式二：使用版本更新工作流

1. 在 GitHub 仓库页面，点击 "Actions" 标签
2. 在左侧选择 "Update App Version" 工作流
3. 点击 "Run workflow" 按钮
4. 输入新的版本名称（例如：1.0.0）和版本号（例如：10）
5. 点击 "Run workflow" 按钮启动工作流

#### 方式三：手动触发构建流程

1. 在 GitHub 仓库页面，点击 "Actions" 标签
2. 在左侧选择 "Android Release" 工作流
3. 点击 "Run workflow" 按钮
4. 点击绿色的 "Run workflow" 按钮启动构建流程

### 调试构建问题

如果构建失败，可以查看 Actions 日志以了解具体原因。常见问题包括：

- 签名密钥不正确：确保 `SIGNING_KEY` 是正确的 Base64 编码字符串
- 密钥别名或密码错误：确保 `ALIAS`、`KEY_STORE_PASSWORD` 和 `KEY_PASSWORD` 与密钥设置一致
- 构建脚本问题：检查 .github/workflows/android-release.yml 文件

### 自定义构建

如需自定义构建过程，请修改以下文件：

- `.github/workflows/android-release.yml` - 发布工作流
- `.github/workflows/update-version.yml` - 版本更新工作流
- `app/build.gradle` - 应用构建配置
