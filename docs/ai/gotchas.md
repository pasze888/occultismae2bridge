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

## Occultism 的 4 个前置必须显式声明，且用 localRuntime

`curse.maven:occultism-361026:<fileID>` 这类坐标**没有 POM，依赖不传递**，
Occultism 自己声明的 modonomicon / geckolib / curios / smartbrainlib 不会自动带进来。
漏掉就在启动时炸在 ModSorter 阶段，日志形如：

```
Missing or unsupported mandatory dependencies:
	Mod ID: 'modonomicon', Requested by: 'occultism', Expected range: '[1.117.2,)', Actual version: '[MISSING]'
```

- 四处依赖写在 `build.gradle` 的 `dependencies`（`build.gradle:91-94`），用 **`localRuntime`**
  而不是 `implementation`：本模组不 import 这四个库的任何类，它们只是本地运行时的加载前置，
  用 `implementation` 会把它们塞进编译类路径并作为依赖对外发布。
  `localRuntime` 由 `runtimeClasspath.extendsFrom localRuntime` 接进运行时（MDK 自带）。
- **fileID 直接写死**，版本下限写在行尾注释里。不引 `gradle.properties` 变量：这些 fileID 是
  一次性查出来的，没有第二处引用，多一层变量只会增加跳转。`occultism_file_id` 是项目原有写法，
  保持不动。
- 升级 Occultism 后要重新核对：Occultism jar 的 `META-INF/neoforge.mods.toml` 里
  `[[dependencies.occultism]]` 段给出了各前置的 `versionRange` 下限，照它取版本。

