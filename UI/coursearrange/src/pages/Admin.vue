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
        <h2 class="login-title">{{ loginTypeTitle }}</h2>
        <p class="login-subtitle">欢迎登录智能排课系统</p>

        <!-- 登录表单 -->
        <el-form class="login-form" ref="loginFormRef" :model="adminLoginForm" :rules="adminLoginFormRules">
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input 
              v-model="adminLoginForm.username" 
              placeholder="请输入账号" 
              prefix-icon="el-icon-user"
              size="large">
            </el-input>
          </el-form-item>
          
          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input 
              v-model="adminLoginForm.password" 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock" 
              type="password" 
              show-password
              size="large"
              @keyup.enter.native="login">
            </el-input>
          </el-form-item>
          
          <!-- 登录类型 -->
          <el-form-item class="login-type-selector">
            <el-radio-group v-model="radio" @change="getType">
              <el-radio label="1" class="type-radio">
                <span class="radio-icon">👨‍💼</span>
                <span>管理员</span>
              </el-radio>
              <el-radio label="2" class="type-radio">
                <span class="radio-icon">👨‍🏫</span>
                <span>教师</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 按钮 -->
          <el-form-item class="button-group">
            <el-button type="primary" @click="login" class="login-btn" size="large">
              <span>登 录</span>
            </el-button>
            <el-button @click="resetLoginForm" class="reset-btn" size="large">
              <span>重 置</span>
            </el-button>
          </el-form-item>

          <!-- 注册入口 -->
          <div class="register-entry">
            <router-link class="register-link" :to="{ path: '/student/register', query: { role: 'teacher' } }">
              <span>还没有账号？</span>
              <span class="link-text">教师注册</span>
            </router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Admin",
  data() {
    return {
      // 类型选择，默认选择管理员登录
      radio: '1',
      // 登录表单的对象
      adminLoginForm: {
        username: '',
        password: ''
      },
      adminLoginFormRules: {
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
  computed: {
    loginTypeTitle() {
      return this.radio === '1' ? '管理员登录' : '教师登录';
    }
  },
  mounted() {
    // 根据 URL 参数设置登录类型
    const type = this.$route.query.type;
    if (type === 'teacher') {
      this.radio = '2';
    } else if (type === 'admin') {
      this.radio = '1';
    }
  },
  methods: {
    // 重置方法
    resetLoginForm() {
      this.$refs.loginFormRef.resetFields();
    },
    getType() {
      // 调用这个方法直接获取到了类型
    },
    
    login() {
      console.log('=== 登录按钮被点击 ===');
      console.log('表单数据:', this.adminLoginForm);
      console.log('登录类型:', this.radio);
      console.log('axios baseURL:', this.$axios.defaults.baseURL);
      
      this.$refs.loginFormRef.validate(valid => {
        console.log('表单验证结果:', valid);
        // 表单预验证
        if (!valid) {
          console.log('表单验证失败，停止登录');
          return;
        }
        
        if (this.radio == 1) {
          // 管理员登录
          const loginUrl = '/admin/login';
          console.log('准备发送管理员登录请求到:', loginUrl);
          
          this.$axios.post(loginUrl, {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            console.log('管理员登录响应:', res);
            if (res.data.code == 0) {
              let ret = res.data.data
              // 保存信息，跳转到主页
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('admin', JSON.stringify(ret.admin))
              console.log('登录成功，准备跳转到 /coursetable');
              this.$router.push('/coursetable')
              this.$message({message: "登录成功", type: "success"})
            } else {
              console.log('登录失败:', res.data.message);
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            console.error('管理员登录请求失败:', error);
            this.$message.error("登录失败: " + error.message)
          });
        } else if(this.radio == 2) {
          // 教师登录
          const loginUrl = '/teacher/login';
          console.log('准备发送教师登录请求到:', loginUrl);
          
          this.$axios.post(loginUrl, {
            username: this.adminLoginForm.username,
            password: this.adminLoginForm.password
          })
          .then(res => {
            console.log('教师登录响应:', res);
            if (res.data.code == 0) {
              let ret = res.data.data
              window.localStorage.setItem('token', ret.token)
              window.localStorage.setItem('teacher', JSON.stringify(ret.teacher))
              console.log('登录成功，准备跳转到 /coursetable');
              // 跳转
              this.$router.push('/coursetable')
              this.$message({message: "登录成功", type: "success"})
            } else {
              console.log('登录失败:', res.data.message);
              alert(res.data.message)
              this.adminLoginForm.password = ''
            }
          }).catch((error) => {
            console.error('教师登录请求失败:', error);
            this.$message.error("登录失败: " + error.message)
          });
        }
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

  .login-type-selector {
    /deep/ .el-radio-group {
      display: flex;
      gap: 12px;
      width: 100%;
    }

    /deep/ .el-radio {
      flex: 1;
      margin: 0;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      padding: 12px 16px;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      background: #ffffff;

      &:hover {
        border-color: #667eea;
        background: rgba(102, 126, 234, 0.03);
      }

      &.is-checked {
        border-color: #667eea;
        background: rgba(102, 126, 234, 0.08);

        .el-radio__label {
          color: #667eea;
        }
      }
    }

    /deep/ .el-radio__input {
      display: none;
    }

    /deep/ .el-radio__label {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 0;
      font-size: 15px;
      font-weight: 500;
      color: #64748b;

      .radio-icon {
        font-size: 20px;
      }
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

    .reset-btn {
      flex: 0.5;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 2px;
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

  .register-entry {
    text-align: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #e2e8f0;
  }

  .register-link {
    color: #64748b;
    text-decoration: none;
    font-size: 14px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: all 0.3s ease;

    .link-text {
      color: #667eea;
      font-weight: 600;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 0;
        height: 2px;
        background: #667eea;
        transition: width 0.3s ease;
      }
    }

    &:hover {
      .link-text::after {
        width: 100%;
      }
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

      .reset-btn {
        flex: 1;
      }
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