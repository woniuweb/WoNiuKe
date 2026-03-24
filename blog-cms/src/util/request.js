import axios from 'axios'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import {Message} from 'element-ui'

const apiBase = process.env.VUE_APP_API_BASE || ''

const request = axios.create({
	baseURL: apiBase ? `${apiBase}/admin/` : '/admin/',
	timeout: 5000
})

let CancelToken = axios.CancelToken

request.interceptors.request.use(config => {
	const userJson = window.localStorage.getItem('user') || '{}'
	const user = JSON.parse(userJson)
	if (userJson !== '{}' && user.role !== 'ROLE_admin' && config.method !== 'get') {
		config.cancelToken = new CancelToken(function executor(cancel) {
			cancel('演示模式，不允许操作')
		})
		return config
	}

	NProgress.start()
	const token = window.localStorage.getItem('token')
	if (token) {
		config.headers.Authorization = token
	}
	return config
},
error => {
	console.info(error)
	return Promise.reject(error)
})

request.interceptors.response.use(response => {
	NProgress.done()
	const res = response.data
	if (res.code !== 200) {
		let msg = res.msg || 'Error'
		Message.error(msg)
		return Promise.reject(new Error(msg))
	}
	return res
},
error => {
	console.info(error)
	Message.error(error.message)
	return Promise.reject(error)
})

export default request
