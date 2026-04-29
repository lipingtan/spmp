<template>
  <div class="init-container">
    <div class="init-card">
      <h2 class="init-title">SPMP 系统初始化</h2>
      <p class="init-desc">首次部署，请填写基础设施连接信息完成系统初始化</p>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 32px">
        <el-step title="填写配置" />
        <el-step title="测试连接" />
        <el-step title="执行初始化" />
      </el-steps>

      <!-- 步骤 1：填写配置 -->
      <el-form
        v-if="currentStep === 0"
        ref="configFormRef"
        :model="configForm"
        :rules="formRules"
        label-width="120px"
      >
        <h3>数据库配置</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="地址" prop="database.host">
              <el-input v-model="configForm.database.host" placeholder="127.0.0.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="database.port">
              <el-input-number v-model="configForm.database.port" :min="1" :max="65535" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="数据库名" prop="database.databaseName">
              <el-input v-model="configForm.database.databaseName" placeholder="spmp" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="database.username">
              <el-input v-model="configForm.database.username" placeholder="root" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密码" prop="database.password">
          <el-input v-model="configForm.database.password" type="password" show-password placeholder="请输入数据库密码" />
        </el-form-item>

        <h3 style="margin-top: 24px">Redis 配置</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="地址" prop="redis.host">
              <el-input v-model="configForm.redis.host" placeholder="127.0.0.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="redis.port">
              <el-input-number v-model="configForm.redis.port" :min="1" :max="65535" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="redis.password">
              <el-input v-model="configForm.redis.password" type="password" show-password placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据库编号" prop="redis.database">
              <el-input-number v-model="configForm.redis.database" :min="0" :max="15" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleTestConnection">下一步：测试连接</el-button>
        </el-form-item>
      </el-form>

      <!-- 步骤 2：测试连接 -->
      <div v-if="currentStep === 1" class="test-result">
        <el-result
          v-if="!testing"
          :icon="testPassed ? 'success' : 'error'"
          :title="testPassed ? '连接测试通过' : '连接测试未通过'"
        >
          <template #sub-title>
            <div>
              <p>数据库：{{ testResult?.databaseConnected ? '✅ ' + testResult.databaseMessage : '❌ ' + testResult?.databaseMessage }}</p>
              <p>Redis：{{ testResult?.redisConnected ? '✅ ' + testResult.redisMessage : '❌ ' + testResult?.redisMessage }}</p>
            </div>
          </template>
          <template #extra>
            <el-button @click="currentStep = 0">返回修改</el-button>
            <el-button v-if="testPassed" type="primary" @click="handleExecuteInit">下一步：执行初始化</el-button>
            <el-button v-else type="warning" @click="handleTestConnection">重新测试</el-button>
          </template>
        </el-result>
        <div v-else style="text-align: center; padding: 40px">
          <el-icon class="is-loading" :size="48"><Loading /></el-icon>
          <p>正在测试连接...</p>
        </div>
      </div>

      <!-- 步骤 3：执行初始化 -->
      <div v-if="currentStep === 2" class="init-result">
        <div v-if="initializing" style="text-align: center; padding: 40px">
          <el-icon class="is-loading" :size="48"><Loading /></el-icon>
          <p>正在执行初始化，请稍候...</p>
        </div>
        <el-result v-else-if="initResult?.success" icon="success" title="初始化完成">
          <template #sub-title>
            <div>
              <p>数据库：{{ initResult.summary.databaseHost }} / {{ initResult.summary.databaseName }}</p>
              <p>Redis：{{ initResult.summary.redisHost }}</p>
              <p>已执行脚本：{{ initResult.summary.scriptsExecuted }} 个</p>
              <el-alert :type="initResult.modeSwitched ? 'success' : 'warning'" :closable="false" style="margin-top: 16px">
                {{ initResult.modeSwitched ? '运行态已切换成功，正在进入登录页' : '初始化完成，但运行态尚未切换成功' }}
              </el-alert>
            </div>
          </template>
        </el-result>
        <el-result v-else icon="error" title="初始化失败">
          <template #sub-title>
            <p>{{ initError }}</p>
          </template>
          <template #extra>
            <el-button @click="currentStep = 0">返回修改配置</el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { testConnection, executeInit } from '@/api/init'
import type { ConnectionTestResult, InitResult } from '@/api/init'

const configFormRef = ref<FormInstance>()
const currentStep = ref(0)
const testing = ref(false)
const initializing = ref(false)
const testResult = ref<ConnectionTestResult | null>(null)
const initResult = ref<InitResult | null>(null)
const initError = ref('')

const configForm = reactive({
  database: {
    host: '127.0.0.1',
    port: 3306,
    databaseName: 'spmp',
    username: 'root',
    password: ''
  },
  redis: {
    host: '127.0.0.1',
    port: 6379,
    password: '',
    database: 0
  }
})

const formRules: FormRules = {
  'database.host': [{ required: true, message: '请输入数据库地址', trigger: 'blur' }],
  'database.port': [{ required: true, message: '请输入数据库端口', trigger: 'blur' }],
  'database.databaseName': [{ required: true, message: '请输入数据库名称', trigger: 'blur' }],
  'database.username': [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  'redis.host': [{ required: true, message: '请输入 Redis 地址', trigger: 'blur' }],
  'redis.port': [{ required: true, message: '请输入 Redis 端口', trigger: 'blur' }]
}

const testPassed = computed(() => {
  return testResult.value?.databaseConnected && testResult.value?.redisConnected
})

async function handleTestConnection() {
  if (configFormRef.value) {
    const valid = await configFormRef.value.validate().catch(() => false)
    if (!valid) return
  }

  testing.value = true
  currentStep.value = 1
  try {
    testResult.value = await testConnection(configForm)
  } catch (e: any) {
    testResult.value = {
      databaseConnected: false,
      databaseMessage: '请求失败',
      redisConnected: false,
      redisMessage: e.message || '网络异常'
    }
  } finally {
    testing.value = false
  }
}

async function handleExecuteInit() {
  initializing.value = true
  currentStep.value = 2
  initError.value = ''
  try {
    initResult.value = await executeInit(configForm)
    if (initResult.value.modeSwitched) {
      ElMessage.success('初始化完成，正在进入登录页')
      window.setTimeout(() => {
        window.location.href = '/login'
      }, 1200)
    } else {
      ElMessage.warning('初始化完成，但运行态切换未完成')
    }
  } catch (e: any) {
    initResult.value = null
    initError.value = e.message || '初始化失败'
  } finally {
    initializing.value = false
  }
}
</script>

<style scoped>
.init-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.init-card {
  width: 720px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.init-title {
  text-align: center;
  margin-bottom: 8px;
  color: #303133;
}
.init-desc {
  text-align: center;
  color: #909399;
  margin-bottom: 32px;
}
h3 {
  color: #606266;
  margin-bottom: 16px;
  font-size: 15px;
}
</style>