<template>
	<!--随机文章-->
	<div class="ui segments m-box random-container">
		<div class="ui secondary segment random-header">
			<i class="bookmark icon"></i>随机文章
		</div>
		<div class="ui yellow segment">
			<div class="ui divided items">
				<div class="m-item" v-for="blog in randomBlogList" :key="blog.id" @click.prevent="toBlog(blog)">
					<div class="img" :style="{'background-image':'url(' + blog.firstPicture + ')'}"></div>
					<div class="info">
						<div class="date">{{ blog.createTime | dateFormat('YYYY-MM-DD') }}</div>
						<div class="title">{{ blog.title }}</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: "RandomBlog",
		props: {
			randomBlogList: {
				type: Array,
				required: true
			},
		},
		methods: {
			toBlog(blog) {
				this.$store.dispatch('goBlogPage', blog)
			}
		}
	}
</script>

<style scoped>
	.random-container {
		border-radius: 12px !important;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08) !important;
		overflow: hidden;
		transition: all 0.3s ease;
	}

	.random-container:hover {
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12) !important;
	}

	.random-header {
		padding: 12px 15px !important;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
		color: white !important;
		font-size: 15px;
		font-weight: 600;
		border: none !important;
	}

	.random-header .icon {
		margin-right: 6px;
	}

	.ui.yellow.segment {
		padding: 15px !important;
		background: white !important;
		margin: 0 !important;
	}

	.ui.divided.items .m-item:first-child {
		margin-top: 0;
	}

	.ui.divided.items .m-item {
		margin-top: 12px;
		height: 100px;
		position: relative;
		overflow: hidden;
		border-radius: 8px;
		cursor: pointer;
		user-select: none;
		transition: all 0.3s ease;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	}

	.ui.divided.items .m-item:hover {
		transform: translateY(-3px);
		box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
	}

	.ui.divided.items .m-item .img {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		object-fit: cover;
		background-position: center;
		background-size: cover;
		transition: transform 0.5s ease;
	}

	.ui.divided.items .m-item:hover .img {
		transform: scale(1.1);
	}

	.ui.divided.items .m-item .info {
		z-index: 1;
		background: linear-gradient(to bottom, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.75));
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 10px 12px !important;
		font-size: 12px;
		color: white;
	}

	.ui.divided.items .m-item .info .date {
		font-size: 11px;
		opacity: 0.9;
		margin-bottom: 4px;
	}

	.ui.divided.items .m-item .info .title {
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		word-break: break-word;
		line-height: 1.4;
		font-weight: 500;
	}
</style>