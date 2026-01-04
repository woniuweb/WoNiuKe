<template>
	<!--标签云-->
	<div class="ui segments m-box tag-container">
		<div class="ui secondary segment tag-header">
			<i class="tags icon"></i>标签云
		</div>
		<div class="ui yellow segment m-padding-small">
			<router-link :to="`/tag/${tag.name}`" class="ui label m-text-500" :class="getTagColor(tag.color, index)" v-for="(tag,index) in tagList" :key="index">
				{{ tag.name }}
			</router-link>
		</div>
	</div>
</template>

<script>
	export default {
		name: "Tags",
		props: {
			tagList: {
				type: Array,
				required: true
			},
		},
		methods: {
			getTagColor(color, index) {
				if (color) return color
				const colors = ['red', 'orange', 'yellow', 'olive', 'green', 'teal', 'blue', 'violet', 'purple', 'pink']
				return colors[index % colors.length]
			}
		}
	}
</script>

<style scoped>
	.tag-container {
		border-radius: 12px !important;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08) !important;
		overflow: hidden;
		transition: all 0.3s ease;
	}

	.tag-container:hover {
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12) !important;
	}

	.tag-header {
		padding: 12px 15px !important;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
		color: white !important;
		font-size: 15px;
		font-weight: 600;
		border: none !important;
	}

	.tag-header .icon {
		margin-right: 6px;
	}

	.m-padding-small {
		padding: 15px !important;
		background: white !important;
	}

	.tag-item {
		margin: 4px !important;
		padding: 6px 14px !important;
		border-radius: 16px !important;
		font-size: 13px !important;
		transition: all 0.3s ease !important;
		cursor: pointer;
		display: inline-block;
	}

	.tag-item:hover {
		transform: translateY(-2px);
		box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15) !important;
	}
</style>