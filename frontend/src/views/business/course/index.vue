<template>
    <div class="course-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">课程管理</h1>
                <p class="page-subtitle">维护课程基础信息，支持部门、教师、学分和学期等关键字段管理。</p>
            </div>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-input v-model="query.keyword" clearable placeholder="课程编码或名称" />
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.deptId" clearable placeholder="所属部门" style="width: 180px;">
                        <el-option
                            v-for="option in flatDepartmentOptions"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px;">
                        <el-option :value="1" label="启用" />
                        <el-option :value="0" label="停用" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadCourses">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('business:course:create')" type="success" @click="openCreateDialog">新增课程</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="courseList" stripe>
                <el-table-column prop="courseCode" label="课程编码" min-width="140" />
                <el-table-column prop="courseName" label="课程名称" min-width="180" />
                <el-table-column prop="deptName" label="所属部门" min-width="160" />
                <el-table-column prop="teacherName" label="授课教师" min-width="120" />
                <el-table-column prop="credit" label="学分" width="100" />
                <el-table-column prop="courseType" label="课程类型" min-width="120" />
                <el-table-column prop="semester" label="开课学期" min-width="120" />
                <el-table-column label="附件" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.attachmentCount > 0 ? 'primary' : 'info'">
                            {{ row.attachmentCount || 0 }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">
                            {{ row.status === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="240" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="hasPermission('business:course:update')" link type="primary" @click="openEditDialog(row.id)">编辑</el-button>
                        <el-button v-if="hasPermission('business:course:delete')" link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="课程编码" prop="courseCode">
                    <el-input v-model="form.courseCode" />
                </el-form-item>
                <el-form-item label="课程名称" prop="courseName">
                    <el-input v-model="form.courseName" />
                </el-form-item>
                <el-form-item label="所属部门" prop="deptId">
                    <el-select v-model="form.deptId" placeholder="请选择所属部门" style="width: 100%;">
                        <el-option
                            v-for="option in flatDepartmentOptions"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="授课教师" prop="teacherId">
                    <el-select v-model="form.teacherId" clearable placeholder="请选择授课教师" style="width: 100%;">
                        <el-option
                            v-for="teacher in teacherOptions"
                            :key="teacher.id"
                            :label="teacher.teacherName"
                            :value="teacher.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="学分" prop="credit">
                    <el-input-number v-model="form.credit" :precision="1" :step="0.5" :min="0" :max="10" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="课程类型" prop="courseType">
                    <el-input v-model="form.courseType" />
                </el-form-item>
                <el-form-item label="开课学期" prop="semester">
                    <el-input v-model="form.semester" placeholder="例如：2026春" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :value="1">启用</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="3" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission(form.id ? 'business:course:update' : 'business:course:create')"
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
            biz-type="course"
            :biz-id="attachmentTarget.id"
            :biz-label="attachmentTarget.label"
        />
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCourse, deleteCourse, getCourseDetail, getCoursePage, updateCourse } from '@/api/business/course'
import { getDepartmentOptions } from '@/api/organization/department'
import { getTeacherPage } from '@/api/personnel/teacher'
import { usePermission } from '@/composables/use_permission'
import BusinessAttachmentDialog from '@/components/business_attachment_dialog.vue'

const formRef = ref()
const courseList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const attachmentDialogVisible = ref(false)
const submitting = ref(false)
const departmentOptions = ref([])
const teacherOptions = ref([])
const { hasPermission } = usePermission()
const attachmentTarget = reactive({
    id: null,
    label: ''
})

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    deptId: null,
    status: null
})

const form = reactive({
    id: null,
    courseCode: '',
    courseName: '',
    deptId: null,
    teacherId: null,
    credit: 0,
    courseType: '',
    semester: '',
    status: 1,
    remark: ''
})

const rules = {
    courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }],
    courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
    deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }]
}

const dialogTitle = computed(() => (form.id ? '编辑课程' : '新增课程'))
const flatDepartmentOptions = computed(() => flattenOptions(departmentOptions.value))

function resetForm() {
    Object.assign(form, {
        id: null,
        courseCode: '',
        courseName: '',
        deptId: null,
        teacherId: null,
        credit: 0,
        courseType: '',
        semester: '',
        status: 1,
        remark: ''
    })
}

async function loadCourses() {
    const response = await getCoursePage(query)
    courseList.value = response.data.list
    total.value = response.data.total
}

function resetQuery() {
    query.keyword = ''
    query.deptId = null
    query.status = null
    query.pageNum = 1
    loadCourses()
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const response = await getCourseDetail(id)
    Object.assign(form, response.data)
    dialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateCourse(form.id, form)
            ElMessage.success('课程更新成功')
        } else {
            await createCourse(form)
            ElMessage.success('课程创建成功')
        }
        dialogVisible.value = false
        loadCourses()
    } finally {
        submitting.value = false
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除后将进入逻辑删除状态，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteCourse(id)
    ElMessage.success('课程删除成功')
    loadCourses()
}

function openAttachmentDialog(row) {
    attachmentTarget.id = row.id
    attachmentTarget.label = `${row.courseName}（${row.courseCode}）`
    attachmentDialogVisible.value = true
}

function handlePageChange(page) {
    query.pageNum = page
    loadCourses()
}

async function loadBaseOptions() {
    const [departmentResponse, teacherResponse] = await Promise.all([
        getDepartmentOptions(),
        getTeacherPage({
            pageNum: 1,
            pageSize: 200,
            status: 1
        })
    ])
    departmentOptions.value = departmentResponse.data
    teacherOptions.value = teacherResponse.data.list
}

function flattenOptions(tree, level = 0) {
    return tree.flatMap((item) => {
        const prefix = level === 0 ? '' : `${'　'.repeat(level)}└ `
        const current = {
            label: `${prefix}${item.label}`,
            value: item.value
        }
        return [current, ...flattenOptions(item.children || [], level + 1)]
    })
}

onMounted(() => {
    loadBaseOptions()
    loadCourses()
})
</script>

<style scoped>
.course-page {
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
