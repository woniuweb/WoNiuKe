module.exports = {
	devServer: {
		proxy: {
			'/api': {
				target: 'http://localhost:8090',
				changeOrigin: true,
				pathRewrite: {
					'^/api': ''
				}
			},
			'/admin': {
				target: 'http://localhost:8090',
				changeOrigin: true
			},
			'/video': {
				target: 'http://localhost:8090',
				changeOrigin: true
			}
		}
	},
	configureWebpack: {
		resolve: {
			alias: {
				'assets': '@/assets',
				'common': '@/common',
				'components': '@/components',
				'api': '@/api',
				'views': '@/views',
				'plugins': '@/plugins'
			}
		},
		optimization: {
			splitChunks: {
				cacheGroups: {
					vendor: {
						test: /[\\/]node_modules[\\/]/,
						name(module) {
							const packageName = module.context.match(
								/[\\/]node_modules[\\/](.*?)([\\/]|$)/
							)[1];
							return `npm.${packageName.replace("@", "")}`;
						},
						chunks: "all",
						enforce: true,
						priority: 10,
						minSize: 50000,
						maxSize: 200000,
						reuseExistingChunk: true,
					},
				},
			},
		}
	}
}
