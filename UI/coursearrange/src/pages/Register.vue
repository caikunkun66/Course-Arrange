<template>
  <div class="register-wrapper">
    <!-- 注册表单 -->
    <div class="register-box">
      <!-- 图标 -->
      <div class="register-avatar">
        <img src="@/assets/logo.png" />
      </div>

      <el-form class="register-form" ref="regFormRef" :model="studentRegForm" :rules="studentRegRules">
        <h3>学生注册</h3>
        <!-- 用户名/昵称 -->        
        <el-form-item prop="username">
        <el-input placeholder="请输入昵称（用于登录）"  v-model="studentRegForm.username" clearable></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
        <el-input  placeholder="请输入密码" v-model.trim="studentRegForm.password" show-password></el-input>
        </el-form-item>
        <el-form-item prop="password2">
        <el-input placeholder="确认密码" v-model.trim="studentRegForm.password2" show-password autocomplete="off"></el-input>
        </el-form-item>
        <!-- 按钮 -->
        <el-form-item class="button">
          <el-button type="primary" @click="register">注册</el-button>
          <el-button type="info" @click="hasNo">已有帐号</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    var validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else {
        if (this.studentRegForm.password !== '') {
          this.$refs.regFormRef.validateField('password2');
        }
        callback();
      }
    };
    var validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.studentRegForm.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };

    return {
      studentRegForm: {
        username: '',
        password: '',
        password2: ''
      },
  
      studentRegRules: {
        username: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validatePass, trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        password2: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },

  methods: {

    hasNo() {
      // 已经有账号，跳转到登录界面
      window.location.href="http://localhost:8081/#/student/login"
    },

    // 注册按钮方法
    register() {
      // 进行表单预验证
      this.$refs.regFormRef.validate(valid => {
        if (!valid) return;
        // 进行注册， 携带填写的注册信息给后台
        this.$axios.post('/student/register', {
          username: this.studentRegForm.username,
          password: this.studentRegForm.password
        })
        .then(res => {
          // 注册成功
          if (res.data.code == 0) {
            this.$message.success('注册成功，请用昵称登录系统')
            this.$router.push('/student/login')
          } else {
            this.$message.error(res.data.message || '注册失败')
          }
        })
        .catch(error => {
          this.$message.error('注册失败，请重试')
        })
      })
    },


  }
}
</script>

<style lang="less" scoped>
  .register-wrapper {
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

  .register-box {
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

  .register-avatar {
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

  .register-form {
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
      margin-bottom: 20px;
    }
    
    /deep/ .el-input__inner {
      height: 48px;
      line-height: 48px;
      font-size: 15px;
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