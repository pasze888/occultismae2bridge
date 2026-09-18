# Occultism AE2 Bridge

[English](README.md) | [简体中文](README.zh-CN.md)

NeoForge 1.21.1 加法模组（addon）：把 **Occultism 的存储控制器**以原生
`MEStorage` 暴露给 **Applied Energistics 2**，让 AE2 存储总线直接读写控制器
底层哈希表的真实聚合数量——每种物品一条记录，数量可超过堆叠上限——而不是
逐槽的 `IItemHandler` 视图。

纯行为模组：不注册任何自己的方块、物品或其他内容。

## 为什么用原生 MEStorage

- 逐槽视图会把数量超过堆叠上限的物品摊成多个虚拟槽；`MEStorage` 直接给出
  聚合总数。
- 成本为 O(槽数) 全表扫描 + O(不同物品类型数) 差异比较；单一物品的数量是
  聚合值，读取 O(1)。
- 无变更监听时 AE2 会让总线休眠（`ITickingMonitor`），不像外部存储桥接那样
  主动轮询。

结构上对照官方 [Applied-Mekanistics](https://github.com/ramidzkh/Applied-Mekanistics)
的 QIO 适配器（许可方面的影响见 `docs/ai/gotchas.md`）。

## 运行需求

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.244 及以上 |
| Applied Energistics 2 | 19.2.17 及以上 |
| Occultism | 1.205.0 及以上 |

AE2 与 Occultism 均为必需的前置依赖（`AFTER`）。

## 安装

把 `occultismae2bridge-<version>.jar` 放入 `mods/` 目录，与 NeoForge 1.21.1、
AE2、Occultism 一起使用即可，无其他步骤。

## 使用方法

1. 放置任意存储控制器变体——普通、稳定化、黑暗、稳定化黑暗。四种全部支持；
   底座/基座方块没有库存，刻意排除。
2. 将 AE2 存储总线朝向控制器。
3. 控制器内容以聚合物品类型出现在 ME 网络中，存入/取出直接进入控制器的
   哈希表存储。总线过滤器照常生效。

## 配置

无配置。本模组没有配置文件、没有任何注册内容，只为控制器提供
`ME_STORAGE` 方块能力。

## 常用命令

在本项目目录内执行：

```bash
./gradlew build        # 编译 + 打包 mod jar（build/libs/）
./gradlew runClient    # 开发版客户端（首次会下载运行环境）
./gradlew runServer    # 开发版服务器
```

## 文档

- `docs/reference/occultism-mestorage-bridge.md` —— 已验证的 API 事实
  （Occultism / AE2 / NeoForge 签名及源码位置）。改动桥接代码前先读它。
- `docs/ai/gotchas.md` —— 后续会话需要避免的协作坑。
- `docs/troubleshooting.md` —— 构建 / 环境问题。

## 许可证

LGPL-3.0-or-later。部分代码改编自
[Applied-Mekanistics](https://github.com/ramidzkh/Applied-Mekanistics)
（Copyright ramidzkh，LGPL-3.0-or-later），涉及的源文件内已标注出处。
