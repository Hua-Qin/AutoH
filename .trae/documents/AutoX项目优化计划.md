# AutoX.js v7 项目优化计划

## 任务概述

本计划旨在完成以下两个核心优化任务：
1. 移除项目中的白名单限制功能，确保用户可以查看所有应用的节点内容
2. 解决控件组件的混淆问题，优化控件的识别与使用体验

---

## 一、白名单机制分析

### 1.1 白名单相关代码定位

经过详细分析，发现白名单机制涉及以下关键文件：

| 文件路径 | 作用 | 关键代码位置 |
|---------|------|-------------|
| `autojs/src/main/java/com/stardust/autojs/runtime/accessibility/AccessibilityConfig.java` | 白名单配置核心类 | 第21行：`mWhiteList` 列表定义 |
| `autojs/src/main/java/com/stardust/autojs/core/accessibility/UiSelector.java` | 使用白名单过滤节点 | 第111-114行：白名单检查逻辑 |
| `app/src/main/java/org/autojs/autojs/autojs/AutoJs.java` | 添加应用到白名单 | 第173-179行：添加酷安到白名单 |

### 1.2 白名单工作原理

```
用户执行控件查找
    ↓
UiSelector.findImpl() 被调用
    ↓
遍历窗口根节点
    ↓
检查 root.getPackageName() 是否在白名单中
    ↓
如果在白名单 → 返回空集合（阻止访问）
如果不在白名单 → 正常返回节点信息
```

### 1.3 白名单移除方案

**方案：完全移除白名单检查逻辑**

修改文件清单：
1. **AccessibilityConfig.java** - 移除白名单相关字段和方法
2. **UiSelector.java** - 移除白名单检查逻辑
3. **AutoJs.java** - 移除白名单配置代码

---

## 二、控件混淆问题分析

### 2.1 问题定位

在 `automator/src/main/java/com/stardust/automator/filter/ClassNameFilters.kt` 中发现：

```kotlin
fun equals(text: String): Filter {
    var className = text
    if (!className.contains(".")) {
        className = "android.widget.$className"  // 自动添加前缀
    }
    return StringEqualsFilter(className, CLASS_NAME_GETTER)
}
```

**问题：**
- 用户使用简写类名时自动添加 `android.widget.` 前缀
- 可能导致无法识别自定义控件
- 不同应用的控件可能有相同简短名称但不同完整类名

### 2.2 优化方案

保留现有的自动补全功能（兼容性考虑），但增加以下改进：
- 无需修改，当前实现已考虑了不包含点号时才添加前缀
- 如果用户传入完整类名（包含点号），则直接使用

---

## 三、详细实施步骤

### 步骤 1：修改 AccessibilityConfig.java

**文件路径：** `autojs/src/main/java/com/stardust/autojs/runtime/accessibility/AccessibilityConfig.java`

**修改内容：**
- 移除 `mWhiteList` 字段
- 移除 `whiteListContains()` 方法
- 移除 `addWhiteList()` 方法
- 移除构造函数中的白名单初始化逻辑
- 保留 `mSealed` 字段和 `seal()` 方法（其他功能可能依赖）

### 步骤 2：修改 UiSelector.java

**文件路径：** `autojs/src/main/java/com/stardust/autojs/core/accessibility/UiSelector.java`

**修改内容：**
- 移除 `findImpl()` 方法中的白名单检查逻辑（第111-114行）

**修改前：**
```java
if (root.getPackageName() != null && mAccessibilityBridge.getConfig().whiteListContains(root.getPackageName().toString())) {
    Log.d(TAG, "package in white list, return null");
    return UiObjectCollection.Companion.getEMPTY();
}
```

**修改后：** 完全移除此段代码

### 步骤 3：修改 AutoJs.java

**文件路径：** `app/src/main/java/org/autojs/autojs/autojs/AutoJs.java`

**修改内容：**
- 移除 `createAccessibilityConfig()` 方法中的白名单添加逻辑
- 简化该方法，直接返回父类创建的配置

**修改前：**
```java
@Override
protected AccessibilityConfig createAccessibilityConfig() {
    AccessibilityConfig config = super.createAccessibilityConfig();
    if (BuildConfig.CHANNEL.equals("coolapk")) {
        config.addWhiteList("com.coolapk.market");
    }
    return config;
}
```

**修改后：**
```java
@Override
protected AccessibilityConfig createAccessibilityConfig() {
    return super.createAccessibilityConfig();
}
```

### 步骤 4：验证修改

1. 检查是否有其他代码引用被移除的方法
2. 确保编译通过
3. 验证功能正常

---

## 四、风险评估与应对

### 4.1 潜在风险

| 风险 | 影响程度 | 应对措施 |
|-----|---------|---------|
| 其他模块可能依赖白名单方法 | 低 | 通过全局搜索确认无其他引用 |
| 编译错误 | 低 | 逐步修改，确保每步编译通过 |
| 功能回归 | 低 | 白名单功能本身是限制性功能，移除不影响核心功能 |

### 4.2 回滚方案

保留原始代码备份，如有问题可快速回滚。

---

## 五、测试计划

### 5.1 编译测试
- 执行 `./gradlew build` 确保项目编译通过

### 5.2 功能测试
- 测试控件选择器功能是否正常
- 测试在不同应用中的节点访问
- 验证无崩溃和异常

---

## 六、预期结果

1. **白名单功能完全移除**：用户可以访问所有应用的节点信息
2. **代码简化**：移除不必要的限制逻辑
3. **功能增强**：提升用户体验，无访问限制

---

## 七、修改文件清单

| 序号 | 文件路径 | 修改类型 |
|-----|---------|---------|
| 1 | `autojs/src/main/java/com/stardust/autojs/runtime/accessibility/AccessibilityConfig.java` | 修改 |
| 2 | `autojs/src/main/java/com/stardust/autojs/core/accessibility/UiSelector.java` | 修改 |
| 3 | `app/src/main/java/org/autojs/autojs/autojs/AutoJs.java` | 修改 |
