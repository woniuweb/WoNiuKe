<template>
	<div>
		<el-row :gutter="20">
			<el-col :span="12">
				<el-card>
					<div slot="header">
						<span>基础设置</span>
					</div>
					<el-form label-position="right" label-width="100px">
						<el-form-item
							v-for="item in normalType1Settings"
							:key="item.id || item.nameEn"
							:label="item.nameZh"
						>
							<el-input v-model="item.value" size="mini"></el-input>
						</el-form-item>
					</el-form>
				</el-card>
			</el-col>
			<el-col :span="12">
				<el-card>
					<div slot="header">
						<span>资料卡片</span>
					</div>
					<el-form label-position="right" label-width="100px">
						<el-form-item v-for="item in typeMap.type2" :key="item.id || item.key" :label="item.nameZh">
							<div v-if="item.nameEn === 'favorite'">
								<el-col :span="20">
									<el-input v-model="item.value" size="mini"></el-input>
								</el-col>
								<el-col :span="4">
									<el-button type="danger" size="mini" icon="el-icon-delete" @click="deleteFavorite(item)">删除</el-button>
								</el-col>
							</div>
							<div v-else>
								<el-input v-model="item.value" size="mini"></el-input>
							</div>
						</el-form-item>
						<el-button type="primary" size="mini" icon="el-icon-plus" @click="addFavorite">添加自定义</el-button>
					</el-form>
				</el-card>
			</el-col>
		</el-row>

		<el-row style="margin-top: 20px">
			<el-card>
				<div slot="header" class="video-header">
					<span>首页视频</span>
					<el-button size="mini" icon="el-icon-refresh" @click="loadVideoList">刷新列表</el-button>
				</div>

				<el-form label-position="right" label-width="100px">
					<el-form-item label="当前视频">
						<el-input v-model="homeVideoUrl" size="mini" readonly></el-input>
					</el-form-item>
				</el-form>

				<el-upload
					action=""
					:auto-upload="false"
					:show-file-list="false"
					:on-change="handleVideoChange"
					accept=".mp4,.webm,.ogg,video/mp4,video/webm,video/ogg"
				>
					<el-button size="mini" type="primary" icon="el-icon-upload">上传视频</el-button>
				</el-upload>

				<el-alert
					class="video-alert"
					title="推荐首页背景视频压缩到 20MB 以内。当前支持较大文件上传，但超大视频只建议桌面端展示。"
					type="warning"
					:closable="false"
					show-icon
				></el-alert>

				<div class="video-list" v-loading="videoListLoading">
					<div class="video-card" v-for="item in videoList" :key="item.id">
						<div class="video-preview-wrap">
							<img
								v-if="item.posterUrl"
								class="video-poster"
								:src="item.posterUrl"
								:alt="item.originalName || item.fileName"
							>
							<div v-else class="video-preview-placeholder">
								<span>暂无封面</span>
							</div>
						</div>
						<div class="video-info">
							<div class="video-name" :title="item.originalName || item.fileName">{{ item.originalName || item.fileName }}</div>
							<div class="video-meta">视频地址：{{ item.url }}</div>
							<div class="video-meta" v-if="item.posterUrl">封面地址：{{ item.posterUrl }}</div>
							<div class="video-meta" :class="{ 'video-meta-warning': isLargeVideo(item.fileSize) }">
								文件大小：{{ formatFileSize(item.fileSize) }}
								<span v-if="isLargeVideo(item.fileSize)">（建议仅桌面端展示）</span>
							</div>
							<div class="video-meta">上传时间：{{ formatDate(item.createTime) }}</div>
						</div>
						<div class="video-actions">
							<el-button
								size="mini"
								type="primary"
								:loading="currentVideoLoading === item.url"
								@click="useVideo(item.url)"
							>
								设为首页视频
							</el-button>
							<el-button size="mini" @click="copyText(item.url)">复制视频地址</el-button>
							<el-button v-if="item.posterUrl" size="mini" @click="copyText(item.posterUrl)">复制封面地址</el-button>
							<el-button
								size="mini"
								type="danger"
								:loading="deleteVideoLoading === item.id"
								@click="handleDeleteVideo(item)"
							>
								删除
							</el-button>
						</div>
					</div>
					<el-empty v-if="!videoListLoading && !videoList.length" description="暂无视频"></el-empty>
				</div>
			</el-card>
		</el-row>

		<el-row style="margin-top: 20px">
			<el-card>
				<div slot="header">
					<span>页脚徽标</span>
				</div>
				<el-form :inline="true" v-for="badge in typeMap.type3" :key="badge.id || badge.key">
					<el-form-item label="title">
						<el-input v-model="badge.value.title" size="mini"></el-input>
					</el-form-item>
					<el-form-item label="url">
						<el-input v-model="badge.value.url" size="mini"></el-input>
					</el-form-item>
					<el-form-item label="subject">
						<el-input v-model="badge.value.subject" size="mini"></el-input>
					</el-form-item>
					<el-form-item label="value">
						<el-input v-model="badge.value.value" size="mini"></el-input>
					</el-form-item>
					<el-form-item label="color">
						<el-input v-model="badge.value.color" size="mini"></el-input>
					</el-form-item>
					<el-form-item>
						<el-button type="danger" size="mini" icon="el-icon-delete" @click="deleteBadge(badge)">删除</el-button>
					</el-form-item>
				</el-form>
				<el-button type="primary" size="mini" icon="el-icon-plus" @click="addBadge">添加 badge</el-button>
			</el-card>
		</el-row>

		<div style="text-align: right; margin-top: 30px">
			<el-button type="primary" icon="el-icon-check" @click="submit">保存站点设置</el-button>
		</div>
	</div>
