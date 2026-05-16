<template>
    <div class="attachment-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">附件管理</h1>
                <p class="page-subtitle">统一管理业务附件，支持按业务类型和业务对象筛选、上传、下载与删除。</p>
            </div>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-select v-model="query.bizType" clearable placeholder="业务类型" style="width: 180px;">
                        <el-option v-for="option in bizTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-input v-model="query.bizId" clearable placeholder="业务ID" style="width: 180px;" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadAttachments">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('file:attachment:upload')" type="success" @click="openUploadDialog">上传附件</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <div class="table-hint">
                <span>当前筛选：{{ query.bizType ? formatBizType(query.bizType) : '未选择业务类型' }}</span>
                <span>{{ query.bizId || '未填写业务ID' }}</span>
            </div>

            <el-table :data="attachmentList" stripe>
                <el-table-column prop="fileName" label="文件名" min-width="260" show-overflow-tooltip />
                <el-table-column label="业务类型" min-width="120">
                    <template #default="{ row }">
                        {{ formatBizType(row.bizType) }}
                    </template>
                </el-table-column>
                <el-table-column prop="bizId" label="业务ID" min-width="120" />
                <el-table-column prop="contentType" label="MIME类型" min-width="180" />
                <el-table-column label="文件大小" min-width="120">
                    <template #default="{ row }">
                        {{ formatFileSize(row.fileSize) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleDownload(row.id, row.fileName)">下载</el-button>
                        <el-button
                            v-if="hasPermission('file:attachment:delete')"
                            link
                            type="danger"
                            @click="handleDelete(row.id)"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </section>

        <el-dialog v-model="dialogVisible" title="上传附件" width="560px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="业务类型" prop="bizType">
                    <el-select v-model="form.bizType" placeholder="请选择业务类型" style="width: 100%;">
                        <el-option v-for="option in bizTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                </el-form-item>
                <el-form-item label="业务ID" prop="bizId">
                    <el-input v-model="form.bizId" placeholder="请输入业务ID" />
                </el-form-item>
                <el-form-item label="附件文件" prop="file">
                    <el-upload
                        ref="uploadRef"
                        v-model:file-list="uploadFileList"
                        :auto-upload="false"
                        :limit="1"
                        :on-change="handleFileChange"
                        :on-remove="handleFileRemove"
                    >
                        <template #trigger>
                            <el-button type="primary" plain>选择文件</el-button>
                        </template>
                        <template #tip>
                            <div class="upload-tip">支持上传任意业务附件，当前采用本地文件存储。</div>
                        </template>
                    </el-upload>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="handleUpload">上传</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAttachment, downloadAttachment, getAttachmentList, uploadAttachment } from '@/api/file/attachment'
import { usePermission } from '@/composables/use_permission'

const formRef = ref()
const uploadRef = ref()
const attachmentList = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const selectedFile = ref(null)
const uploadFileList = ref([])
const { hasPermission } = usePermission()

const bizTypeOptions = [
    { label: '课程', value: 'course' },
    { label: '公告', value: 'notice' },
    { label: '申请', value: 'application' },
    { label: '学生', value: 'student' },
    { label: '教师', value: 'teacher' }
]

const query = reactive({
    bizType: '',
    bizId: ''
})

const form = reactive({
    bizType: '',
    bizId: ''
})

const rules = {
    bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
    bizId: [{ required: true, message: '请输入业务ID', trigger: 'blur' }]
}

async function loadAttachments() {
    if (!query.bizType || !query.bizId) {
        attachmentList.value = []
        return
    }
    const response = await getAttachmentList({
        bizType: query.bizType,
        bizId: Number(query.bizId)
    })
    attachmentList.value = response.data
}

function resetQuery() {
    query.bizType = ''
    query.bizId = ''
    attachmentList.value = []
}

function openUploadDialog() {
    form.bizType = query.bizType
    form.bizId = query.bizId
    selectedFile.value = null
    uploadFileList.value = []
    dialogVisible.value = true
}

function handleFileChange(file) {
    selectedFile.value = file.raw
}

function handleFileRemove() {
    selectedFile.value = null
}

async function handleUpload() {
    await formRef.value.validate()
    if (!selectedFile.value) {
        ElMessage.warning('请选择上传文件')
        return
    }

    const payload = new FormData()
    payload.append('bizType', form.bizType)
    payload.append('bizId', Number(form.bizId))
    payload.append('file', selectedFile.value)

    submitting.value = true
    try {
        await uploadAttachment(payload)
        ElMessage.success('附件上传成功')
        dialogVisible.value = false
        query.bizType = form.bizType
        query.bizId = form.bizId
        await loadAttachments()
    } finally {
        submitting.value = false
    }
}

async function handleDownload(id, fileName) {
    const blob = await downloadAttachment(id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除后将同步移除本地附件文件，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteAttachment(id)
    ElMessage.success('附件删除成功')
    loadAttachments()
}

function formatBizType(value) {
    const target = bizTypeOptions.find((item) => item.value === value)
    return target ? target.label : value
}

function formatFileSize(size = 0) {
    if (size < 1024) {
        return `${size} B`
    }
    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`
    }
    return `${(size / 1024 / 1024).toFixed(2)} MB`
}
</script>

<style scoped>
.attachment-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.toolbar-card,
.table-card {
    padding: 20px;
}

.query-form {
    margin-top: 18px;
}

.table-hint {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    color: #64748b;
    font-size: 13px;
}

.page-title {
    margin: 0;
    font-size: 24px;
}

.page-subtitle {
    margin: 6px 0 0;
    color: #64748b;
}

.upload-tip {
    color: #64748b;
    font-size: 12px;
}
</style>
