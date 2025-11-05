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
            <el-tag v-if="studentInfo.teacherId" type="primary" effect="plain">
              <i class="el-icon-user"></i> 所属教师: {{ teacherName || '加载中...' }}
            </el-tag>
            <el-tag v-else type="info" effect="plain">
              <i class="el-icon-warning"></i> 未认领教师
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
        <el-button v-if="!studentInfo.teacherId" type="danger" icon="el-icon-user-solid" @click="handleClaimTeacher">
          认领教师
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

    <!-- 认领教师对话框 -->
    <el-dialog 
      title="认领教师" 
      :visible.sync="claimDialogVisible"
      width="600px"
      @open="loadTeacherList"
    >
      <div class="claim-teacher-content">
        <el-alert
          title="温馨提示"
          type="info"
          description="请选择您的任课教师，认领后将无法修改，请谨慎选择！"
          :closable="false"
          style="margin-bottom: 20px;">
        </el-alert>
        
        <el-form :model="claimForm" :rules="claimRules" ref="claimForm" label-width="100px">
          <el-form-item label="选择教师" prop="teacherId">
            <el-select 
              v-model="claimForm.teacherId" 
              placeholder="请选择教师"
              filterable
              style="width: 100%"
              :loading="teacherListLoading"
            >
              <el-option
                v-for="teacher in teacherList"
                :key="teacher.id"
                :label="teacher.username"
                :value="teacher.id"
              >
                <span style="float: left">{{ teacher.username }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  ID: {{ teacher.id }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <div v-if="selectedTeacher" class="teacher-info-preview">
          <h4>教师信息预览</h4>
          <div class="teacher-detail">
            <el-avatar :size="60" :src="selectedTeacher.avatar || defaultAvatar">
              <i class="el-icon-user-solid"></i>
            </el-avatar>
            <div class="teacher-detail-info">
              <p><strong>姓名：</strong>{{ selectedTeacher.username }}</p>
              <p v-if="selectedTeacher.description"><strong>简介：</strong>{{ selectedTeacher.description }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="claimDialogVisible = false">取 消</el-button>
        <el-button type="danger" @click="handleConfirmClaim" :loading="claimLoading">
          确认认领
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
      teacherName: '',
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      editDialogVisible: false,
      saveLoading: false,
      editForm: {
        id: null,
        username: '',
        avatar: '',
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
      },
      // 认领教师相关
      claimDialogVisible: false,
      claimLoading: false,
      teacherListLoading: false,
      teacherList: [],
      claimForm: {
        teacherId: null
      },
      claimRules: {
        teacherId: [
          { required: true, message: '请选择教师', trigger: 'change' }
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
    },
    // 获取选中的教师信息
    selectedTeacher() {
      if (!this.claimForm.teacherId) return null;
      return this.teacherList.find(t => t.id === this.claimForm.teacherId);
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
      // 添加token到请求头
      const config = {
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      };
      this.$axios.get(`/student/${id}`, config)
        .then(res => {
          if (res.data.code === 0 && res.data.data) {
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
          // 添加token到请求头
          const config = {
            headers: {
              'Authorization': localStorage.getItem('token') || ''
            }
          };
          this.$axios.post('/student/modify', this.editForm, config)
            .then(res => {
              if (res.data.code === 0) {
                this.$message.success('保存成功！');
                this.editDialogVisible = false;
                // 重新加载学生信息
                this.fetchStudentFromServer(this.studentInfo.id);
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
      this.$router.push('/courseList');
    },
    
    // 修改密码
    handleChangePassword() {
      this.$router.push('/password');
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
    },

    // 获取教师名称
    fetchTeacherName(teacherId) {
      if (!teacherId) return;
      const config = {
        headers: {
          'Authorization': localStorage.getItem('token') || ''
        }
      };
      this.$axios.get(`/teacher/${teacherId}`, config)
        .then(res => {
          if (res.data.code === 0 && res.data.data) {
            this.teacherName = res.data.data.username;
          }
        })
        .catch(err => {
          console.error('获取教师信息失败:', err);
        });
    },

    // 打开认领教师对话框
    handleClaimTeacher() {
      this.claimDialogVisible = true;
    },

    // 加载教师列表
    loadTeacherList() {
      this.teacherListLoading = true;
      this.$axios.get('/teacher/all')
        .then(res => {
          if (res.data.code === 0) {
            this.teacherList = res.data.data || [];
          } else {
            this.$message.error('获取教师列表失败');
          }
        })
        .catch(err => {
          this.$message.error('获取教师列表失败：' + err.message);
        })
        .finally(() => {
          this.teacherListLoading = false;
        });
    },

    // 确认认领教师
    handleConfirmClaim() {
      this.$refs.claimForm.validate(valid => {
        if (valid) {
          this.$confirm('认领教师后将无法修改，确认认领该教师吗？', '确认认领', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.claimLoading = true;
            this.$axios.post(`/student/claim-teacher/${this.studentInfo.id}`, null, {
              headers: {
                'Authorization': localStorage.getItem('token') || ''
              },
              params: {
                teacherId: this.claimForm.teacherId
              }
            })
              .then(res => {
                if (res.data.code === 0) {
                  this.$message.success('认领成功！');
                  this.claimDialogVisible = false;
                  // 重新加载学生信息
                  this.fetchStudentFromServer(this.studentInfo.id);
                  // 重置表单
                  this.claimForm.teacherId = null;
                } else {
                  this.$message.error(res.data.message || '认领失败');
                }
              })
              .catch(err => {
                this.$message.error('认领失败：' + err.message);
              })
              .finally(() => {
                this.claimLoading = false;
              });
          }).catch(() => {
            this.$message.info('已取消认领');
          });
        }
      });
    }
  },
  watch: {
    // 监听学生信息变化，获取教师名称
    'studentInfo.teacherId': {
      handler(newVal) {
        if (newVal) {
          this.fetchTeacherName(newVal);
        }
      },
      immediate: true
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

// 认领教师对话框样式
.claim-teacher-content {
  .teacher-info-preview {
    margin-top: 24px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 8px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 15px;
      color: #1f2937;
    }
    
    .teacher-detail {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .teacher-detail-info {
        flex: 1;
        
        p {
          margin: 0 0 8px 0;
          font-size: 14px;
          color: #4b5563;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          strong {
            color: #1f2937;
          }
        }
      }
    }
  }
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