</template>

<script>
import {getSiteSettingData, getHomeVideo, update, updateHomeVideo} from "@/api/siteSetting";
import {deleteVideo, getVideoList, uploadVideo} from "@/api/upload";
import _ from 'lodash'

const VIDEO_SETTING_KEYS = ['videoUrl', 'videoPoster']

export default {
	name: "SiteSetting",
	data() {
		return {
			deleteIds: [],
			typeMap: {
				type1: [],
				type2: [],
				type3: [],
			},
			homeVideoUrl: '',
			videoList: [],
			videoListLoading: false,
			currentVideoLoading: '',
			deleteVideoLoading: '',
		}
	},
	computed: {
		normalType1Settings() {
			return this.typeMap.type1.filter(item => !VIDEO_SETTING_KEYS.includes(item.nameEn))
		}
	},
	created() {
		this.getData()
		this.loadHomeVideo()
		this.loadVideoList()
	},
	methods: {
		getData() {
			getSiteSettingData().then(res => {
				const typeMap = res.data || {type1: [], type2: [], type3: []}
				typeMap.type1 = (typeMap.type1 || []).filter(item => !VIDEO_SETTING_KEYS.includes(item.nameEn))
				typeMap.type2 = typeMap.type2 || []
				typeMap.type3 = typeMap.type3 || []
				typeMap.type3.forEach(item => {
					item.value = JSON.parse(item.value)
				})
				this.typeMap = typeMap
			})
		},
		loadHomeVideo() {
			getHomeVideo().then(res => {
				this.homeVideoUrl = res.data || ''
			})
		},
		loadVideoList() {
			this.videoListLoading = true
			getVideoList().then(res => {
				this.videoList = res.data || []
			}).finally(() => {
				this.videoListLoading = false
			})
		},
		handleVideoChange(file) {
			const rawFile = file.raw
			if (!rawFile) {
				return
			}
			uploadVideo(rawFile).then(res => {
				this.msgSuccess(res.msg)
				this.homeVideoUrl = res.data.url
				this.loadVideoList()
			})
		},
		useVideo(url) {
			this.currentVideoLoading = url
			updateHomeVideo(url).then(res => {
				this.homeVideoUrl = res.data || url
				this.msgSuccess('首页视频已更新')
			}).finally(() => {
				this.currentVideoLoading = ''
			})
		},
		handleDeleteVideo(item) {
			this.$confirm('删除后将同时移除视频文件和首帧封面，是否继续？', '删除视频', {
				confirmButtonText: '确定删除',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.deleteVideoLoading = item.id
				return deleteVideo(item.id)
			}).then(res => {
				this.msgSuccess(res.msg)
				this.loadVideoList()
			}).finally(() => {
				this.deleteVideoLoading = ''
			})
		},
		copyText(text) {
			const input = document.createElement('input')
			input.value = text
			document.body.appendChild(input)
			input.select()
			document.execCommand('copy')
			document.body.removeChild(input)
			this.msgSuccess('复制成功')
		},
		addFavorite() {
			this.typeMap.type2.push({
				key: Date.now(),
				nameEn: "favorite",
				nameZh: "自定义",
				type: 2,
				value: "{\"title\":\"\",\"content\":\"\"}"
			})
		},
		addBadge() {
			this.typeMap.type3.push({
				key: Date.now(),
				nameEn: "badge",
				nameZh: "徽标",
				type: 3,
				value: {
					color: "",
					subject: "",
					title: "",
					url: "",
					value: ""
				}
			})
		},
		deleteFavorite(favorite) {
			let arr = this.typeMap.type2
			if (favorite.id) {
				this.deleteIds.push(favorite.id)
				arr.forEach((item, index) => {
					if (item.id === favorite.id) {
						arr.splice(index, 1)
					}
				})
			} else {
				arr.forEach((item, index) => {
					if (item.key === favorite.key) {
						arr.splice(index, 1)
					}
				})
			}
		},
		deleteBadge(badge) {
			let arr = this.typeMap.type3
			if (badge.id) {
				this.deleteIds.push(badge.id)
				arr.forEach((item, index) => {
					if (item.id === badge.id) {
						arr.splice(index, 1)
					}
				})
			} else {
				arr.forEach((item, index) => {
					if (item.key === badge.key) {
						arr.splice(index, 1)
					}
				})
			}
		},
		submit() {
			const result = _.cloneDeep(this.typeMap)
			result.type3.forEach(item => {
				item.value = JSON.stringify(item.value)
			})
			let updateArr = []
			updateArr.push(...result.type1)
			updateArr.push(...result.type2)
			updateArr.push(...result.type3)
			update(updateArr, this.deleteIds).then(res => {
				this.deleteIds = []
				this.getData()
				this.msgSuccess(res.msg)
			})
		},
		formatFileSize(size) {
			if (!size && size !== 0) {
				return '-'
			}
			if (size < 1024) {
				return size + ' B'
			}
			if (size < 1024 * 1024) {
				return (size / 1024).toFixed(2) + ' KB'
			}
			if (size < 1024 * 1024 * 1024) {
				return (size / 1024 / 1024).toFixed(2) + ' MB'
			}
			return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
		},
		formatDate(value) {
			if (!value) {
				return '-'
			}
			return new Date(value).toLocaleString()
		},
		isLargeVideo(size) {
			return size > 20 * 1024 * 1024
		}
	}
}
</script>

