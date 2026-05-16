<template>
    <el-dialog
        :model-value="modelValue"
        :title="dialogTitle"
        width="760px"
        destroy-on-close
        @close="handleClose"
    >
        <el-empty v-if="!normalizedBizId" description="请先保存业务数据后再管理附件。" />

        <template v-else>
            <div class="attachment-header">
                <div>
                    <div class="attachment-biz-label">{{ currentBizLabel }}</div>
                    <div class="attachment-biz-meta">业务类型：{{ bizTypeLabel }} · 业务ID：{{ normalizedBizId }}</div>
                </div>
                <el-button
                    v-if="hasPermission('file:attachment:upload')"
                    type="success"
                    @click="openUploadDialog"
                >
                    上传附件
                </el-button>
            </div>

            <el-table v-loading="loading" :data="attachmentList" stripe>
                <el-table-column prop="fileName" label="文件名" min-width="280" show-overflow-tooltip />
                <el-table-column label="文件大小" min-width="120">
                    <template #default="{ row }">
                        {{ formatFileSize(row.fileSize) }}
                    </template>
                </el-table-column>
                <el-table-column prop="contentType" label="MIME类型" min-width="180" show-overflow-tooltip />
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
        </template>

        <el-dialog
            v-model="uploadDialogVisible"
            title="上传附件"
            width="560px"
            append-to-body
            destroy-on-close
        >
            <el-form ref="formRef" :model="uploadForm" :rules="uploadRules" label-width="96px">
                <el-form-item label="业务类型">
                    <el-input :model-value="bizTypeLabel" disabled />
                </el-form-item>
                <el-form-item label="业务ID">
                    <el-input :model-value="String(normalizedBizId || '')" disabled />
                </el-form-item>
                <el-form-item label="附件文件" prop="file">
                    <el-upload
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
                            <div class="upload-tip">当前附件将直接绑定到该业务记录。</div>
                        </template>
                    </el-upload>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="uploadDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="handleUpload">上传</el-button>
            </template>
        </el-dialog>
    </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAttachment, downloadAttachment, getAttachmentList, uploadAttachment } from '@/api/file/attachment'
import { usePermission } from '@/composables/use_permission'

defineOptions({
    name: 'BusinessAttachmentDialog'
})

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    bizType: {
        type: String,
        required: true
    },
    bizId: {
        type: [Number, String],
        default: null
    },
    bizLabel: {
        type: String,
        default: ''
    }
})

const emit = defineEmits(['update:modelValue'])

const formRef = ref()
const attachmentList = ref([])
const loading = ref(false)
const uploadDialogVisible = ref(false)
const submitting = ref(false)
const selectedFile = ref(null)
const uploadFileList = ref([])
const { hasPermission } = usePermission()

const bizTypeMap = {
    course: '课程',
    notice: '公告',
    application: '申请'
}

const uploadForm = reactive({
    file: null
})

const uploadRules = {
    file: [{ required: true, message: '请选择上传文件', trigger: 'change' }]
}

const normalizedBizId = computed(() => {
    if (props.bizId === null || props.bizId === undefined || props.bizId === '') {
        return null
    }
    return Number(props.bizId)
})

const bizTypeLabel = computed(() => bizTypeMap[props.bizType] || props.bizType)
const currentBizLabel = computed(() => props.bizLabel || `${bizTypeLabel.value}附件`)
const dialogTitle = computed(() => `${bizTypeLabel.value}附件`)

watch(
    () => [props.modelValue, normalizedBizId.value],
    ([visible, bizId]) => {
        if (visible && bizId) {
            loadAttachments()
        }
        if (!visible) {
            resetUploadState()
        }
    },
    { immediate: true }
)

async function loadAttachments() {
    if (!normalizedBizId.value) {
        attachmentList.value = []
        return
    }

    loading.value = true
    try {
        const response = await getAttachmentList({
            bizType: props.bizType,
            bizId: normalizedBizId.value
        })
        attachmentList.value = response.data
    } finally {
        loading.value = false
    }
}

function openUploadDialog() {
    resetUploadState()
    uploadDialogVisible.value = true
}

function handleFileChange(file) {
    selectedFile.value = file.raw
    uploadForm.file = file.raw
}

function handleFileRemove() {
    selectedFile.value = null
    uploadForm.file = null
}

async function handleUpload() {
    await formRef.value.validate()

    const payload = new FormData()
    payload.append('bizType', props.bizType)
    payload.append('bizId', normalizedBizId.value)
    payload.append('file', selectedFile.value)

    submitting.value = true
    try {
        await uploadAttachment(payload)
        ElMessage.success('附件上传成功')
        uploadDialogVisible.value = false
        resetUploadState()
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
    await ElMessageBox.confirm('删除后将同步移除附件文件，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteAttachment(id)
    ElMessage.success('附件删除成功')
    await loadAttachments()
}

function handleClose() {
    uploadDialogVisible.value = false
    resetUploadState()
    emit('update:modelValue', false)
}

function resetUploadState() {
    selectedFile.value = null
    uploadFileList.value = []
    uploadForm.file = null
    formRef.value?.clearValidate?.()
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
.attachment-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
}

.attachment-biz-label {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
}

.attachment-biz-meta {
    margin-top: 6px;
    font-size: 13px;
    color: #64748b;
}

.upload-tip {
    color: #64748b;
    font-size: 13px;
}

@media (max-width: 768px) {
    .attachment-header {
        align-items: flex-start;
        flex-direction: column;
    }
}
</style>
