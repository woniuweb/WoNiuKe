<template>
  <div class="login_container">
    <div class="login_box">
      <div class="avatar_box">
        <img src="/img/avatar.jpg" alt="">
      </div>
      <div class="login_title">
        <h2>欢迎回来</h2>
        <p>Welcome Back</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginFormRules" class="login_form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="el-icon-user-solid" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" show-password placeholder="请输入密码" @keyup.native.enter="login"></el-input>
        </el-form-item>
        <el-form-item class="btns">
          <el-button type="primary" @click="login" class="login-btn">登录</el-button>
          <el-button type="info" @click="resetLoginForm" class="reset-btn">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="decoration-circle circle-1"></div>
    <div class="decoration-circle circle-2"></div>
    <div class="decoration-circle circle-3"></div>
  </div>
</template>

<script>
import {login} from "@/api/login";

export default {
  name: "Login",
  data() {
    return {
      loginForm: {
        username: 'WoNiu',
        password: '*****'
      },
      loginFormRules: {
        username: [
          {required: true, message: '请输入用户名', trigger: 'blur'},
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'},
        ]
      }
    }
  },
  methods: {
    resetLoginForm() {
      this.$refs.loginFormRef.resetFields();
    },
    login() {
      this.$refs.loginFormRef.validate(valid => {
        if (valid) {
          login(this.loginForm).then(res => {
            if (res.code === 200) {
              this.msgSuccess(res.msg)
              window.localStorage.setItem('adminToken', res.data.token)
              this.$router.push('/home')
            } else {
              this.msgError(res.msg)
            }
          }).catch(() => {
            this.msgError("请求失败")
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login_container {
  box-sizing: unset !important;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #4b67a2 100%);
  position: relative;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.05);
  }
}

.login_box {
  width: 480px;
  height: 520px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  padding: 40px;
  box-sizing: border-box;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translate(-50%, -40%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

.login_box .avatar_box {
  height: 110px;
  width: 110px;
  border: 4px solid #667eea;
  border-radius: 50%;
  padding: 5px;
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
  position: absolute;
  left: 50%;
  top: -55px;
  transform: translateX(-50%);
  background-color: #fff;
  transition: all 0.3s ease;
}

.login_box .avatar_box:hover {
  transform: translateX(-50%) scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
}

.login_box .avatar_box img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #f5f5f5;
  object-fit: cover;
}

.login_title {
  text-align: center;
  margin-top: 70px;
  margin-bottom: 30px;
}

.login_title h2 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.login_title p {
  font-size: 14px;
  color: #999;
  margin: 0;
  letter-spacing: 1px;
}

.login_form {
  position: relative;
  width: 100%;
}

.login_form .el-form-item {
  margin-bottom: 24px;
}

.login_form >>> .el-input__inner {
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  padding-left: 45px;
}

.login_form >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login_form >>> .el-input__prefix {
  left: 12px;
  font-size: 18px;
  color: #999;
}

.btns {
  margin-top: 35px;
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.btns >>> .el-button {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  color: white !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4) !important;
}

.reset-btn {
  background: #f5f5f5 !important;
  border: 2px solid #e8e8e8 !important;
  color: #666 !important;
}

.reset-btn:hover {
  background: #fff !important;
  border-color: #667eea !important;
  color: #667eea !important;
}
</style>