<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerForm = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
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
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
})

async function handleRegister() {
  try {
    await registerForm.value.validate()
    loading.value = true
    await register({
      username: form.username,
      password: form.password,
      email: form.email,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/user/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>ArmPro 用户平台</h2>
          <p>创建新账号</p>
        </div>
      </template>
      <el-form ref="registerForm" :model="form" :rules="rules" label-width="80px" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%;" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div style="text-align: center; width: 100%; font-size: 14px;">
            已有账号？
            <router-link to="/user/login">去登录</router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 480px;
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}
.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
</style>
