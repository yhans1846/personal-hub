# Profile 个人资料模块 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 Settings 页面新增"个人资料"Tab，支持头像上传、基本信息编辑、联系方式维护、个人简介管理

**架构：** 后端新增 UserController 暴露 profile CRUD + 头像上传 API，扩展 User 实体/Service；前端新增 ProfileSettings 组件作为 Settings 的第一个 Tab

**技术栈：** Spring Boot 3 + MyBatis-Plus + Vue 3 + Element Plus

---

### 任务 1：后端 — User 实体加字段

**文件：** 修改 `personal-hub-server/ph-system/src/main/java/com/personalhub/system/entity/User.java`

- [ ] **步骤 1：在 email 字段后添加新字段**

```java
    /** 性别 0-保密 1-男 2-女 */
    private Integer gender;

    /** 出生日期 */
    private java.time.LocalDate birthday;

    /** 手机号 */
    private String phone;

    /** 国家 */
    private String country;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 个人网站 */
    private String website;

    /** GitHub 地址 */
    private String github;

    /** 个人简介 */
    private String bio;
```

插入位置：`private String email;` 之后，`private LocalDateTime createdAt;` 之前。

- [ ] **步骤 2：验证编译**

运行：`cd personal-hub-server && mvn compile -pl ph-system -am -q`

---

### 任务 2：后端 — UserProfileVO 加字段

**文件：** 修改 `personal-hub-server/ph-system/src/main/java/com/personalhub/system/vo/UserProfileVO.java`

- [ ] **步骤 1：VO 新增字段 + from() 映射**

```java
    @Schema(description = "性别 0-保密 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "出生日期", example = "1990-01-01")
    private java.time.LocalDate birthday;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "国家", example = "中国")
    private String country;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "个人网站", example = "https://example.com")
    private String website;

    @Schema(description = "GitHub 地址", example = "https://github.com/username")
    private String github;

    @Schema(description = "个人简介", example = "热爱编程")
    private String bio;
```

在 `from()` 方法中补充 set 调用：

```java
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setPhone(user.getPhone());
        vo.setCountry(user.getCountry());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setWebsite(user.getWebsite());
        vo.setGithub(user.getGithub());
        vo.setBio(user.getBio());
```

---

### 任务 3：后端 — UserProfileUpdateDTO 扩展

**文件：** 修改 `personal-hub-server/ph-system/src/main/java/com/personalhub/system/dto/UserProfileUpdateDTO.java`

- [ ] **步骤 1：DTO 新增所有可编辑字段 + 校验注解**

```java
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度 2~20 个字符")
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "性别 0-保密 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "出生日期", example = "1990-01-01")
    private java.time.LocalDate birthday;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "国家", example = "中国")
    private String country;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "个人网站", example = "https://example.com")
    private String website;

    @Schema(description = "GitHub 地址", example = "https://github.com/username")
    private String github;

    @Size(max = 200, message = "个人简介不超过 200 字")
    @Schema(description = "个人简介", example = "热爱编程")
    private String bio;
```

注意需要补充 import：`import jakarta.validation.constraints.NotBlank;` `import jakarta.validation.constraints.Pattern;`

---

### 任务 4：后端 — UserService / UserServiceImpl 更新

**文件：** 修改：
- `personal-hub-server/ph-system/src/main/java/com/personalhub/system/service/UserService.java`
- `personal-hub-server/ph-system/src/main/java/com/personalhub/system/service/impl/UserServiceImpl.java`

- [ ] **步骤 1：UserService 接口替换 updateProfile 签名**

```java
    /**
     * 修改个人信息
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 更新头像
     */
    void updateAvatar(Long userId, String avatarUrl);
```

需要 import UserProfileUpdateDTO。

- [ ] **步骤 2：UserServiceImpl 重写 updateProfile + 实现 updateAvatar**

```java
    @Override
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("更新资料用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        user.setPhone(dto.getPhone());
        user.setCountry(dto.getCountry());
        user.setProvince(dto.getProvince());
        user.setCity(dto.getCity());
        user.setWebsite(dto.getWebsite());
        user.setGithub(dto.getGithub());
        user.setBio(dto.getBio());
        userMapper.updateById(user);
        log.info("用户更新资料: userId={}", userId);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("更新头像用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        log.info("用户更新头像: userId={}", userId);
    }
```

