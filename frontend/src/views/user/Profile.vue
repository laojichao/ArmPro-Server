<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, changePassword, rechargeCard } from '@/api/user'

const loading = ref(false)
const changingPassword = ref(false)
const recharging = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  email: '',
  vipExpireTime: '',
})

const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!/[A-Z]/.test(value)) {
          callback(new Error('密码需包含至少一个大写字母'))
        } else if (!/[a-z]/.test(value)) {
          callback(new Error('密码需包含至少一个小写字母'))
        } else if (!/[0-9]/.test(value)) {
          callback(new Error('密码需包含至少一个数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
})

const rechargeFormRef = ref()
const rechargeForm = reactive({
  cardKey: '',
})

const rechargeRules = reactive({
  cardKey: [
    { required: true, message: '请输入卡密', trigger: 'blur' },
  ],
})

async function fetchUserInfo() {
  loading.value = true
  try {
    const res = await getUserInfo()
    userInfo.value = res
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleChangePassword() {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

async function handleRecharge() {
  try {
    await rechargeFormRef.value.validate()
    recharging.value = true
    const res = await rechargeCard({ cardKey: rechargeForm.cardKey })
    ElMessage.success('充值成功')
    rechargeForm.cardKey = ''
    if (res.expireTime) {
      userInfo.value.vipExpireTime = res.expireTime
    }
  } catch (error) {
    console.error('充值失败:', error)
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-container" v-loading="loading">
    <!-- 用户信息卡片 -->
    <el-card shadow="hover" style="margin-bottom: 16px;">
      <template #header>
        <span style="font-weight: bold;">个人信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="VIP到期时间">
          <el-tag v-if="userInfo.vipExpireTime" type="success">{{ userInfo.vipExpireTime }}</el-tag>
          <el-tag v-else type="info">未开通</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="16">
      <!-- 修改密码 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>
            <span style="font-weight: bold;">修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 卡密充值 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>
            <span style="font-weight: bold;">卡密充值</span>
          </template>
          <el-form
            ref="rechargeFormRef"
            :model="rechargeForm"
            :rules="rechargeRules"
            label-width="80px"
          >
            <el-form-item label="卡密" prop="cardKey">
              <el-input
                v-model="rechargeForm.cardKey"
                placeholder="请输入充值卡密"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="recharging" @click="handleRecharge">
                <el-icon style="margin-right: 4px;"><Ticket /></el-icon>
                立即充值
              </el-button>
            </el-form-item>
          </el-form>
          <el-alert
            title="输入有效的卡密即可延长VIP有效期"
            type="info"
            :closable="false"
            show-icon
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
}
</style>
