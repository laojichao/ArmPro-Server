<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfig, saveSystemConfig, type SystemConfig } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('basic')

const configForm = reactive<SystemConfig>({
  siteName: '',
  siteUrl: '',
  adminEmail: '',
  uploadType: 'local',
  qiniuAccessKey: '',
  qiniuSecretKey: '',
  qiniuBucket: '',
  qiniuDomain: '',
  localUploadPath: '',
  vipLevels: [],
})

onMounted(() => {
  loadConfig()
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await getSystemConfig()
    Object.assign(configForm, res)
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await saveSystemConfig(configForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    saving.value = false
  }
}

function addVipLevel() {
  configForm.vipLevels.push({
    id: Date.now(),
    name: '',
    days: 30,
    price: 0,
    description: '',
  })
}

function removeVipLevel(index: number) {
  configForm.vipLevels.splice(index, 1)
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">系统配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">
            <el-icon><Check /></el-icon>保存配置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="configForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="站点名称">
              <el-input v-model="configForm.siteName" placeholder="请输入站点名称" />
            </el-form-item>
            <el-form-item label="站点URL">
              <el-input v-model="configForm.siteUrl" placeholder="请输入站点URL" />
            </el-form-item>
            <el-form-item label="管理员邮箱">
              <el-input v-model="configForm.adminEmail" placeholder="请输入管理员邮箱" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="会员等级" name="vip">
          <el-button type="primary" size="small" style="margin-bottom: 16px;" @click="addVipLevel">
            <el-icon><Plus /></el-icon>添加等级
          </el-button>
          <el-table :data="configForm.vipLevels" border style="width: 100%;">
            <el-table-column label="等级名称" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.name" placeholder="等级名称" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="天数" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.days" :min="1" size="small" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="价格" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.price" :min="0" :precision="2" size="small" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="说明" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.description" placeholder="等级说明" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link size="small" @click="removeVipLevel($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="上传配置" name="upload">
          <el-form :model="configForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="上传方式">
              <el-radio-group v-model="configForm.uploadType">
                <el-radio value="local">本地存储</el-radio>
                <el-radio value="qiniu">七牛云</el-radio>
              </el-radio-group>
            </el-form-item>

            <template v-if="configForm.uploadType === 'local'">
              <el-form-item label="本地路径">
                <el-input v-model="configForm.localUploadPath" placeholder="请输入本地上传路径" />
              </el-form-item>
            </template>

            <template v-if="configForm.uploadType === 'qiniu'">
              <el-form-item label="AccessKey">
                <el-input v-model="configForm.qiniuAccessKey" placeholder="请输入七牛云AccessKey" show-password />
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input v-model="configForm.qiniuSecretKey" placeholder="请输入七牛云SecretKey" show-password />
              </el-form-item>
              <el-form-item label="Bucket">
                <el-input v-model="configForm.qiniuBucket" placeholder="请输入七牛云Bucket名称" />
              </el-form-item>
              <el-form-item label="域名">
                <el-input v-model="configForm.qiniuDomain" placeholder="请输入七牛云域名" />
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
