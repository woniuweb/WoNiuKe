<template>
	<div>
		<div class="ui padded attached segment m-padded-tb-large m-margin-bottom-big m-box blog-card" v-for="item in blogList" :key="item.id" @click="toBlog(item)">
			<div class="ui large red right corner label top-badge" v-if="item.top">
				<i class="arrow alternate circle up icon"></i>
			</div>
			<div class="ui middle aligned mobile reversed stackable">
				<div class="ui grid m-margin-lr">
					<!--分类-->
					<router-link :to="`/category/${item.category.name}`" class="ui orange large ribbon label category-label" @click.native.stop>
						<i class="small folder open icon"></i><span class="m-text-500">{{ item.category.name }}</span>
					</router-link>
					<!--标题-->
					<div class="row m-padded-tb-small">
						<h2 class="ui header m-center m-scaleup blog-title">
							<a href="javascript:;" class="m-black">{{ item.title }}</a>
						</h2>
					</div>
					<!--文章简要信息-->
					<div class="row m-padded-tb-small">
						<div class="ui horizontal link list m-center blog-meta">
							<div class="item m-datetime">
								<i class="small calendar icon"></i><span>{{ item.createTime | dateFormat('YYYY-MM-DD')}}</span>
							</div>
							<div class="item m-views">
								<i class="small eye icon"></i><span>{{ item.views }}</span>
							</div>
							<div class="item m-common-black">
								<i class="small pencil alternate icon"></i><span>字数≈{{ item.words }}字</span>
							</div>
							<div class="item m-common-black">
								<i class="small clock icon"></i><span>阅读时长≈{{ item.readTime }}分</span>
							</div>
						</div>
					</div>
					<!--文章Markdown描述-->
					<div class="typo m-padded-tb-small line-numbers match-braces rainbow-braces blog-description" v-lazy-container="{selector: 'img'}" v-viewer v-html="item.description"></div>
					<!--横线-->
					<div class="ui section divider m-margin-lr-no"></div>
					<!--标签-->
					<div class="row m-padded-tb-no">
						<div class="column m-padding-left-no">
							<router-link :to="`/tag/${tag.name}`" class="ui tag label m-text-500 m-margin-small blog-tag" :class="getTagColor(tag.color, index)" v-for="(tag,index) in item.tags" :key="index" @click.native.stop>{{ tag.name }}</router-link>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: "BlogItem",
		props: {
			blogList: {
				type: Array,
				required: true
			}
		},
		methods: {
			toBlog(blog) {
				this.$store.dispatch('goBlogPage', blog)
			},
			getTagColor(color, index) {
				if (color) return color
				const colors = ['red', 'orange', 'yellow', 'olive', 'green', 'teal', 'blue', 'violet', 'purple', 'pink']
				return colors[index % colors.length]
			}
		}
	}
</script>

<style scoped>
	.blog-card {
		border-radius: 12px !important;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08) !important;
		transition: all 0.3s ease !important;
		overflow: visible !important;
		background: white !important;
		border: 1px solid rgba(0, 0, 0, 0.08) !important;
		cursor: pointer;
		position: relative;
	}

	.blog-card:hover {
		transform: translateY(-5px);
		box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
		border-color: rgba(102, 126, 234, 0.2) !important;
	}

	.top-badge {
		border-radius: 0 0 0 12px !important;
	}

	.blog-title a {
		color: #2c3e50 !important;
		font-weight: 600 !important;
		transition: all 0.3s ease;
	}

	.blog-card:hover .blog-title a {
		color: #667eea !important;
	}

	.blog-meta {
		background: rgba(102, 126, 234, 0.05);
		padding: 10px 18px;
		border-radius: 8px;
		display: inline-flex !important;
	}

	.blog-meta .item {
		color: #666 !important;
		font-size: 13px;
		padding: 0 10px !important;
		border-right: 1px solid rgba(0, 0, 0, 0.08);
	}

	.blog-meta .item:last-child {
		border-right: none;
	}

	.blog-meta .item i {
		color: #667eea !important;
	}

	.category-label {
		margin-top: 0 !important;
		margin-left: -1em !important;
		z-index: 10;
		position: relative;
	}

	.blog-description {
		padding: 15px 0 !important;
		line-height: 1.8 !important;
		color: #555 !important;
	}

	.blog-tag {
		transition: all 0.2s ease !important;
	}

	.blog-tag:hover {
		opacity: 0.8;
		transform: translateY(-1px);
	}
</style>
