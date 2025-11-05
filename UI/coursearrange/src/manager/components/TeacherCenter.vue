<template>
  <div class="teacher-center">
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
          <el-avatar :size="120" :src="teacherInfo.avatar || defaultAvatar" class="user-avatar">
            <i class="el-icon-user-solid"></i>
          </el-avatar>
          <el-button size="small" type="primary" plain @click="handleEditProfile" class="edit-btn">
            <i class="el-icon-edit"></i> 编辑资料
          </el-button>
        </div>
        
        <div class="user-info">
          <h2 class="username">{{ teacherInfo.username || '未设置昵称' }}</h2>
          <p class="user-description">{{ teacherInfo.description || '这位老师很懒，什么都没留下~' }}</p>
          <div class="user-meta">
            <el-tag type="success" effect="plain">
              <i class="el-icon-check"></i> 账号正常
            </el-tag>
            <el-tag v-if="teacherInfo.teach" type="primary" effect="plain">
              <i class="el-icon-notebook-1"></i> 教授科目: {{ teacherInfo.teach }}
            </el-tag>
            <span class="join-time">
              <i class="el-icon-time"></i>
              加入时间: {{ formatDate(teacherInfo.createTime) }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 教学统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card stat-students">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">名下学生</p>
              <h3 class="stat-value">{{ studentCount }}</h3>
              <p class="stat-unit">人</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card stat-priority">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-star-off"></i>
            </div>
            <div class="stat-info">
              <p class="stat-label">优先级</p>
              <h3 class="stat-value">{{ teacherInfo.piority || 0 }}</h3>
              <p class="stat-unit">级别</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="hover" class="actions-card">
      <div slot="header" class="card-header">
        <span><i class="el-icon-setting"></i> 快捷操作</span>
      </div>
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-date" @click="handleViewSchedule">
          查看课表
        </el-button>
        <el-button type="primary" icon="el-icon-s-custom" @click="handleViewStudents">
          我的学生
        </el-button>
        <el-button type="success" icon="el-icon-unlock" @click="handleChangePassword">
          修改密码
        </el-button>
        <el-button type="warning" icon="el-icon-edit" @click="handleEditProfile">
          编辑资料
        </el-button>
      </div>
    </el-card>

    <!-- 其他信息 -->
    <el-card shadow="hover" class="info-card" v-if="teacherInfo.address">
      <div slot="header" class="card-header">
        <span><i class="el-icon-location"></i> 其他信息</span>
      </div>
      <div class="info-content">
        <div v-if="teacherInfo.address" class="info-item">
          <label>地址：</label>
          <p>{{ teacherInfo.address }}</p>
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
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="avatar-upload-tip">点击上传头像，支持 jpg/png 格式，不超过2MB</div>
          </div>
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="用户名" disabled></el-input>
        </el-form-item>

        <el-form-item label="教授科目">
          <el-input v-model="editForm.teach" placeholder="请输入教授科目"></el-input>
        </el-form-item>

        <el-form-item label="地址">
          <el-input v-model="editForm.address" placeholder="请输入地址"></el-input>
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
  name: 'TeacherCenter',
  data() {
    return {
      teacherInfo: {},
      studentCount: 0,
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      editDialogVisible: false,
      saveLoading: false,
      editForm: {
        id: null,
        username: '',
        avatar: '',
        teach: '',
        address: '',
        description: ''
      },
      uploadUrl: process.env.API_BASE_URL + '/upload/avatar',
      uploadHeaders: {
        'Authorization': localStorage.getItem('token') || ''
      },
      editRules: {
        username: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadTeacherInfo();
    this.loadStudentCount();
  },
  methods: {
    // 加载教师信息
    loadTeacherInfo() {
      const teacherStr = window.localStorage.getItem('teacher');
      if (teacherStr) {
        const teacher = JSON.parse(teacherStr);
        this.teacherInfo = teacher;
        
        // 如果有ID，从服务器获取最新数据
        if (teacher.id) {
          this.fetchTeacherFromServer(teacher.id);
        }
      }
    },
    
    // 从服务器获取最新的教师信息
    fetchTeacherFromServer(id) {
      const config = {
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      };
      this.$axios.get(`/teacher/${id}`, config)
        .then(res => {
          if (res.data.code === 0 && res.data.data) {
            this.teacherInfo = res.data.data;
            // 更新localStorage
            window.localStorage.setItem('teacher', JSON.stringify(res.data.data));
          }
        })
        .catch(err => {
          console.error('获取教师信息失败:', err);
        });
    },

    // 加载名下学生数量
    loadStudentCount() {
      if (!this.teacherInfo.id) return;
      
      this.$axios.get(`/teacher/students/${this.teacherInfo.id}/1`, {
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        },
        params: { limit: 1 }
      })
        .then(res => {
          if (res.data.code === 0 && res.data.data) {
            this.studentCount = res.data.data.total || 0;
          }
        })
        .catch(err => {
          console.error('获取学生数量失败:', err);
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
        id: this.teacherInfo.id,
        username: this.teacherInfo.username,
        avatar: this.teacherInfo.avatar,
        teach: this.teacherInfo.teach,
        address: this.teacherInfo.address,
        description: this.teacherInfo.description
      };
      this.editDialogVisible = true;
    },
    
    // 保存资料
    handleSaveProfile() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.saveLoading = true;
          // 添加token到请求头
          const config = {
            headers: {
              'Authorization': localStorage.getItem('token') || ''
            }
          };
          this.$axios.post('/teacher/modify', this.editForm, config)
            .then(res => {
              if (res.data.code === 0) {
                this.$message.success('保存成功！');
                this.editDialogVisible = false;
                // 重新加载教师信息
                this.fetchTeacherFromServer(this.teacherInfo.id);
              } else {
                this.$message.error(res.data.message || '保存失败');
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
      this.$router.push('/coursetable');
    },

    // 查看我的学生
    handleViewStudents() {
      this.$router.push('/studentlist');
    },
    
    // 修改密码
    handleChangePassword() {
      this.$router.push('/updatepass');
    },
    
    // 头像上传前验证
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
        return false;
      }
      return true;
    },

    // 头像上传成功
    handleAvatarSuccess(res, file) {
      if (res.code === 0) {
        this.editForm.avatar = res.data.url;
        this.$message.success('头像上传成功');
      } else {
        this.$message.error(res.message || '头像上传失败');
      }
    }
  }
}
</script>

<style lang="less" scoped>
.teacher-center {
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
      flex-wrap: wrap;
      
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

.stat-students {
  .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }
  
  .stat-value {
    color: #667eea;
  }
}

.stat-priority {
  .stat-icon {
    background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
    color: #ffffff;
  }
  
  .stat-value {
    color: #f59e0b;
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

// 信息卡片
.info-card {
  border-radius: 16px;
  margin-bottom: 24px;
  
  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    
    i {
      color: #6366f1;
      margin-right: 8px;
    }
  }
  
  .info-content {
    .info-item {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      label {
        display: inline-block;
        font-weight: 600;
        color: #4b5563;
        margin-right: 8px;
      }
      
      p {
        display: inline;
        margin: 0;
        color: #6b7280;
        line-height: 1.6;
      }
    }
  }
}

// 对话框样式
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  
  .avatar-uploader {
    /deep/ .el-upload {
      border: 2px dashed #d9d9d9;
      border-radius: 12px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s ease;
      width: 120px;
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fafafa;
      
      &:hover {
        border-color: #667eea;
        background: #f0f0ff;
      }
    }
    
    .avatar-preview {
      width: 120px;
      height: 120px;
      display: block;
      object-fit: cover;
      border-radius: 10px;
    }
    
    .avatar-uploader-icon {
      font-size: 32px;
      color: #8c939d;
      transition: color 0.3s ease;
      
      &:hover {
        color: #667eea;
      }
    }
  }
  
  .avatar-upload-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }
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