需要 import：`import com.personalhub.system.dto.UserProfileUpdateDTO;`

---

### 任务 5：后端 — UserController 新建

**文件：** 创建 `personal-hub-server/ph-system/src/main/java/com/personalhub/system/controller/UserController.java`

- [ ] **步骤 1：创建 UserController**

```java
package com.personalhub.system.controller;

import com.personalhub.common.util.SecurityUtils;
import com.personalhub.system.dto.UserProfileUpdateDTO;
import com.personalhub.system.service.FileResourceService;
import com.personalhub.system.service.UserService;
import com.personalhub.system.vo.UserProfileVO;
import com.personalhub.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 用户资料管理
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户资料")
public class UserController {

    private final UserService userService;
    private final StorageService storageService;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户资料")
    public UserProfileVO getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public UserProfileVO updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, dto);
        return userService.getProfile(userId);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像")
    public java.util.Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) ext = originalName.substring(dot + 1).toLowerCase();
        }
        if (!java.util.Set.of("jpg", "jpeg", "png", "gif", "webp").contains(ext)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WebP 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("头像文件不能超过 5MB");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativePath = "avatars/" + storedName;

        storageService.store(file, relativePath);

        // 构建可访问 URL
        String avatarUrl = "/api/files/avatar/" + storedName;

        userService.updateAvatar(userId, avatarUrl);
        log.info("用户上传头像: userId={}, path={}", userId, relativePath);

        return java.util.Map.of("url", avatarUrl);
    }
}
```

需要确认 SecurityUtils 有 getCurrentUserId 方法。

- [ ] **步骤 2：检查 SecurityUtils**

找 `ph-common/src/main/java/com/personalhub/common/util/SecurityUtils.java`，确认有 `getCurrentUserId()` 方法。如果没有则实现。

- [ ] **步骤 3：编译验证**

运行：`cd personal-hub-server && mvn compile -pl ph-system -am -q`

---

### 任务 6：后端 — 头像文件访问接口

为了让 `/api/files/avatar/{filename}` 可访问，需要添加一个公开的文件访问端点。

**文件：** 修改 `personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/controller/FileController.java`

- [ ] **步骤 1：添加头像访问端点**

```java
    @GetMapping("/avatar/{filename}")
    @Operation(summary = "获取头像文件")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(@PathVariable String filename) {
        try {
            String path = "avatars/" + filename;
            org.springframework.core.io.Resource resource = storageService.load(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(org.springframework.http.CacheControl.maxAge(7, java.util.concurrent.TimeUnit.DAYS))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filename) {
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot > 0) ext = filename.substring(dot + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
```

需要 import：
```java
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
```

---

### 任务 7：前端 — 用户类型定义

**文件：** 创建 `personal-hub-web/src/types/user.ts`

- [ ] **步骤 1：创建用户类型文件**

```typescript
/** 性别 */
export type Gender = 0 | 1 | 2
export const GenderLabel: Record<Gender, string> = {
  0: '保密',
  1: '男',
  2: '女',
}

/** 个人资料 */
export interface ProfileVO {
  id: number
  username: string
  nickname: string
  avatar: string | null
  email: string
  gender: Gender
  birthday: string | null
  phone: string | null
  country: string | null
  province: string | null
  city: string | null
  website: string | null
  github: string | null
  bio: string | null
  createdAt: string
}

/** 更新个人资料 */
export interface ProfileUpdateDTO {
  nickname: string
  email: string
  gender: Gender
  birthday: string | null
  phone: string | null
  country: string | null
  province: string | null
  city: string | null
  website: string | null
  github: string | null
  bio: string | null
}
```

---

### 任务 8：前端 — Profile API

**文件：** 修改 `personal-hub-web/src/modules/system/api.ts`

- [ ] **步骤 1：添加 profile 相关 API**

