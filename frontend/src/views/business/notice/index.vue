<template>
    <div class="notice-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">公告管理</h1>
                <p class="page-subtitle">统一维护公告内容、发布状态和置顶策略。</p>
            </div>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-input v-model="query.keyword" clearable placeholder="公告标题" />
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.noticeType" clearable placeholder="公告类型" style="width: 160px;">
                        <el-option label="教学通知" value="教学通知" />
                        <el-option label="系统通知" value="系统通知" />
                        <el-option label="行政通知" value="行政通知" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.publishStatus" clearable placeholder="发布状态" style="width: 120px;">
                        <el-option :value="1" label="已发布" />
                        <el-option :value="0" label="草稿" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadNotices">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('business:notice:create')" type="success" @click="openCreateDialog">新增公告</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="noticeList" stripe>
                <el-table-column prop="title" label="标题" min-width="220" />
                <el-table-column prop="noticeType" label="类型" min-width="120" />
                <el-table-column label="置顶" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.pinned === 1 ? 'warning' : 'info'">
                            {{ row.pinned === 1 ? '置顶' : '普通' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="发布状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.publishStatus === 1 ? 'success' : 'info'">
                            {{ row.publishStatus === 1 ? '已发布' : '草稿' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="附件" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.attachmentCount > 0 ? 'primary' : 'info'">
                            {{ row.attachmentCount || 0 }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="publisherName" label="发布人" min-width="120" />
                <el-table-column prop="publishTime" label="发布时间" min-width="180" />
                <el-table-column label="操作" width="300" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="hasPermission('business:notice:update')" link type="primary" @click="openEditDialog(row.id)">编辑</el-button>
                        <el-button v-if="hasPermission('business:notice:delete')" link type="danger" @click="handleDelete(row.id)">删除</el-button>
                        <el-button
                            v-if="hasPermission('business:notice:publish')"
                            link
                            type="success"
                            @click="handlePublish(row)"
                        >
                            {{ row.publishStatus === 1 ? '撤回' : '发布' }}
                        </el-button>
                        <el-button
                            v-if="hasPermission('file:attachment:view')"
                            link
                            type="info"
                            @click="openAttachmentDialog(row)"
                        >
                            附件
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pager">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.pageNum"
                    :page-size="query.pageSize"
                    :total="total"
                    @current-change="handlePageChange"
                />
            </div>
        </section>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="公告标题" prop="title">
                    <el-input v-model="form.title" />
                </el-form-item>
                <el-form-item label="公告类型" prop="noticeType">
                    <el-select v-model="form.noticeType" clearable placeholder="请选择公告类型" style="width: 100%;">
                        <el-option label="教学通知" value="教学通知" />
                        <el-option label="系统通知" value="系统通知" />
                        <el-option label="行政通知" value="行政通知" />
                    </el-select>
                </el-form-item>
                <el-form-item label="发布状态" prop="publishStatus">
                    <el-radio-group v-model="form.publishStatus">
                        <el-radio :value="0">草稿</el-radio>
                        <el-radio :value="1">已发布</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="是否置顶" prop="pinned">
                    <el-radio-group v-model="form.pinned">
                        <el-radio :value="0">普通</el-radio>
                        <el-radio :value="1">置顶</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="公告内容" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="6" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission(form.id ? 'business:notice:update' : 'business:notice:create')"
                    type="primary"
                    :loading="submitting"
                    @click="handleSubmit"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <BusinessAttachmentDialog
            v-model="attachmentDialogVisible"
            biz-type="notice"
            :biz-id="attachmentTarget.id"
            :biz-label="attachmentTarget.label"
        />
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createNotice, deleteNotice, getNoticeDetail, getNoticePage, revokeNotice, updateNotice, updateNoticePublishStatus } from '@/api/business/notice'
import { usePermission } from '@/composables/use_permission'
import BusinessAttachmentDialog from '@/components/business_attachment_dialog.vue'

const formRef = ref()
const noticeList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const attachmentDialogVisible = ref(false)
const submitting = ref(false)
const { hasPermission } = usePermission()
const attachmentTarget = reactive({
    id: null,
    label: ''
})

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    noticeType: '',
    publishStatus: null
})

const form = reactive({
    id: null,
    title: '',
    noticeType: '',
    content: '',
    publishStatus: 0,
    pinned: 0
})

const rules = {
    title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }]
}

const dialogTitle = computed(() => (form.id ? '编辑公告' : '新增公告'))

function resetForm() {
    Object.assign(form, {
        id: null,
        title: '',
        noticeType: '',
        content: '',
        publishStatus: 0,
        pinned: 0
    })
}

async function loadNotices() {
    const response = await getNoticePage(query)
    noticeList.value = response.data.list
    total.value = response.data.total
}

function resetQuery() {
    query.keyword = ''
    query.noticeType = ''
    query.publishStatus = null
    query.pageNum = 1
    loadNotices()
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const response = await getNoticeDetail(id)
    Object.assign(form, response.data)
    dialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateNotice(form.id, form)
            ElMessage.success('公告更新成功')
        } else {
            await createNotice(form)
            ElMessage.success('公告创建成功')
        }
        dialogVisible.value = false
        loadNotices()
    } finally {
        submitting.value = false
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除后将进入逻辑删除状态，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteNotice(id)
    ElMessage.success('公告删除成功')
    loadNotices()
}

async function handlePublish(row) {
    if (row.publishStatus === 1) {
        await revokeNotice(row.id)
        ElMessage.success('公告已撤回')
    } else {
        await updateNoticePublishStatus(row.id, {
            publishStatus: 1
        })
        ElMessage.success('公告已发布')
    }
    loadNotices()
}

function openAttachmentDialog(row) {
    attachmentTarget.id = row.id
    attachmentTarget.label = row.title
    attachmentDialogVisible.value = true
}

function handlePageChange(page) {
    query.pageNum = page
    loadNotices()
}

onMounted(() => {
    loadNotices()
})
</script>

<style scoped>
.notice-page {
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

.pager {
    display: flex;
    justify-content: flex-end;
    margin-top: 18px;
}

.page-title {
    margin: 0;
    font-size: 24px;
}

.page-subtitle {
    margin: 6px 0 0;
    color: #64748b;
}
</style>
