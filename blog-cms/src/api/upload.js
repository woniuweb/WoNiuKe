import axios from '@/util/request'

function createFormData(file) {
	const formData = new FormData()
	formData.append('file', file)
	return formData
}

export function uploadImage(file) {
	return axios({
		url: 'upload/image',
		method: 'POST',
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		data: createFormData(file)
	})
}

export function uploadVideo(file) {
	return axios({
		url: 'upload/video',
		method: 'POST',
		timeout: 10 * 60 * 1000,
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		data: createFormData(file)
	})
}

export function getVideoList() {
	return axios({
		url: 'upload/videos',
		method: 'GET'
	})
}

export function deleteVideo(id) {
	return axios({
		url: 'upload/video',
		method: 'DELETE',
		params: {
			id
		}
	})
}