```typescript
import type { ProfileVO, ProfileUpdateDTO } from '@/types/user'

/** 获取个人资料 */
export function getProfile() {
  return request.get<Result<ProfileVO>>('/user/profile')
}

/** 更新个人资料 */
export function updateProfile(data: ProfileUpdateDTO) {
  return request.put<Result<ProfileVO>>('/user/profile', data)
}

/** 上传头像 */
export function uploadAvatar(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<Result<{ url: string }>>('/user/avatar', form)
}
```

---

### 任务 9：前端 — 地区级联数据

**文件：** 创建 `personal-hub-web/src/modules/system/settings/components/regionData.ts`

- [ ] **步骤 1：创建地区数据（省级精简版）**

```typescript
/** 地区级联选择器数据：国家 → 省份 → 城市 */
export const regionOptions = [
  {
    value: '中国',
    label: '中国',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
        ],
      },
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '静安区', label: '静安区' },
          { value: '浦东新区', label: '浦东新区' },
        ],
      },
      {
        value: '广东省',
        label: '广东省',
        children: [
          { value: '广州市', label: '广州市', children: [{ value: '天河区', label: '天河区' }, { value: '越秀区', label: '越秀区' }, { value: '海珠区', label: '海珠区' }] },
          { value: '深圳市', label: '深圳市', children: [{ value: '南山区', label: '南山区' }, { value: '福田区', label: '福田区' }, { value: '宝安区', label: '宝安区' }, { value: '龙岗区', label: '龙岗区' }] },
          { value: '珠海市', label: '珠海市' },
        ],
      },
      {
        value: '浙江省',
        label: '浙江省',
        children: [
          { value: '杭州市', label: '杭州市', children: [{ value: '西湖区', label: '西湖区' }, { value: '滨江区', label: '滨江区' }, { value: '余杭区', label: '余杭区' }] },
          { value: '宁波市', label: '宁波市' },
          { value: '温州市', label: '温州市' },
        ],
      },
      {
        value: '江苏省',
        label: '江苏省',
        children: [
          { value: '南京市', label: '南京市', children: [{ value: '鼓楼区', label: '鼓楼区' }, { value: '玄武区', label: '玄武区' }, { value: '江宁区', label: '江宁区' }] },
          { value: '苏州市', label: '苏州市' },
          { value: '无锡市', label: '无锡市' },
        ],
      },
      {
        value: '四川省',
        label: '四川省',
        children: [
          { value: '成都市', label: '成都市', children: [{ value: '武侯区', label: '武侯区' }, { value: '锦江区', label: '锦江区' }, { value: '高新区', label: '高新区' }] },
        ],
      },
      {
        value: '湖北省',
        label: '湖北省',
        children: [
          { value: '武汉市', label: '武汉市', children: [{ value: '武昌区', label: '武昌区' }, { value: '洪山区', label: '洪山区' }, { value: '江汉区', label: '江汉区' }] },
        ],
      },
      {
        value: '湖南省',
        label: '湖南省',
        children: [
          { value: '长沙市', label: '长沙市' },
        ],
      },
    ],
  },
]
```

---

### 任务 10：前端 — ProfileSettings 组件

**文件：** 创建 `personal-hub-web/src/modules/system/settings/components/ProfileSettings.vue`

主要功能：
- 头像上传（hover 遮罩 + 上传按钮）
- 基本信息 Card
- 联系方式 Card（含级联选择器）
- 个人简介 Card（含字数统计）
- 底部重置 + 保存按钮
- 表单校验

- [ ] **步骤 1：创建 ProfileSettings.vue**

