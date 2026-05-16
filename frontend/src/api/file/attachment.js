import request from '@/utils/request'

export function uploadAttachment(data) {
    return request({
        url: '/attachments/upload',
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export function getAttachmentList(params) {
    return request({
        url: '/attachments',
        method: 'get',
        params
    })
}

export function getAttachmentDetail(id) {
    return request({
        url: `/attachments/${id}`,
        method: 'get'
    })
}

export function downloadAttachment(id) {
    return request({
        url: `/attachments/${id}/download`,
        method: 'get',
        responseType: 'blob'
    })
}

export function deleteAttachment(id) {
    return request({
        url: `/attachments/${id}`,
        method: 'delete'
    })
}
