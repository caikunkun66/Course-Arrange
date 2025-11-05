<template>
  <!-- 学生端主界面 -->
  <!-- 学生点击修改密码，跳转到updatepass.vue与其他用户共用一个组件 -->
  <!-- 学生登录之后默认进入查看课表页面 -->
  <!-- 点击学生右上角的个人中心跳转到一个页面展示个人信息，上方放一个按钮加入班级，一个修改信息 -->
  <div class="wrapper">
    <el-container>
      <el-header>
        <!-- 头 -->
        <el-header style="text-align: right; font-size: 12px">
          <!-- 系统标题 -->
          <el-dropdown @command="handleCommand">
            <i class="el-icon-setting" style="margin-right: 15px"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="center">个人中心</el-dropdown-item>
              <el-dropdown-item command="exit">退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>您好，
          <span>{{name}}</span>
        </el-header>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <!-- 侧边 -->
          <!-- 默认展开的索引default-active -->
          <el-menu :default-active="default_active" @select="handleSelect" unique-opened>
            <el-menu-item index="/courseList">
              <template slot="title">
                <i class="el-icon-date"></i>
                <span slot="title">课程表</span>
              </template>
            </el-menu-item>
             <el-menu-item index="/center">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span slot="title">个人中心</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/password">
              <template slot="title">
                <i class="el-icon-unlock"></i>
                <span slot="title">修改密码</span>
              </template>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <!-- Main区域，数据显示 -->
          <router-view></router-view>
        </el-main>
      </el-container>
      <!-- 显示系统时间 -->
      <el-footer>{{time}}</el-footer>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "ManagerMain",
  data() {
    return {
      time: "",
      default_active: "0",
      name: "用户名"
    };
  },
  computed: {},
  mounted() {
    setInterval(() => {
      this.getTime();
    }, 1000);

    let student = window.localStorage.getItem("student")
    if (student != null) {
      this.name = JSON.parse(student).username
    }

    // 初始化菜单选中状态
    this.default_active = this.$route.path;
  },
  watch: {
    // 监听路由变化，更新菜单选中状态
    '$route'(to) {
      this.default_active = to.path;
    }
  },

  methods: {
    handleCommand(command) {
      // alert(command)
      if (command == "exit") {
        localStorage.removeItem("token");
        localStorage.removeItem("student");
        // 判断，返回指定页面
        this.$router.push("/student/login");
      } else if (command == "center") {
        // 跳转到个人中心
        this.$router.push("/center");
      }
    },

    // 获取系统时间
    getTime() {
      this.time = new Date().toLocaleString();
    },

    // 展开一个菜单
    handleSelect(val) {
      this.default_active = val;
      this.$router.push(val);
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  height: 100%;
  width: 100%;
  background: #f9fafb;
  
  .a {
    text-decoration: none !important;
    color: #6366f1 !important;
    transition: all 0.3s ease;
    
    &:hover {
      color: #4f46e5 !important;
    }
  }
  
  .links {
    text-decoration: none !important;
    color: #6366f1 !important;
    transition: all 0.3s ease;
    
    &:hover {
      color: #4f46e5 !important;
    }
  }
}

.el-container {
  height: 100%;
  padding: 0;
  margin: 0;
  width: 100%;
}

.el-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  text-align: right;
  line-height: 60px;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 100;
  
  /deep/ .el-dropdown {
    color: #ffffff;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      opacity: 0.85;
    }
  }
  
  /deep/ .el-icon-setting {
    font-size: 20px;
    color: #ffffff;
    transition: transform 0.3s ease;
    
    &:hover {
      transform: rotate(45deg);
    }
  }
  
  span {
    font-weight: 600;
    font-size: 15px;
    margin-left: 8px;
  }
}

.el-aside {
  background: linear-gradient(180deg, #ffffff 0%, #f9fafb 100%);
  color: #333;
  text-align: left;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  overflow-x: hidden;
  
  /deep/ .el-menu {
    background: transparent;
    border: none;
    padding: 16px 8px;
    
    .el-menu-item {
      height: 52px;
      line-height: 52px;
      margin: 6px 0;
      border-radius: 12px;
      transition: all 0.3s ease;
      color: #4b5563;
      font-weight: 500;
      padding-left: 20px !important;
      
      i {
        color: #6366f1;
        font-size: 20px;
        transition: all 0.3s ease;
        margin-right: 12px;
      }
      
      span {
        font-size: 15px;
      }
      
      &:hover {
        background: rgba(99, 102, 241, 0.1);
        color: #6366f1;
        transform: translateX(4px);
        
        i {
          transform: scale(1.1);
        }
      }
      
      &.is-active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        
        i {
          color: #ffffff;
        }
      }
    }
  }
}

.el-main {
  background: #f9fafb;
  color: #1f2937;
  padding: 24px;
  overflow-y: auto;
}

.el-footer {
  background: #ffffff;
  color: #6b7280;
  text-align: center;
  line-height: 50px;
  font-size: 13px;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 滚动条样式 */
.el-aside::-webkit-scrollbar {
  width: 6px;
}

.el-aside::-webkit-scrollbar-track {
  background: transparent;
}

.el-aside::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
  
  &:hover {
    background: #9ca3af;
  }
}

/* 页面加载动画 */
.el-main > * {
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>