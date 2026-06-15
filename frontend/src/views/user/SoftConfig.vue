<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSoftConfig, updateSoftConfig, type SoftConfig } from '@/api/user'

const route = useRoute()
const router = useRouter()
const softId = Number(route.params.id)
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('notice')

const form = reactive<SoftConfig>({
  id: 0,
  softId: softId,
  noticeTitle: '',
  noticeContent: '',
  noticeStyle: 'info',
  trialCount: 0,
  bindMode: 'device',
  versionCode: 1,
  versionName: '1.0.0',
  updateUrl: '',
  updateLog: '',
  forceUpdate: false,
  customModules: '{}',
  adEnabled: false,
  adConfig: '{}',
})

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getSoftConfig(softId)
    Object.assign(form, res)
  } catch (error) {
    ElMessage.error('获取配置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateSoftConfig(softId, { ...form })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push(`/user/soft-detail/${softId}`)
}

onMounted(() => {
  fetchConfig()
})
</script>

<template>
  <div class="soft-config-container" v-loading="loading">
    <!-- 顶部操作栏 -->
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <el-button @click="goBack">
        <el-icon style="margin-right: 4px;"><ArrowLeft /></el-icon>
        返回详情
      </el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">
        <el-icon style="margin-right: 4px;"><Check /></el-icon>
        保存配置
      </el-button>
    </div>

    <el-card shadow="hover">
      <el-tabs v-model="activeTab">
        <!-- 远程公告配置 -->
        <el-tab-pane label="远程公告" name="notice">
          <el-form :model="form" label-width="100px" style="max-width: 600px; margin-top: 16px;">
            <el-form-item label="公告标题">
              <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" />
            </el-form-item>
            <el-form-item label="公告内容">
              <el-input
                v-model="form.noticeContent"
                type="textarea"
                :rows="4"
                placeholder="请输入公告内容"
              />
            </el-form-item>
            <el-form-item label="公告样式">
              <el-select v-model="form.noticeStyle" style="width: 100%;">
                <el-option label="信息 (Info)" value="info" />
                <el-option label="成功 (Success)" value="success" />
                <el-option label="警告 (Warning)" value="warning" />
                <el-option label="错误 (Error)" value="error" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 单码验证配置 -->
        <el-tab-pane label="单码验证" name="trial">
          <el-form :model="form" label-width="120px" style="max-width: 600px; margin-top: 16px;">
            <el-form-item label="试用次数">
              <el-input-number v-model="form.trialCount" :min="0" :max="999" />
            </el-form-item>
            <el-form-item label="绑定模式">
              <el-select v-model="form.bindMode" style="width: 100%;">
                <el-option label="设备绑定" value="device" />
                <el-option label="IP绑定" value="ip" />
                <el-option label="不绑定" value="none" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 更新配置 -->
        <el-tab-pane label="更新配置" name="update">
          <el-form :model="form" label-width="120px" style="max-width: 600px; margin-top: 16px;">
            <el-form-item label="版本号 (Code)">
              <el-input-number v-model="form.versionCode" :min="1" />
            </el-form-item>
            <el-form-item label="版本名称">
              <el-input v-model="form.versionName" placeholder="例如：1.0.0" />
            </el-form-item>
            <el-form-item label="更新地址">
              <el-input v-model="form.updateUrl" placeholder="APK下载地址" />
            </el-form-item>
            <el-form-item label="更新说明">
              <el-input
                v-model="form.updateLog"
                type="textarea"
                :rows="4"
                placeholder="请输入更新说明"
              />
            </el-form-item>
            <el-form-item label="强制更新">
              <el-switch v-model="form.forceUpdate" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 自定义模块配置 -->
        <el-tab-pane label="自定义模块" name="custom">
          <el-form :model="form" label-width="120px" style="max-width: 600px; margin-top: 16px;">
            <el-form-item label="自定义配置">
              <el-input
                v-model="form.customModules"
                type="textarea"
                :rows="8"
                placeholder='请输入JSON格式配置，例如：{"module1": true, "module2": false}'
              />
            </el-form-item>
            <el-alert
              title="请使用标准JSON格式填写自定义模块配置"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 16px;"
            />
          </el-form>
        </el-tab-pane>

        <!-- 广告配置 -->
        <el-tab-pane label="广告配置" name="ad">
          <el-form :model="form" label-width="120px" style="max-width: 600px; margin-top: 16px;">
            <el-form-item label="启用广告">
              <el-switch v-model="form.adEnabled" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-form-item label="广告配置" v-if="form.adEnabled">
              <el-input
                v-model="form.adConfig"
                type="textarea"
                :rows="6"
                placeholder='请输入JSON格式广告配置'
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.soft-config-container {
  max-width: 1000px;
}
</style>
