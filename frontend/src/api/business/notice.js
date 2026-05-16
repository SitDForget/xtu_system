import request from '@/utils/request'

export function getNoticePage(params) {
    return request({
        url: '/notices',
        method: 'get',
        params
    })
}

export function getNoticeDetail(id) {
    return request({
        url: `/notices/${id}`,
        method: 'get'
    })
}

export function createNotice(data) {
    return request({
        url: '/notices',
        method: 'post',
        data
    })
}

export function updateNotice(id, data) {
    return request({
        url: `/notices/${id}`,
        method: 'put',
        data
    })
}

export function deleteNotice(id) {
    return request({
        url: `/notices/${id}`,
        method: 'delete'
    })
}

export function updateNoticePublishStatus(id, data) {
    return request({
        url: `/notices/${id}/publish`,
        method: 'put',
        data
    })
}

export function revokeNotice(id) {
    return request({
        url: `/notices/${id}/revoke`,
        method: 'put'
    })
}