```vue
<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, uploadAvatar } from '@/modules/system/api'
import { useAuthStore } from '@/store/authStore'
import { GenderLabel } from '@/types/user'
import type { ProfileVO, ProfileUpdateDTO, Gender } from '@/types/user'
import { regionOptions } from './regionData'
import { User, Mail, Phone, MapPin, Globe, Github, FileText, Camera } from 'lucide-vue-next'

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreview = ref<string | null>(null)

const form = reactive<ProfileUpdateDTO>({
  nickname: '',
  email: '',
  gender: 0 as Gender,
  birthday: null,
  phone: null,
  country: null,
  province: null,
  city: null,
  website: null,
  github: null,
  bio: null,
})

const initialForm = ref<string>('') // 用于重置

const formRef = ref<any>(null)

// 地区级联 v-model（三元素数组）
const regionValue = ref<string[]>([])

// 字数统计
const bioCount = computed(() => (form.bio || '').length)

onMounted(async () => {
  try {
    const res = await getProfile()
    const data = res.data.data
    Object.assign(form, {
      nickname: data.nickname || '',
      email: data.email || '',
      gender: data.gender ?? 0,
      birthday: data.birthday || null,
      phone: data.phone || null,
      country: data.country || null,
      province: data.province || null,
      city: data.city || null,
      website: data.website || null,
      github: data.github || null,
      bio: data.bio || null,
    })
    if (data.country && data.province && data.city) {
      regionValue.value = [data.country, data.province, data.city]
    } else if (data.country && data.province) {
      regionValue.value = [data.country, data.province]
    }
    avatarPreview.value = data.avatar
    initialForm.value = JSON.stringify({ ...form })
  } catch {
    ElMessage.error('加载个人资料失败')
  } finally {
    loading.value = false
  }
})

// 地区变更
function onRegionChange(val: string[]) {
  if (val.length >= 1) form.country = val[0]
  if (val.length >= 2) form.province = val[1]
  if (val.length >= 3) form.city = val[2]
  if (val.length === 0) {
    form.country = null
    form.province = null
    form.city = null
  }
}

// 头像上传
async function handleAvatarUpload(file: File) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG/GIF/WebP 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像文件不能超过 5MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    avatarPreview.value = res.data.data.url
    authStore.user.avatar = res.data.data.url
    authStore.setUser(authStore.user)
    ElMessage.success('头像已更新')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

function onAvatarSelect() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/gif,image/webp'
  input.onchange = (e: any) => {
    const file = e.target.files?.[0]
    if (file) handleAvatarUpload(file)
  }
  input.click()
}

// 重置
function handleReset() {
  const saved = JSON.parse(initialForm.value)
  Object.assign(form, saved)
  if (form.country && form.province) {
    regionValue.value = form.city ? [form.country, form.province, form.city] : [form.country, form.province]
  } else {
    regionValue.value = []
  }
  ElMessage.info('已重置')
}

// 保存
async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请检查表单填写')
    return
  }
  saving.value = true
  try {
    const res = await updateProfile(form)
    // 同步 authStore
    if (authStore.user) {
      authStore.user.nickname = form.nickname
      authStore.setUser(authStore.user)
    }
    initialForm.value = JSON.stringify({ ...form })
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const genderOptions = [
  { value: 0, label: '保密' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

// 校验规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度 2~20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  website: [
    { pattern: /^$|^https?:\/\/.+/, message: '请输入正确的网址（以 https:// 开头）', trigger: 'blur' },
  ],
  github: [
    { pattern: /^$|^https:\/\/github\.com\/.+/, message: '请输入正确的 GitHub 地址', trigger: 'blur' },
  ],
}
</script>

<template>
  <div v-loading="loading" class="profile-settings">
    <!-- 头像 -->
    <div class="profile-card avatar-card">
      <div class="avatar-wrap" @click="onAvatarSelect">
        <img
          v-if="avatarPreview"
          :src="avatarPreview"
          class="avatar-img"
          alt="头像"
        />
        <div v-else class="avatar-placeholder">
          <User :size="40" />
        </div>
        <div class="avatar-overlay">
          <Camera :size="18" />
          <span>{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
        </div>
      </div>
      <p class="avatar-hint">支持 JPG、PNG、GIF、WebP，最大 5MB</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="profile-form"
    >
      <!-- 基本信息 -->
      <div class="profile-card">
        <h3 class="card-title"><User :size="16" /> 基本信息</h3>
        <div class="form-grid">
          <el-form-item label="用户名">
            <el-input :model-value="authStore.user?.username" disabled class="field-input" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="输入昵称" class="field-input" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="field-input" />
          </el-form-item>
        </div>
      </div>

      <!-- 联系方式 -->
      <div class="profile-card">
        <h3 class="card-title"><Mail :size="16" /> 联系方式</h3>
        <div class="form-grid">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="输入邮箱" class="field-input" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="输入手机号（选填）" class="field-input" maxlength="11" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-cascader
              v-model="regionValue"
              :options="regionOptions"
              placeholder="选择地区（选填）"
              class="field-input"
              clearable
              @change="onRegionChange"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 个人简介 -->
      <div class="profile-card">
        <h3 class="card-title"><FileText :size="16" /> 个人简介</h3>
        <div class="form-grid">
          <el-form-item label="GitHub" prop="github">
            <el-input v-model="form.github" placeholder="https://github.com/用户名（选填）" class="field-input">
              <template #prefix><Github :size="14" /></template>
            </el-input>
          </el-form-item>
          <el-form-item label="个人网站" prop="website">
            <el-input v-model="form.website" placeholder="https://（选填）" class="field-input">
              <template #prefix><Globe :size="14" /></template>
            </el-input>
          </el-form-item>
          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="介绍自己…"
              class="field-textarea"
            />
          </el-form-item>
        </div>
      </div>
    </el-form>

    <!-- 按钮 -->
    <div class="form-actions">
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
    </div>
  </div>
</template>

<style scoped>
.profile-settings {
  max-width: 640px;
}
.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 头像 */
.avatar-card { text-align: center; }
.avatar-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 12px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-color);
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: var(--bg-hover);
  color: var(--text-tertiary);
}
.avatar-overlay {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity var(--transition);
}
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.avatar-hint { font-size: 12px; color: var(--text-tertiary); margin: 0; }

/* 表单 */
.profile-form :deep(.el-form-item) { margin-bottom: 18px; }
.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  padding-bottom: 4px;
}
.form-grid {
  display: flex;
  flex-direction: column;
}
.field-input {
  width: 420px;
  max-width: 100%;
}
.field-textarea {
  width: 420px;
  max-width: 100%;
}

/* 按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-3);
  padding-top: 8px;
}
</style>
```

