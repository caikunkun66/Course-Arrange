<template>
  <!-- 后台管理系统主界面 -->
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
              <el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>
              <el-dropdown-item command="exit">退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>您好，
          <span>{{name}}</span>
          <el-avatar :size="36" :src="userAvatar" class="user-avatar-header">
            <i class="el-icon-user-solid"></i>
          </el-avatar>
        </el-header>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <!-- 侧边 -->
          <!-- 默认展开的索引default-active -->
          <el-menu :default-active="default_active" @select="handleSelect" unique-opened router>
            <el-menu-item index="/coursetable">
              <i class="el-icon-date"></i>
              <span slot="title">我的课表</span>
            </el-menu-item>

            <el-menu-item index="/studentlist">
              <i class="el-icon-s-custom"></i>
              <span slot="title">学生管理</span>
            </el-menu-item>

            <el-menu-item v-if="isTeacher" index="/teachercenter">
              <i class="el-icon-user"></i>
              <span slot="title">个人中心</span>
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
      default_active: "/coursetable",
      name: '用户名',
      userAvatar: '',
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    };
  },
  computed: {
    isTeacher:()=>{
      return window.localStorage.getItem('teacher') != null;
    }
  },
  mounted() {
    setInterval(() => {
      this.getTime();
    }, 1000);
    
    // 根据当前路由设置菜单选中状态
    this.default_active = this.$route.path;
    
    let admin = window.localStorage.getItem('admin')
    if(admin != null){
      const adminData = JSON.parse(admin)
      this.name = adminData.realname
      this.userAvatar = adminData.avatar || this.defaultAvatar
    } else {
      let teacher = window.localStorage.getItem('teacher')
      if (teacher != null) {
        const teacherData = JSON.parse(teacher)
        this.name = teacherData.username
        this.userAvatar = teacherData.avatar || this.defaultAvatar
      }
    }
  },
  watch: {
    // 监听路由变化，更新菜单选中状态
    '$route'(to) {
      this.default_active = to.path;
    }
  },

  methods: {
    // 下拉菜单功能，退出、个人中心
    handleCommand(command) {
      // alert(command)
      if (command == 'exit') {
        localStorage.removeItem('token')
        localStorage.removeItem('admin')
        localStorage.removeItem('teacher')
        // 判断，返回指定页面
        this.$router.push('/')
      } else if (command == 'center') {
        // 跳转到个人中心
        if (this.isTeacher) {
          this.$router.push('/teachercenter')
        }
      } else if (command == 'updatePassword') {
        // 修改密码页面
        this.$router.push('/updatepass')
      }
      
    },

    // 获取系统时间
    getTime() {
      this.time = new Date().toLocaleString();
    },

    // 展开一个菜单
    handleSelect(val) {
      this.default_active = val;
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  height: 100%;
  width: 100%;
  background: #f9fafb;
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
  
  .user-avatar-header {
    margin-left: 12px;
    vertical-align: middle;
    border: 2px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.05);
      border-color: rgba(255, 255, 255, 0.6);
    }
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
    
    .el-menu-item,
    .el-submenu__title {
      height: 48px;
      line-height: 48px;
      margin: 4px 0;
      border-radius: 12px;
      transition: all 0.3s ease;
      color: #4b5563;
      font-weight: 500;
      
      i {
        color: #6366f1;
        font-size: 18px;
        transition: all 0.3s ease;
      }
      
      &:hover {
        background: rgba(99, 102, 241, 0.1);
        color: #6366f1;
        
        i {
          transform: scale(1.1);
        }
      }
    }
    
    .el-menu-item.is-active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      
      i {
        color: #ffffff;
      }
    }
    
    .el-submenu {
      .el-menu-item {
        height: 42px;
        line-height: 42px;
        padding-left: 55px !important;
        
        &:hover {
          background: rgba(99, 102, 241, 0.08);
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
