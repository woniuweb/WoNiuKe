import axios from 'axios'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const apiBase = process.env.VUE_APP_API_BASE || ''

const request = axios.create({
	baseURL: apiBase ? `${apiBase}/` : '/',
	timeout: 10000,
})

request.interceptors.request.use(config => {
	NProgress.start()
	const identification = window.localStorage.getItem('identification')
	if (identification && !(config.url.startsWith('http://') || config.url.startsWith('https://'))) {
		config.headers.identification = identification
	}
	return config
})

request.interceptors.response.use(config => {
	NProgress.done()
	const identification = config.headers.identification
	if (identification) {
		window.localStorage.setItem('identification', identification)
	}
	return config.data
})

export default request
