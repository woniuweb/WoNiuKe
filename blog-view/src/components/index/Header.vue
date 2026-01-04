<template>
	<header ref="header">
		<div class="view">
			<!-- 视频背景 -->
			<video 
				ref="videoBackground" 
				class="video-background" 
				:src="defaultSettings.videoUrl || '/video/bg-video.mp4'"
				autoplay 
				loop 
				muted 
				playsinline
				@loadeddata="onVideoLoaded"
				:style="{opacity: videoLoaded ? 1 : 0}"
			></video>
			<!-- 视频加载中的占位背景 -->
			<div class="loading-background" v-if="!videoLoaded">
				<div class="loading-spinner"></div>
			</div>
			<!-- 轻微的渐变遮罩，不影响视频清晰度 -->
			<div class="overlay-light"></div>
		</div>

		<div class="header-content">
			<div class="greeting-text">{{ greetingText }}</div>
			<div class="title-container">
				<h1 class="typewriter-text">{{ displayedText }}<span class="cursor">|</span></h1>
			</div>
			<div class="subtitle">{{ defaultSettings.subtitle || '记录生活' }}</div>
			<div class="social-links">
				<a v-if="defaultSettings.github" :href="defaultSettings.github" target="_blank" class="social-icon">
					<i class="el-icon-link"></i> GitHub
				</a>
				<a v-if="defaultSettings.email" :href="'mailto:'+defaultSettings.email" class="social-icon">
					<i class="el-icon-message"></i> Email
				</a>
			</div>
		</div>


		<div class="wrapper">
			<i class="ali-iconfont icon-down" @click="scrollToMain"></i>
		</div>
		<div class="wave1"></div>
		<div class="wave2"></div>

		<canvas ref="particleCanvas" class="particle-canvas"></canvas>
	</header>
</template>

<script>
	import {mapState} from 'vuex'
	import defaultSettings from '@/settings'

	export default {
		name: "Header",
		data() {
			return {
				loaded: false,
				videoLoaded: false,
				defaultSettings,
				displayedText: '',
				fullText: defaultSettings.headerTitle || 'Welcome to My Blog',
				typewriterIndex: 0,
				greetingText: '',
				particles: [],
				animationFrame: null
			}
		},
		computed: {
			...mapState(['clientSize'])
		},
		watch: {
			'clientSize.clientHeight'() {
				this.setHeaderHeight()
				this.initParticles()
			}
		},
		mounted() {
			this.setHeaderHeight()
			// this.setupMouseEffect() // 已注释：鼠标移动视图左右移动效果
			this.startTypewriter()
			this.setGreeting()
			this.initParticles()
			this.addScrollEffect()
			// 确保视频自动播放
			this.$nextTick(() => {
				const video = this.$refs.videoBackground
				if (video) {
					video.play().catch(e => {
						console.log('视频自动播放失败，使用后备图片')
					})
				}
			})
		},
		beforeDestroy() {
			if (this.animationFrame) {
				cancelAnimationFrame(this.animationFrame)
			}
			window.removeEventListener('scroll', this.handleScroll)
		},
		methods: {
			setHeaderHeight() {
				this.$refs.header.style.height = this.clientSize.clientHeight + 'px'
			},

			scrollToMain() {
				window.scrollTo({top: this.clientSize.clientHeight, behavior: 'smooth'})
			},

			// 已注释：鼠标移动时视图左右移动的效果
			// setupMouseEffect() {
			// 	let startingPoint
			// 	const header = this.$refs.header
			// 	header.addEventListener('mouseenter', (e) => {
			// 		startingPoint = e.clientX
			// 	})
			// 	header.addEventListener('mouseout', () => {
			// 		header.classList.remove('moving')
			// 		header.style.setProperty('--percentage', 0.5)
			// 	})
			// 	header.addEventListener('mousemove', (e) => {
			// 		let percentage = (e.clientX - startingPoint) / window.outerWidth + 0.5
			// 		header.style.setProperty('--percentage', percentage)
			// 		header.classList.add('moving')
			// 	})
			// },

			startTypewriter() {
				const type = () => {
					if (this.typewriterIndex < this.fullText.length) {
						this.displayedText += this.fullText.charAt(this.typewriterIndex)
						this.typewriterIndex++
						setTimeout(type, 150)
					}
				}
				setTimeout(type, 500)
			},

			setGreeting() {
				const hour = new Date().getHours()
				if (hour < 6) {
					this.greetingText = '🌙 夜深了，注意休息'
				} else if (hour < 12) {
					this.greetingText = '🌅 早上好'
				} else if (hour < 14) {
					this.greetingText = '🌞 中午好'
				} else if (hour < 18) {
					this.greetingText = '☀️ 下午好'
				} else {
					this.greetingText = '🌆 晚上好'
				}
			},

			initParticles() {
				const canvas = this.$refs.particleCanvas
				if (!canvas) return

				canvas.width = window.innerWidth
				canvas.height = this.clientSize.clientHeight
				const ctx = canvas.getContext('2d')

				this.particles = []
				const particleCount = 50

				for (let i = 0; i < particleCount; i++) {
					this.particles.push({
						x: Math.random() * canvas.width,
						y: Math.random() * canvas.height,
						radius: Math.random() * 2 + 1,
						speedX: (Math.random() - 0.5) * 0.5,
						speedY: (Math.random() - 0.5) * 0.5,
						opacity: Math.random() * 0.5 + 0.2
					})
				}

				const animate = () => {
					ctx.clearRect(0, 0, canvas.width, canvas.height)

					this.particles.forEach(particle => {
						particle.x += particle.speedX
						particle.y += particle.speedY

						if (particle.x < 0 || particle.x > canvas.width) particle.speedX *= -1
						if (particle.y < 0 || particle.y > canvas.height) particle.speedY *= -1

						ctx.beginPath()
						ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2)
						ctx.fillStyle = `rgba(255, 255, 255, ${particle.opacity})`
						ctx.fill()
					})

					this.animationFrame = requestAnimationFrame(animate)
				}

				animate()
			},

			addScrollEffect() {
				this.handleScroll = () => {
					const scrolled = window.pageYOffset
					const header = this.$refs.header
					if (header) {
						const scale = 1 + scrolled / 2000
						// 已移除：鼠标移动的 translatex 效果，只保留滚动缩放
						header.querySelector('.view').style.transform = `scale(${scale})`
						header.querySelector('.view').style.opacity = 1 - scrolled / 800
					}
				}
				window.addEventListener('scroll', this.handleScroll)
			},
			
			onVideoLoaded() {
				this.videoLoaded = true
				this.loaded = true
			}
		}
	}
