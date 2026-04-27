# Repository Guidelines

## 项目结构与模块组织
`src/main/java/net/frozenorb/potpvp` 是主插件代码，按 `arena`、`match`、`party`、`queue`、`command`、`listener` 等功能分包。`src/main/kotlin/com/qrakn/morpheus` 保存 Morpheus 事件与游戏逻辑。`src/main/resources` 放 Bukkit 资源文件，重点是 `plugin.yml` 和 `config.yml`。`docs/` 记录 arena 和 duel 设计说明；`lib/` 存放 `server.jar`、`qLib.jar`、`WorldEdit.jar`、`ProtocolLib.jar` 等本地依赖；`target/` 是构建输出，不应手工修改。

## 构建、测试与开发命令
先确保 `JAVA_HOME` 可用，否则 Maven 无法启动。

- `mvn clean package`：编译 Java/Kotlin 并生成插件包到 `target/`
- `mvn -DskipTests package`：快速打包，适合只验证编译与资源过滤
- `mvn test`：执行测试阶段；当前仓库没有提交 `src/test`，因此它更像一次构建冒烟检查

本地联调通常是在 Spigot 1.7.10 测试服中替换 `plugins/` 下产物后验证。

## 代码风格与命名约定
遵循现有风格：Java 8、Kotlin 1.3.11、4 空格缩进，不混用大规模格式化。包名全小写，类名使用 UpperCamelCase。命令、监听器、处理器保持后缀一致，例如 `SpeedCommand`、`MatchGeneralListener`、`QueueHandler`。新增功能优先放进现有功能分包，避免创建含义模糊的 `util` 扩散点。

## 测试规范
新增纯逻辑代码时，在 `src/test/java` 或 `src/test/kotlin` 补充小而集中的单元测试；涉及 Bukkit 生命周期、菜单点击、队列、决斗或 arena 分配的改动，至少记录手动验证步骤。当前没有覆盖率门槛，但 PR 需要说明你验证了哪些场景。

## 提交与 Pull Request 规范
Git 历史以简短英文摘要为主，通常以动词开头，例如 `Added bot system.`、`Optimize GUI logic.`。保持一次提交只处理一个主题。PR 说明应包含：变更摘要、受影响模块、配置或资源文件变更、手动验证结果；如果修改菜单或 GUI，附截图或录屏更易审查。

## 配置与安全提示
`src/main/resources/config.yml` 包含 Mongo 和 Redis 连接信息，不要提交生产环境凭据。修改 `lib/` 中的系统依赖版本时，要同时确认 `pom.xml` 与目标服务端版本兼容。
