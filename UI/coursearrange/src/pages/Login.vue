<template>
  <div class="login-wrapper">
    
    <div class="login-box">
      <!-- 头像 -->
      <div class="login-avatar">
        <img src="@/assets/logo.png" alt />
      </div>

      <!-- 登录表单 -->
      <el-form class="login-form" ref="loginFormRef" :model="studentLoginForm" :rules="studentLoginFormRules">
        <h3>学生登录</h3>
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="studentLoginForm.username" placeholder="请输入学号" prefix-icon="iconfont iconicon"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="studentLoginForm.password" placeholder="请输入密码" @keyup.enter.native="login" prefix-icon="iconfont iconmima" type="password"></el-input>
        </el-form-item>        
        <!-- 按钮 -->
        <el-form-item class="button">
          <el-button type="primary" @click="login">登录</el-button>
          <el-button type="info" @click="registerNo">注册账号</el-button>
        </el-form-item>
      </el-form>
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
      window.location.href="http://localhost:8081/#/student/register"
    },
    
    login() {
      // 表单预验证
      this.$refs.loginFormRef.validate(valid => {
        if (!valid) return;
        this.$axios.post('/student/login', {
        username: this.studentLoginForm.username,
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
  .login-wrapper {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    
    /* 添加动态背景装饰 */
    &::before {
      content: '';
      position: absolute;
      width: 500px;
      height: 500px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      top: -200px;
      left: -200px;
      animation: float 6s ease-in-out infinite;
    }
    
    &::after {
      content: '';
      position: absolute;
      width: 400px;
      height: 400px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      bottom: -150px;
      right: -150px;
      animation: float 8s ease-in-out infinite reverse;
    }
  }

  @keyframes float {
    0%, 100% {
      transform: translateY(0px);
    }
    50% {
      transform: translateY(20px);
    }
  }

  .login-box {
    width: 450px;
    background-color: #ffffff;
    border-radius: 24px;
    position: relative;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    padding: 80px 40px 40px;
    animation: slideUp 0.5s ease-out;
    z-index: 1;
    backdrop-filter: blur(10px);
  }

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .login-avatar {
    height: 100px;
    width: 100px;
    border: 4px solid #ffffff;
    border-radius: 50%;
    padding: 8px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    position: absolute;
    left: 50%;
    top: 0;
    transform: translate(-50%, -50%);
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translate(-50%, -50%) scale(1.05);
      box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
    }
    
    img {
      height: 100%;
      width: 100%;
      border-radius: 50%;
      background-color: transparent;
      object-fit: cover;
    }
  }

  .login-form {
    width: 100%;
    
    h3 {
      text-align: center;
      color: #1f2937;
      font-size: 28px;
      font-weight: 700;
      margin-bottom: 32px;
      letter-spacing: -0.5px;
    }
    
    /deep/ .el-form-item {
      margin-bottom: 24px;
    }
    
    /deep/ .el-input__inner {
      height: 48px;
      line-height: 48px;
      font-size: 15px;
      padding-left: 45px;
    }
    
    /deep/ .el-input__prefix {
      left: 15px;
      font-size: 18px;
      color: #6366f1;
    }
  }

  .button {
    display: flex;
    gap: 12px;
    margin-top: 32px;
    
    .el-button {
      flex: 1;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      transition: all 0.3s ease;
    }
    
    .el-button--primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
      }
    }
    
    .el-button--info {
      background: #f3f4f6;
      color: #6b7280;
      border: none;
      
      &:hover {
        background: #e5e7eb;
        color: #374151;
      }
    }
  }
</style>