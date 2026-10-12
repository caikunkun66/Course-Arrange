<template>
  <div class="login-wrapper">
    <!-- 装饰性背景 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 返回主页按钮 -->
    <router-link to="/" class="back-home">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <span>返回主页</span>
    </router-link>
    
    <div class="login-container">
      <div class="login-box">
        <!-- Logo图标 -->
        <div class="logo-badge">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <!-- 标题 -->
        <h2 class="login-title">学生登录</h2>
        <p class="login-subtitle">欢迎登录智能排课系统</p>

        <!-- 登录表单 -->
        <el-form class="login-form" ref="loginFormRef" :model="studentLoginForm" :rules="studentLoginFormRules">
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input 
              v-model="studentLoginForm.username" 
              placeholder="请输入账号" 
              prefix-icon="el-icon-user"
              size="large">
            </el-input>
          </el-form-item>
          
          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input 
              v-model="studentLoginForm.password" 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock" 
              type="password"
              show-password
              size="large"
              @keyup.enter.native="login">
            </el-input>
          </el-form-item>
          
          <!-- 按钮 -->
          <el-form-item class="button-group">
            <el-button type="primary" @click="login" class="login-btn" size="large">
              <span>登 录</span>
            </el-button>
            <el-button @click="registerNo" class="register-btn" size="large">
              <span>注册账号</span>
            </el-button>
          </el-form-item>

          <!-- 提示信息 -->
          <div class="tip-text">
            <span class="tip-icon">💡</span>
            <span>首次使用？点击注册账号创建学生账户</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      // 登录表单的对象
      studentLoginForm: {
        username: '',
        password: ''
      },
      studentLoginFormRules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 12, message: '长度在 5 到 12 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
        ]
      },
    }
  },
  methods: {
    
    registerNo() {
      // 跳转到注册页面
      this.$router.push('/student/register')
    },
    
    login() {
      // 表单预验证
      this.$refs.loginFormRef.validate(valid => {
        if (!valid) return;
        this.$axios.post('/student/login', {
        account: this.studentLoginForm.username,
        password: this.studentLoginForm.password
        })
        .then((res) => {
          if (res.data.code == 0) {
            // 成功响应,得到token
            let ret = res.data.data
            window.localStorage.setItem('token', ret.token)
            window.localStorage.setItem('student', JSON.stringify(ret.student))
            this.$router.push('/student')
            this.$message({message: "登录成功", type: "success"})
          } else {
            alert(res.data.message)
          }
        }).catch((error) => {
          // 失败
          this.$message.error("登录失败")
        });
      })
    }
  }
};
</script>

<style lang="less" scoped>
  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-20px); }
  }

  @keyframes fadeInScale {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  @keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.05); }
  }

  .login-wrapper {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    overflow: hidden;
  }

  .bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 0;
    overflow: hidden;

    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
    }

    .circle-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
      animation: float 8s ease-in-out infinite;
    }

    .circle-2 {
      width: 300px;
      height: 300px;
      bottom: -80px;
      left: -80px;
      animation: float 10s ease-in-out infinite reverse;
    }

    .circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 10%;
      animation: float 12s ease-in-out infinite;
    }
  }

  .back-home {
    position: absolute;
    top: 24px;
    left: 24px;
    z-index: 10;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 10px;
    color: #ffffff;
    text-decoration: none;
    font-weight: 500;
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;

    svg {
      width: 20px;
      height: 20px;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      border-color: rgba(255, 255, 255, 0.4);
      transform: translateX(-3px);
    }
  }

  .login-container {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 480px;
  }

  .login-box {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 48px 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.5);
    animation: fadeInScale 0.5s ease-out;
  }

  .logo-badge {
    width: 70px;
    height: 70px;
    margin: 0 auto 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    animation: pulse 3s ease-in-out infinite;

    svg {
      width: 40px;
      height: 40px;
      color: #ffffff;
    }
  }

  .login-title {
    text-align: center;
    font-size: 28px;
    font-weight: 700;
    color: #667eea;
    margin: 0 0 8px 0;
  }

  .login-subtitle {
    text-align: center;
    font-size: 14px;
    color: #94a3b8;
    margin: 0 0 32px 0;
  }

  .login-form {
    /deep/ .el-form-item {
      margin-bottom: 24px;
    }

    /deep/ .el-input__inner {
      border-radius: 10px;
      border: 2px solid #e2e8f0;
      padding: 12px 15px 12px 45px;
      height: 48px;
      font-size: 15px;
      transition: all 0.3s ease;

      &:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }

      &::placeholder {
        color: #cbd5e1;
      }
    }

    /deep/ .el-input__prefix {
      left: 15px;
      font-size: 18px;
      color: #94a3b8;
    }

    /deep/ .el-input__suffix {
      right: 15px;
    }
  }

  .button-group {
    margin-bottom: 16px;

    /deep/ .el-form-item__content {
      display: flex;
      gap: 12px;
    }

    .login-btn {
      flex: 1;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: 10px;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 2px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .register-btn {
      flex: 0.8;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      color: #64748b;
      background: #ffffff;
      transition: all 0.3s ease;

      &:hover {
        border-color: #667eea;
        color: #667eea;
        background: rgba(102, 126, 234, 0.05);
      }
    }
  }

  .tip-text {
    text-align: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #e2e8f0;
    color: #64748b;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .tip-icon {
      font-size: 16px;
    }
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .login-wrapper {
      padding: 16px;
    }

    .back-home {
      top: 16px;
      left: 16px;
      padding: 8px 16px;
      font-size: 14px;

      svg {
        width: 18px;
        height: 18px;
      }
    }

    .login-box {
      padding: 36px 24px;
    }

    .logo-badge {
      width: 60px;
      height: 60px;

      svg {
        width: 32px;
        height: 32px;
      }
    }

    .login-title {
      font-size: 24px;
    }

    .login-subtitle {
      font-size: 13px;
    }

    .button-group {
      /deep/ .el-form-item__content {
        flex-direction: column;
      }

      .register-btn {
        flex: 1;
      }
    }

    .tip-text {
      font-size: 12px;
      flex-direction: column;
      gap: 4px;
    }

    .bg-decoration {
      .circle-1 {
        width: 250px;
        height: 250px;
      }

      .circle-2 {
        width: 200px;
        height: 200px;
      }

      .circle-3 {
        display: none;
      }
    }
  }
</style>