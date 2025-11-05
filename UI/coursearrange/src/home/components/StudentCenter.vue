<template>
  <div class="student-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-user"></i>
        个人中心
      </h2>
    </div>

    <!-- 个人信息卡片 -->
    <el-card class="profile-card" shadow="hover">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="120" :src="studentInfo.avatar || defaultAvatar" class="user-avatar">
            <i class="el-icon-user-solid"></i>
          </el-avatar>
          <el-button size="small" type="primary" plain @click="handleEditProfile" class="edit-btn">
            <i class="el-icon-edit"></i> 编辑资料
          </el-button>
        </div>
        
        <div class="user-info">
          <h2 class="username">{{ studentInfo.username || '未设置昵称' }}</h2>
          <p class="user-description">{{ studentInfo.description || '这个人很懒，什么都没留下~' }}</p>
          <div class="user-meta">
            <el-tag type="success" effect="plain">
              <i class="el-icon-check"></i> 账号正常
            </el-tag>
            <span class="join-time">
              <i class="el-icon-time"></i>
              加入时间: {{ formatDate(studentInfo.createTime) }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学习统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-notebook-2"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">总课时</p>
              <h3 class="stat-value">{{ studentInfo.totalHours || 0 }}</h3>
              <p class="stat-unit">课时</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-completed">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">已完成</p>
              <h3 class="stat-value">{{ studentInfo.completedHours || 0 }}</h3>
              <p class="stat-unit">课时</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-remaining">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-timer"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">剩余课时</p>
              <h3 class="stat-value">{{ remainingHours }}</h3>
              <p class="stat-unit">课时</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学习进度 -->
    <el-card shadow="hover" class="progress-card" v-if="studentInfo.totalHours > 0">
      <div slot="header" class="card-header">
        <span><i class="el-icon-data-line"></i> 学习进度</span>
      </div>
      <div class="progress-content">
        <el-progress 
          :percentage="progressPercentage" 
          :color="progressColor"
          :stroke-width="20"
          :text-inside="true"
        ></el-progress>
        <p class="progress-text">
          已完成 {{ studentInfo.completedHours || 0 }} / {{ studentInfo.totalHours || 0 }} 课时
        </p>
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <el-card shadow="hover" class="actions-card">
      <div slot="header" class="card-header">
        <span><i class="el-icon-setting"></i> 快捷操作</span>
      </div>
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-s-marketing" @click="handleViewSchedule">
          查看课表
        </el-button>
        <el-button type="success" icon="el-icon-unlock" @click="handleChangePassword">
          修改密码
        </el-button>
        <el-button type="warning" icon="el-icon-edit" @click="handleEditProfile">
          编辑资料
        </el-button>
      </div>
    </el-card>

    <!-- 备注信息 -->
    <el-card shadow="hover" class="remark-card" v-if="studentInfo.studentRemark || studentInfo.courseRemark">
      <div slot="header" class="card-header">
        <span><i class="el-icon-document"></i> 备注信息</span>
      </div>
      <div class="remark-content">
        <div v-if="studentInfo.courseRemark" class="remark-item">
          <label>课程备注：</label>
          <p>{{ studentInfo.courseRemark }}</p>
        </div>
        <div v-if="studentInfo.studentRemark" class="remark-item">
          <label>学生备注：</label>
          <p>{{ studentInfo.studentRemark }}</p>
        </div>
      </div>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog 
      title="编辑个人资料" 
      :visible.sync="editDialogVisible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="editForm.avatar || defaultAvatar">
              <i class="el-icon-user-solid"></i>
            </el-avatar>
            <el-input 
              v-model="editForm.avatar" 
              placeholder="请输入头像URL"
              style="margin-left: 20px; width: 300px;"
            >
              <template slot="prepend">URL</template>
            </el-input>
          </div>
        </el-form-item>
        
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入昵称"></el-input>
        </el-form-item>
        
        <el-form-item label="个性签名">
          <el-input 
            v-model="editForm.description" 
            type="textarea"
            :rows="3"
            placeholder="写点什么介绍一下自己吧~"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">
          保 存
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StudentCenter',
  data() {
    return {
      studentInfo: {},
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      editDialogVisible: false,
      saveLoading: false,
      editForm: {
        id: null,
        username: '',
        avatar: '',
        description: ''
      },
      editRules: {
        username: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 计算剩余课时
    remainingHours() {
      const total = this.studentInfo.totalHours || 0;
      const completed = this.studentInfo.completedHours || 0;
      return Math.max(0, total - completed);
    },
    // 计算进度百分比
    progressPercentage() {
      const total = this.studentInfo.totalHours || 0;
      const completed = this.studentInfo.completedHours || 0;
      if (total === 0) return 0;
      return Math.round((completed / total) * 100);
    },
    // 进度条颜色
    progressColor() {
      const percentage = this.progressPercentage;
      if (percentage < 30) return '#f56c6c';
      if (percentage < 70) return '#e6a23c';
      return '#67c23a';
    }
  },
  mounted() {
    this.loadStudentInfo();
  },
  methods: {
    // 加载学生信息
    loadStudentInfo() {
      const studentStr = window.localStorage.getItem('student');
      if (studentStr) {
        const student = JSON.parse(studentStr);
        this.studentInfo = student;
        
        // 如果有ID，从服务器获取最新数据
        if (student.id) {
          this.fetchStudentFromServer(student.id);
        }
      }
    },
    
    // 从服务器获取最新的学生信息
    fetchStudentFromServer(id) {
      this.$axios.get(`/student/${id}`)
        .then(res => {
          if (res.data.status === 0 && res.data.data) {
            this.studentInfo = res.data.data;
            // 更新localStorage
            window.localStorage.setItem('student', JSON.stringify(res.data.data));
          }
        })
        .catch(err => {
          console.error('获取学生信息失败:', err);
        });
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '未知';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN');
    },
    
    // 编辑资料
    handleEditProfile() {
      this.editForm = {
        id: this.studentInfo.id,
        username: this.studentInfo.username,
        avatar: this.studentInfo.avatar,
        description: this.studentInfo.description
      };
      this.editDialogVisible = true;
    },
    
    // 保存资料
    handleSaveProfile() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.saveLoading = true;
          this.$axios.post('/student/modify', this.editForm)
            .then(res => {
              if (res.data.status === 0) {
                this.$message.success('保存成功！');
                this.editDialogVisible = false;
                // 重新加载学生信息
                this.fetchStudentFromServer(this.studentInfo.id);
              } else {
                this.$message.error(res.data.msg || '保存失败');
              }
            })
            .catch(err => {
              this.$message.error('保存失败：' + err.message);
            })
            .finally(() => {
              this.saveLoading = false;
            });
        }
      });
    },
    
    // 关闭对话框
    handleDialogClose() {
      this.$refs.editForm.resetFields();
    },
    
    // 查看课表
    handleViewSchedule() {
      this.$router.push('/courseList');
    },
    
    // 修改密码
    handleChangePassword() {
      this.$router.push('/password');
    }
  }
}
</script>