<style scoped>
.video-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.video-alert {
	margin-top: 12px;
}

.video-list {
	margin-top: 20px;
}

.video-card {
	display: flex;
	align-items: center;
	padding: 16px;
	border: 1px solid #ebeef5;
	border-radius: 4px;
}

.video-card + .video-card {
	margin-top: 12px;
}

.video-preview-wrap {
	width: 240px;
	height: 135px;
	border-radius: 4px;
	overflow: hidden;
	flex-shrink: 0;
	background: #000;
}

.video-poster,
.video-preview-placeholder {
	width: 100%;
	height: 100%;
	display: block;
	background: #000;
}

.video-poster {
	object-fit: cover;
}

.video-preview-placeholder {
	display: flex;
	align-items: center;
	justify-content: center;
	color: rgba(255, 255, 255, 0.7);
	font-size: 14px;
	letter-spacing: 1px;
	background: linear-gradient(135deg, #3b4256 0%, #202636 100%);
}

.video-info {
	flex: 1;
	margin: 0 16px;
	overflow: hidden;
}

.video-name {
	font-weight: 600;
	margin-bottom: 8px;
}

.video-meta {
	color: #606266;
	font-size: 13px;
	line-height: 1.8;
	word-break: break-all;
}

.video-meta-warning {
	color: #e6a23c;
	font-weight: 600;
}

.video-actions {
	display: flex;
	flex-direction: column;
	gap: 8px;
}
</style>