</script>

<style scoped>
	header {
		--percentage: 0.5;
		user-select: none;
		position: relative;
		overflow: hidden;
	}

	.view {
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		display: flex;
		justify-content: center;
		/* 已注释：鼠标移动时的视图偏移效果 */
		/* transform: translatex(calc(var(--percentage) * 100px)); */
		transition: .2s all ease-in;
	}

	.video-background {
		position: absolute;
		top: 50%;
		left: 50%;
		min-width: 100%;
		min-height: 100%;
		width: auto;
		height: auto;
		transform: translate(-50%, -50%);
		z-index: 10;
		object-fit: cover;
		transition: opacity 0.5s ease-in;
	}

	.loading-background {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: linear-gradient(135deg, #667eea 0%, #4b67a2 100%);
		z-index: 5;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.loading-spinner {
		width: 50px;
		height: 50px;
		border: 4px solid rgba(255, 255, 255, 0.3);
		border-top-color: #fff;
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	}

	.overlay-light {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		/* 非常轻微的渐变，只在底部稍微加深，让文字更清晰 */
		background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0) 60%, rgba(0,0,0,0.2) 100%);
		z-index: 20;
	}

	/* 已注释：鼠标移动时的过渡效果 */
	/* header.moving .view {
		transition: none;
	} */

	.particle-canvas {
		position: absolute;
		top: 0;
		left: 0;
		z-index: 30;
		pointer-events: none;
	}

	.header-content {
		position: absolute;
		top: 45%;
		left: 50%;
		transform: translate(-50%, -50%);
		text-align: center;
		z-index: 50;
		width: 90%;
		max-width: 800px;
	}

	.greeting-text {
		font-size: 20px;
		color: rgba(255, 255, 255, 0.9);
		margin-bottom: 20px;
		animation: fadeInDown 1s ease-out;
	}

	.title-container {
		margin-bottom: 20px;
	}

	.typewriter-text {
		font-size: 48px;
		font-weight: bold;
		color: #fff;
		text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
		margin: 0;
		display: inline-block;
		animation: fadeInUp 1s ease-out;
	}

	.cursor {
		animation: blink 1s infinite;
	}

	@keyframes blink {
		0%, 50% { opacity: 1; }
		51%, 100% { opacity: 0; }
	}

	.subtitle {
		font-size: 20px;
		color: rgba(255, 255, 255, 0.85);
		margin-bottom: 30px;
		letter-spacing: 2px;
		animation: fadeInUp 1s ease-out 0.3s both;
	}

	.social-links {
		display: flex;
		justify-content: center;
		gap: 20px;
		animation: fadeInUp 1s ease-out 0.5s both;
	}

	.social-icon {
		display: inline-flex;
		align-items: center;
		gap: 5px;
		padding: 10px 20px;
		background: rgba(255, 255, 255, 0.15);
		backdrop-filter: blur(10px);
		border: 1px solid rgba(255, 255, 255, 0.3);
		border-radius: 25px;
		color: #fff;
		text-decoration: none;
		font-size: 14px;
		transition: all 0.3s ease;
	}

	.social-icon:hover {
		background: rgba(255, 255, 255, 0.25);
		transform: translateY(-3px);
		box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
	}

	@keyframes fadeInDown {
		from {
			opacity: 0;
			transform: translateY(-30px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	@keyframes fadeInUp {
		from {
			opacity: 0;
			transform: translateY(30px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.wrapper {
		position: absolute;
		width: 100px;
		bottom: 150px;
		left: 0;
		right: 0;
		margin: auto;
		font-size: 26px;
		z-index: 100;
	}

	.wrapper i {
		font-size: 60px;
		opacity: 0.5;
		cursor: pointer;
		position: absolute;
		top: 55px;
		left: 20px;
		color: #fff;
		animation: opener .5s ease-in-out alternate infinite;
		transition: opacity .2s ease-in-out, transform .5s ease-in-out .2s;
	}

	.wrapper i:hover {
		opacity: 1;
		transform: scale(1.1);
	}

	@keyframes opener {
		100% {
			top: 65px
		}
	}

	.wave1, .wave2 {
		position: absolute;
		bottom: 0;
		transition-duration: .4s, .4s;
		z-index: 80;
	}

	.wave1 {
		background: url('/img/header/wave1.png') repeat-x;
		height: 75px;
		width: 100%;
	}

	.wave2 {
		background: url('/img/header/wave2.png') repeat-x;
		height: 90px;
		width: calc(100% + 100px);
		left: -100px;
	}

	@media (max-width: 768px) {
		.typewriter-text {
			font-size: 32px;
		}

		.subtitle {
			font-size: 16px;
		}

		.greeting-text {
			font-size: 16px;
		}

		.social-links {
			flex-direction: column;
			align-items: center;
		}
	}
</style>
