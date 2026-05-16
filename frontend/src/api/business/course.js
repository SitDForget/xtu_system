import request from '@/utils/request'

export function getCoursePage(params) {
    return request({
        url: '/courses',
        method: 'get',
        params
    })
}

export function getCourseOptions() {
    return request({
        url: '/courses/options',
        method: 'get'
    })
}

export function getCourseDetail(id) {
    return request({
        url: `/courses/${id}`,
        method: 'get'
    })
}

export function createCourse(data) {
    return request({
        url: '/courses',
        method: 'post',
        data
    })
}

export function updateCourse(id, data) {
    return request({
        url: `/courses/${id}`,
        method: 'put',
        data
    })
}

export function deleteCourse(id) {
    return request({
        url: `/courses/${id}`,
        method: 'delete'
    })
}