<style lang="less" scoped>
.student-center {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
    
    i {
      color: #6366f1;
      margin-right: 12px;
    }
  }
}

// 个人信息卡片
.profile-card {
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  
  /deep/ .el-card__body {
    padding: 32px;
  }
}

.profile-header {
  display: flex;
  gap: 32px;
  
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    
    .user-avatar {
      border: 4px solid #f3f4f6;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .edit-btn {
      border-radius: 20px;
    }
  }
  
  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .username {
      font-size: 28px;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 12px 0;
    }
    
    .user-description {
      font-size: 15px;
      color: #6b7280;
      margin: 0 0 16px 0;
      line-height: 1.6;
    }
    
    .user-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .join-time {
        color: #9ca3af;
        font-size: 14px;
        
        i {
          margin-right: 6px;
        }
      }
    }
  }
}

// 统计卡片
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  transition: all 0.3s ease;
  border: none;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  }
  
  /deep/ .el-card__body {
    padding: 24px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    flex-shrink: 0;
  }
  
  .stat-info {
    flex: 1;
    
    .stat-label {
      font-size: 14px;
      color: #6b7280;
      margin: 0 0 8px 0;
    }
    
    .stat-value {
      font-size: 32px;
      font-weight: 700;
      margin: 0;
      line-height: 1;
    }
    
    .stat-unit {
      font-size: 13px;
      color: #9ca3af;
      margin: 4px 0 0 0;
    }
  }
}

.stat-total {
  .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }
  
  .stat-value {
    color: #667eea;
  }
}

.stat-completed {
  .stat-icon {
    background: linear-gradient(135deg, #67c23a 0%, #52c41a 100%);
    color: #ffffff;
  }
  
  .stat-value {
    color: #67c23a;
  }
}

.stat-remaining {
  .stat-icon {
    background: linear-gradient(135deg, #e6a23c 0%, #faad14 100%);
    color: #ffffff;
  }
  
  .stat-value {
    color: #e6a23c;
  }
}

// 进度卡片
.progress-card {
  margin-bottom: 24px;
  border-radius: 16px;
  
  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    
    i {
      color: #6366f1;
      margin-right: 8px;
    }
  }
  
  .progress-content {
    padding: 20px 0;
    
    .progress-text {
      text-align: center;
      margin: 16px 0 0 0;
      color: #6b7280;
      font-size: 14px;
    }
  }
}

// 操作卡片
.actions-card {
  margin-bottom: 24px;
  border-radius: 16px;
  
  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    
    i {
      color: #6366f1;
      margin-right: 8px;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    
    /deep/ .el-button {
      border-radius: 8px;
      padding: 12px 24px;
      font-size: 15px;
    }
  }
}

// 备注卡片
.remark-card {
  border-radius: 16px;
  
  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    
    i {
      color: #6366f1;
      margin-right: 8px;
    }
  }
  
  .remark-content {
    .remark-item {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      label {
        display: block;
        font-weight: 600;
        color: #4b5563;
        margin-bottom: 8px;
      }
      
      p {
        margin: 0;
        color: #6b7280;
        line-height: 1.6;
        background: #f9fafb;
        padding: 12px;
        border-radius: 8px;
      }
    }
  }
}

// 对话框样式
.avatar-upload {
  display: flex;
  align-items: center;
}

.dialog-footer {
  text-align: right;
}

// 响应式设计
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-row {
    /deep/ .el-col {
      margin-bottom: 16px;
    }
  }
  
  .action-buttons {
    flex-direction: column;
    
    /deep/ .el-button {
      width: 100%;
    }
  }
}
</style>