---

### 任务 11：前端 — SettingsView 添加个人资料 Tab

**文件：** 修改 `personal-hub-web/src/modules/system/settings/SettingsView.vue`

- [ ] **步骤 1：import User 图标 + ProfileSettings 组件**

```typescript
import { User } from 'lucide-vue-next'
import ProfileSettings from './components/ProfileSettings.vue'
```

- [ ] **步骤 2：tabs 数组最前面插入 profile tab**

```typescript
const tabs: TabItem[] = [
  { key: 'profile', icon: User, label: '个人资料' },
  { key: 'workspace', icon: LayoutDashboard, label: '工作台' },
  ...
]
```

- [ ] **步骤 3：template 中第一个 v-if 前插入 profile 内容**

```html
<div v-if="activeTab === 'profile'">
  <ProfileSettings />
</div>
```

---

### 任务 12：数据库 Migration

- [ ] **步骤 1：执行 SQL（手动执行或通过脚本）**

```sql
ALTER TABLE sys_user
    ADD COLUMN gender     tinyint      DEFAULT 0  COMMENT '性别 0-保密 1-男 2-女' AFTER email,
    ADD COLUMN birthday   date         DEFAULT NULL COMMENT '出生日期' AFTER gender,
    ADD COLUMN phone      varchar(20)  DEFAULT NULL COMMENT '手机号' AFTER birthday,
    ADD COLUMN country    varchar(50)  DEFAULT NULL COMMENT '国家' AFTER phone,
    ADD COLUMN province   varchar(50)  DEFAULT NULL COMMENT '省份' AFTER country,
    ADD COLUMN city       varchar(50)  DEFAULT NULL COMMENT '城市' AFTER province,
    ADD COLUMN website    varchar(200) DEFAULT NULL COMMENT '个人网站' AFTER city,
    ADD COLUMN github     varchar(200) DEFAULT NULL COMMENT 'GitHub 地址' AFTER website,
    ADD COLUMN bio        varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER github;
```
