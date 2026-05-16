import request from '@/utils/request'

export function getApplicationPage(params) {
    return request({
        url: '/applications',
        method: 'get',
        params
    })
}

export function getApplicationDetail(id) {
    return request({
        url: `/applications/${id}`,
        method: 'get'
    })
}

export function createApplication(data) {
    return request({
        url: '/applications',
        method: 'post',
        data
    })
}

export function updateApplication(id, data) {
    return request({
        url: `/applications/${id}`,
        method: 'put',
        data
    })
}

export function deleteApplication(id) {
    return request({
        url: `/applications/${id}`,
        method: 'delete'
    })
}

export function submitApplication(id) {
    return request({
        url: `/applications/${id}/submit`,
        method: 'put'
    })
}

export function withdrawApplication(id) {
    return request({
        url: `/applications/${id}/withdraw`,
        method: 'put'
    })
}

export function getApplicationRecords(id) {
    return request({
        url: `/applications/${id}/records`,
        method: 'get'
    })
}

export function reviewApplication(id, data) {
    return request({
        url: `/applications/${id}/review`,
        method: 'put',
        data
    })
}
