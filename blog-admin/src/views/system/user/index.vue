<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="用户名" prop="nickname">
          <el-input v-model="queryParams.nickname" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="登录方式" prop="loginType">
          <el-select v-model="queryParams.loginType" placeholder="请选择登录方式" clearable>
            <el-option label="邮箱登录" :value="1" />
            <el-option label="微信扫码登录" :value="2" />
            <el-option label="gitee登录" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button v-permission="['sys:user:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
            <el-button v-permission="['sys:user:delete']" type="danger" icon="Delete"
              :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="userList" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="头像" prop="avatar" align="center">
          <template #default="{ row }">
            <el-image :src="row.avatar" style="width: 40px; height: 40px; border-radius: 5px;" />
          </template>
        </el-table-column>
        <el-table-column label="昵称" align="center" prop="nickname" show-overflow-tooltip width="120" />
        <el-table-column label="身份" align="center" prop="roleIds" width="100">
          <template #default="{ row }">
            <el-tag type="primary">{{ getRoleName(row.roleIds) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="性别" align="center" prop="sex" show-overflow-tooltip width="120">
          <template #default="{ row }">
            <el-tag :type="row.sex === 1 ? 'success' : row.sex === 2 ? 'danger' : 'info'">
              {{ row.sex === 1 ? '男' : row.sex === 2 ? '女' : '保密' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="登录方式" align="center" prop="loginType" width="118">
          <template #default="{ row }">
            <el-tag :type="getLoginTypeStyle(row.loginType)">
              {{ getLoginTypeName(row.loginType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="登录IP" align="center" prop="ip" show-overflow-tooltip />
        <el-table-column label="登录地址" align="center" prop="ipLocation" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" align="center" prop="lastLoginTime" width="160" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-button v-permission="['sys:user:update']" type="primary" link icon="Edit"
              :disabled="scope.row.username === 'super_admin' && scope.row.roleIds === 1"
              @click="handleUpdate(scope.row)">修改</el-button>
            <el-button v-permission="['sys:user:reset']" type="info" link icon="Key"
              @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button v-permission="['sys:user:delete']" type="danger" link icon="Delete"
              :disabled="scope.row.username === 'super_admin' && scope.row.roleIds === 1"
              @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]" :total="total" :background="true"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 添加或修改用户对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" append-to-body destroy-on-close
      class="custom-dialog">
      <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="80px" class="custom-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="dialog.type === 'edit'" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" placeholder="请输入昵称" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="userForm.mobile" placeholder="请输入手机号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="userForm.sex">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
                <el-radio :value="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="dialog.type === 'add'">
              <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password clearable />
            </el-form-item>
            <el-form-item label="登录方式" v-if="dialog.type === 'edit'">
              <el-tag :type="getLoginTypeStyle(userForm.loginType)">
                {{ getLoginTypeName(userForm.loginType) }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <!--  -->
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="userForm.roleIds" placeholder="请选择角色" style="width: 100%"
            :disabled="userForm.username === 'super_admin' || isCurrentUserSuperAdmin()" clearable>
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加重置密码弹窗 -->
    <el-dialog title="重置密码" v-model="resetPwdDialog.visible" width="500px" append-to-body destroy-on-close
      class="custom-dialog">
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password
            clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPwdDialog.visible = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitResetPwd">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getUserListApi,
  createUserApi,
  updateUserApi,
  deleteUserApi,
  resetPasswordApi
} from '@/api/system/user'
import { getAllRoleList } from '@/api/system/role'
import { getDictDataByDictTypesApi } from '@/api/system/dict'
import ButtonGroup from '@/components/ButtonGroup/index.vue'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  nickname: '',
  status: '',
  loginType: ''
})

const loading = ref(false)
const total = ref(0)
const userList = ref([])
const queryFormRef = ref<FormInstance>()
const userFormRef = ref<FormInstance>()
const submitLoading = ref(false)

// 选中项数组
const selectedIds = ref<string[]>([])

// 弹窗控制
const dialog = reactive({
  title: '',
  visible: false,
  type: 'add'
})

// 角色选项
const roleOptions = ref<any[]>([])

// 表单数据
const userForm = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: null,
  mobile: '',
  email: '',
  sex: 0,
  status: 1,
  ip: undefined,
  ipLocation: undefined,
  lastLoginTime: undefined,
  createTime: undefined,
  roleIds: undefined as number | undefined,
  loginType: undefined as number | undefined
})

// 表单校验规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleIds: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
})

// 重置密码弹窗控制
const resetPwdDialog = reactive({
  id: undefined,
  visible: false,
  userId: undefined
})

// 重置密码表单
const resetPwdForm = reactive({
  password: '',
  confirmPassword: ''
})

// 重置密码表单校验规则
const resetPwdRules = reactive<FormRules>({
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPwdForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const resetPwdFormRef = ref<FormInstance>()

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getUserListApi(queryParams)
    userList.value = data.records
    total.value = data.total
    console.log('用户数据:', data.records) // 调试用，查看用户数据结构
  } catch (error) {
  }
  loading.value = false
}

