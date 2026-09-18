# 环境 / 构建 / 运行问题排查

## JDK 版本

构建必须用 **Java 21**。本机临时切换：

```bash
export JAVA_HOME=C:/Users/lzp/scoop/apps/dragonwell21-jdk/current
./gradlew build
```

用错 JDK（如 17）会在编译或 NeoForm 阶段报版本类错误。

## 中文模板与 generateModMetadata（重要）

`src/main/templates/META-INF/neoforge.mods.toml` 含中文注释。`build.gradle` 的
`generateModMetadata` 里必须保留 `filteringCharset = 'UTF-8'`：它默认取平台
字符集，Windows 下若 daemon 继承 GBK，中文注释会被转成非法 UTF-8 字节，FML
解析报 `ParsingException` → `is not a valid mod file`，模组完全无法加载
（工作区 Maid-Deep-Mob-Learning 1.0.1 曾因此翻车）。发布前抽查 jar 内 toml
是否为合法 UTF-8。

## 沙箱权限

- Gradle 需写 `~/.gradle`（工作区之外）：受限沙箱下 wrapper 解压/缓存会报
  `FileNotFoundException ... 拒绝访问`，需以更宽权限原样重试同一条命令。
- `git push` 同样可能需要更宽权限。

## Git 远端只走 HTTPS

本机 SSH 22 端口连 github.com 超时，远端一律
`https://github.com/pasze888/occultismae2bridge.git`，不要用
`git@github.com:` 形式。

## 游戏内：总线读不到控制器内容

按顺序检查：

1. 启动日志里 AE2 / Occultism / 本模组三者是否都加载成功。
2. 贴的是否为 4 个带方块实体的控制器变体之一（底座类没有库存）。
3. 若升级过 Occultism，确认 4 个方块 id 字符串查找没有落空（见
   `docs/ai/gotchas.md`，静默 null 是本问题最常见根因）。
