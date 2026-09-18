# Occultism 存储控制器 → AE2 原生 MEStorage（已验证 API 事实）

本模组把 Occultism 的存储控制器以 AE2 `MEStorage` 暴露，结构对照 Applied-Mekanistics 的
`QioStorageAdapter` / `QioSupport`。下表每个签名都在下列依赖版本核对过源码/字节码。

- Occultism `1.205.0`（CurseForge file `7554225`）
- AE2 `19.2.17`（`org.appliedenergistics:appliedenergistics2`）
- NeoForge `21.1.244` / MC `1.21.1`
- 参考源码路径：工作区 `occultism/`、`Applied-Mekanistics/`、`api-sources/`

## 1. Occultism 侧

| API | 签名 / 出处 | 备注 |
|---|---|---|
| `StorageControllerBlockEntity` | `class ... extends NetworkedBlockEntity implements MenuProvider, IStorageController, ...` — `common/blockentity/StorageControllerBlockEntity.java:84` | 存储控制器方块实体 |
| `itemStackHandler` 字段 | `public StorageControllerMapItemStackHandler itemStackHandler = new ...` — 同上 `:96` | public，可直接取；类型是 `MapItemStackHandler` 子类 |
| `keyToCountMap()` | `Object2IntOpenHashMap<ItemStackKey> keyToCountMap()` — `common/misc/MapItemStackHandler.java:113` | 底层真实聚合表：一个 `ItemStackKey` → 总数（int，可超堆叠上限） |
| `insertItem(ItemStack,boolean)` | `@NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate)` — `MapItemStackHandler.java:325` | 返回剩余；`slot` 重载 `:306` 委托到它 |
| `extractItem(ItemStack,int,boolean)` | `@NotNull ItemStack extractItem(@NotNull ItemStack stack, int amount, boolean simulate)` — `MapItemStackHandler.java:422` | 内部 `ItemStackKey.of(stack)` 后走 `:389` |
| `ItemStackKey` | `public record ItemStackKey(ItemStack stack)`；`static of(ItemStack)` — `common/misc/ItemStackKey.java:6,10` | 访问器 `stack()`；record 的 equals/hashCode 按完整物品身份 |
| `getSlots()` / `totalItemCount()` | `MapItemStackHandler.java:290` / `:117` | 逐槽视图 vs 聚合总数 |

**先例（关键）**：Occultism 自己在 `StorageControllerBlockEntity.java:306-307` 就用
`itemStackHandler.keyToCountMap().object2IntEntrySet()` 遍历呈现内容——本模组 `getAvailableStacks`
与之完全同源，说明遍历聚合表是官方认可的内容读取路径，而非旁门。

## 2. 存储控制器方块变体

`StorageControllerBlock`（BE 均为 `StorageControllerBlockEntity`，见
`common/block/storage/StorageControllerBlock.java:109 newBlockEntity`）共 **4 个**，注册于
`registry/OccultismBlocks.java:539-568`：

| 方块 id | 说明 |
|---|---|
| `storage_controller` | 普通 |
| `storage_controller_stabilized` | 稳定化（`Rarity.EPIC`，容量更大） |
| `storage_controller_dark` | 黑暗配色（Otherrock） |
| `storage_controller_stabilized_dark` | 稳定化 + 黑暗 |

`storage_controller_base` / `_base_dark`（`:537`、`:555`）是普通 `Block`，**无方块实体**，
不应接入能力。因四个变体共用同一 BE 类型，能力注册必须覆盖全部 4 个方块 id（曾只注册
`storage_controller` 一个，属缺口）。

## 3. AE2 侧

| API | 签名 / 出处 | 备注 |
|---|---|---|
| `AECapabilities.ME_STORAGE` | `BlockCapability<MEStorage, @Nullable Direction>` | 本模组对控制器注册它 |
| `MEStorage` | `insert(AEKey,long,Actionable,IActionSource):long` / `extract(...)` / `getAvailableStacks(KeyCounter):void` / `getDescription():Component` | 适配器实现这四个 |
| `Actionable` | `enum`（class extends Enum）：`MODULATE` / `SIMULATE`；`isSimulate()`、`getFluidAction()`、`of(FluidAction)`、`ofSimulate(boolean)` — `appeng.api.config.Actionable`（javap 于 19.2.17 jar） | 用 `mode.isSimulate()` 透传给 Occultism handler 的 `simulate` |
| `AEItemKey` | `static of(ItemStack)`、`toStack()`、`toStack(int count)` | `insert` 里用 `toStack((int) min(amount, MAX_VALUE))` |
| `KeyCounter` | `add(AEKey, long)` | `getAvailableStacks` 输出容器 |

## 4. NeoForge 能力注册

| API | 签名 / 出处 |
|---|---|
| `RegisterCapabilitiesEvent.registerBlock` | `<T,C> void registerBlock(BlockCapability<T,C> cap, IBlockCapabilityProvider<T,C> provider, Block... blocks)` — `api-sources/.../RegisterCapabilitiesEvent.java:40` | **varargs**，一次可传多个方块 |
| provider 回调 | `(level, pos, state, be, side) -> ...`，按 `be instanceof StorageControllerBlockEntity` 判定并返回适配器或 `null` |

（另有 `registerBlockEntity(cap, BlockEntityType, provider)`（`:59`）可按 BE 类型注册，自动覆盖共用
该类型的所有方块；本模组沿用 Applied-Mekanistics 的 `registerBlock` 按方块 id 方式。）

## 5. 键缓存与并发

Applied-Mekanistics `QioStorageAdapter.java:36,116` 用 `static WeakHashMap<IHashedItem, AEItemKey>`
缓存 `AEItemKey.of(...)` 转换结果，避免每次轮询重复建 key。本模组对应缓存
`WeakHashMap<ItemStackKey, AEItemKey>`。前提：`ItemStackKey` 作为 key 存进 handler 后不再原地可变
（Occultism 自身也以其为 `Object2IntMap` 的 key，故成立）；AE2 存储访问在服务器线程进行。

## 6. 性能模型（为何用原生 MEStorage 而非逐槽 handler）

- 成本 = O(槽数) 全表扫描 + O(不同物品类型数) 差异比较；**单一物品数量**是合并的聚合值，读它 O(1)，
  只在经由槽数间接影响开销。
- 逐槽 `IItemHandler` 视图会把超过堆叠上限的物品摊成多个虚拟槽；`MEStorage` 直接给聚合总数。
- 原生 `MEStorage` 无变更监听时 `monitor == null` → `ITickingMonitor.sleepDevice()` → AE2 不轮询；
  对比外部存储（如 RS2 ExternalStorage）是主动轮询检测变化。