// 表格选择项变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) return

  ElMessageBox.confirm('是否确认批量删除选中的用户?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUserApi(selectedIds.value)
      ElMessage.success('批量删除成功')
      getList()
      selectedIds.value = []
    } catch (error) {
    }
  })
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// 新增用户
const handleAdd = () => {
  dialog.type = 'add'
  dialog.title = '新增用户'
  dialog.visible = true
  userForm.id = undefined
  userForm.username = ''
  userForm.nickname = ''
  userForm.password = null
  userForm.mobile = ''
  userForm.email = ''
  userForm.sex = 0
  userForm.status = 1
  userForm.ip = undefined
  userForm.ipLocation = undefined
  userForm.lastLoginTime = undefined
  userForm.createTime = undefined
  userForm.roleIds = undefined
  userForm.loginType = undefined
}

// 修改用户
const handleUpdate = (row: any) => {
  dialog.type = 'edit'
  dialog.title = '修改用户'
  dialog.visible = true
  Object.assign(userForm, row)
  // 处理角色数据，如果是数组取第一个，如果是单个值直接使用
  if (Array.isArray(row.roleIds)) {
    userForm.roleIds = row.roleIds.length > 0 ? row.roleIds[0] : undefined
  } else {
    userForm.roleIds = row.roleIds
  }
  userForm.password = null
}

// 提交表单
const submitForm = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = { user: userForm, roleIds: userForm.roleIds ? [userForm.roleIds] : [] }
        if (dialog.type === 'add') {
          await createUserApi(data)
          ElMessage.success('新增成功')
        } else {
          await updateUserApi(data)
          ElMessage.success('修改成功')
        }
        dialog.visible = false
        getList()
      } catch (error) {
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除用户
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除用户"${row.username}"?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUserApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  })
}

// 修改重置密码方法
const handleResetPwd = (row: any) => {
  resetPwdDialog.id = row.id
  resetPwdDialog.visible = true
  resetPwdForm.password = ''
  resetPwdForm.confirmPassword = ''
}

// 提交重置密码
const submitResetPwd = async () => {
  if (!resetPwdFormRef.value) return

  await resetPwdFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await resetPasswordApi({
          id: resetPwdDialog.id,
          password: resetPwdForm.password
        })
        ElMessage.success('重置密码成功')
        resetPwdDialog.visible = false
      } catch (error) {
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 取消按钮
const cancel = () => {
  dialog.visible = false
  userFormRef.value?.resetFields()
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

// 页码改变
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 获取角色列表
const getRoleOptions = async () => {
  try {
    const { data } = await getAllRoleList()
    roleOptions.value = data
    console.log('角色数据:', data) // 调试用，查看角色数据结构
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}
// 获取字典数据（已不再需要，因为登录类型改为静态映射）
// const getDicts = async () => {
//   try {
//     const { data } = await getDictDataByDictTypesApi(['login_type'])
//     loginTypes.value = data.login_type.list
//   } catch (error) {
//   }
// }

// 根据角色ID获取角色名称
const getRoleName = (roleId: number | string) => {
  console.log('查找角色ID:', roleId, '角色选项:', roleOptions.value) // 调试信息

  if (!roleId || roleOptions.value.length === 0) {
    return '未知角色'
  }

  // 处理数组形式的roleId
  let targetRoleId = roleId
  if (Array.isArray(roleId)) {
    targetRoleId = roleId.length > 0 ? roleId[0] : null
  }

  if (!targetRoleId) {
    return '未知角色'
  }

  // 确保比较时数据类型一致
  const role = roleOptions.value.find(role => role.id == targetRoleId)
  console.log('找到的角色:', role) // 调试信息
  return role ? role.name : '未知角色'
}

// 根据登录类型ID获取登录类型名称
const getLoginTypeName = (loginType: number | undefined) => {
  if (!loginType) return '未知登录方式'
  const loginTypeMap: Record<number, string> = {
    1: '邮箱登录',
    2: '微信扫码登录',
    3: 'gitee登录'
  }
  return loginTypeMap[loginType] || '未知登录方式'
}

// 根据登录类型ID获取对应的标签样式
const getLoginTypeStyle = (loginType: number | undefined) => {
  if (!loginType) return 'info'
  const styleMap: Record<number, 'success' | 'warning' | 'info' | 'primary' | 'danger'> = {
    1: 'success',    // 邮箱登录 - 绿色
    2: 'warning',    // 微信扫码登录 - 橙色
    3: 'info'        // gitee登录 - 蓝色
  }
  return styleMap[loginType] || 'info'
}

// 判断当前编辑的用户是否为超级管理员
const isCurrentUserSuperAdmin = () => {
  if (dialog.type === 'edit') {
    // 通过角色ID判断，假设超级管理员的角色ID为1
    return userForm.roleIds === 1 || userForm.username === 'super_admin'
  }
  return false
}

// 初始化
onMounted(async () => {
  // 先加载角色数据，再加载用户列表，确保角色名称能正确显示
  await getRoleOptions()
  getList()
})
</script>
