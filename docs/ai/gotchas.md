# AI 协作坑

后续会话修改本模组前必读。环境/构建类问题见 `docs/troubleshooting.md`。

## 许可证：不是 MIT

- 代码结构改编自 Applied-Mekanistics（**LGPL-3.0**），本模组必须是
  **LGPL-3.0-or-later**，不得改回 MIT；受影响文件头部保留 SPDX 标识与出处注释。
- 若用户坚持要 MIT，唯一合规路径是**不看 Applied-Mekanistics 重写**整个适配器。
- Occultism 本身是 MIT，不构成约束。

## 改桥接代码前先读 docs/reference/

`docs/reference/occultism-mestorage-bridge.md` 是本模组所有跨模组 API 调用的
权威事实（Occultism 1.205.0 / AE2 19.2.17 / NeoForge 21.1.244，带 file:line
签名）。凭记忆写签名会错；升级依赖版本后需重新核对并更新该文档。

## 4 个控制器变体，字符串查 id

- 能力注册覆盖 `storage_controller`、`_stabilized`、`_dark`、
  `_stabilized_dark` 全部 4 个方块 id（共用同一 BE 类型）；
  `storage_controller_base` / `_base_dark` **无方块实体，不要接入**。
  历史上曾只注册 1 个 id，属缺口，勿回退。
- 方块 id 是 `BuiltInRegistries.BLOCK` 字符串查找 + `filter(Objects::nonNull)`，
  **编译期不检查**：Occultism 改名 → 静默 null → 能力缺失，游戏内只表现为
  总线读不到内容。升级 Occultism 后先对照
  `occultism/.../registry/OccultismBlocks.java` 确认 4 个 id 仍在。

## 验证边界

- `runClient` 手动验证由用户在游戏里做，**agent 不要启动游戏**。
- 无自动化测试；改动后至少保证 `./gradlew build` 绿。

## 注册元数据只改 gradle.properties

`neoforge.mods.toml` 是 `generateModMetadata` 的产物（模板在
`src/main/templates/`）。改版本/名字/依赖范围只改 `gradle.properties`，
不手改生成的 toml。
