<template>
  <div class="course-manage">
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-notebook-2"></i>
        课程管理
      </h2>
      <el-button type="primary" icon="el-icon-plus" @click="openCreate">添加课程</el-button>
    </div>

    <el-table :data="courseList" v-loading="loading" border>
      <el-table-column label="图片" width="90">
        <template slot-scope="scope">
          <img v-if="scope.row.imageUrl" :src="scope.row.imageUrl" class="course-thumb" alt="" />
          <span v-else class="no-image">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="课程名称"></el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" style="color: #f56c6c;" @click="removeCourse(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isEdit ? '编辑课程' : '添加课程'" :visible.sync="dialogVisible" width="480px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="courseForm" label-width="90px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="请输入课程名称"></el-input>
        </el-form-item>
        <el-form-item label="课程图片">
          <el-upload
            class="course-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="handleImageSuccess"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="course-preview" alt="" />
            <i v-else class="el-icon-plus uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">支持 jpg、png，不超过 2MB</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCourse">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseManage',
  data() {
    return {
      courseList: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      form: {
        id: null,
        name: '',
        imageUrl: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入课程名称', trigger: 'blur' },
          { max: 50, message: '课程名称不能超过50个字', trigger: 'blur' }
        ]
      },
      uploadUrl: (process.env.API_BASE_URL || '') + '/upload/avatar',
      uploadHeaders: {
        Authorization: localStorage.getItem('token') || ''
      }
    };
  },
  mounted() {
    this.loadCourses();
  },
  methods: {
    loadCourses() {
      this.loading = true;
      this.$axios.get('/course/all')
        .then(res => {
          if (res.data.code === 0) {
            this.courseList = res.data.data || [];
          } else {
            this.$message.error(res.data.message || '加载课程失败');
          }
        })
        .catch(() => {
          this.$message.error('加载课程失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    openCreate() {
      this.isEdit = false;
      this.form = { id: null, name: '', imageUrl: '' };
      this.dialogVisible = true;
    },
    openEdit(course) {
      this.isEdit = true;
      this.form = {
        id: course.id,
        name: course.name,
        imageUrl: course.imageUrl || ''
      };
      this.dialogVisible = true;
    },
    resetForm() {
      if (this.$refs.courseForm) {
        this.$refs.courseForm.resetFields();
      }
    },
    beforeUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImage) {
        this.$message.error('只支持 JPG 或 PNG 格式');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('图片不能超过 2MB');
        return false;
      }
      return true;
    },
    handleImageSuccess(res) {
      if (res && res.code === 0 && res.data && res.data.url) {
        this.form.imageUrl = res.data.url;
        this.$message.success('图片已上传');
      } else {
        this.$message.error((res && res.message) || '图片上传失败');
      }
    },
    saveCourse() {
      this.$refs.courseForm.validate(valid => {
        if (!valid) return;
        this.saving = true;
        const payload = {
          id: this.form.id,
          name: this.form.name,
          imageUrl: this.form.imageUrl
        };
        const request = this.isEdit
          ? this.$axios.post('/course/update', payload)
          : this.$axios.post('/course', payload);
        request.then(res => {
          if (res.data.code === 0) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功');
            this.dialogVisible = false;
            this.loadCourses();
          } else {
            this.$message.error(res.data.message || '保存失败');
          }
        }).catch(() => {
          this.$message.error('保存失败');
        }).finally(() => {
          this.saving = false;
        });
      });
    },
    removeCourse(course) {
      this.$confirm(`确定删除课程「${course.name}」吗？已排的课仍会保留课程名称。`, '提示', {
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/course/${course.id}`).then(res => {
          if (res.data.code === 0) {
            this.$message.success('删除成功');
            this.loadCourses();
          } else {
            this.$message.error(res.data.message || '删除失败');
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    }
  }
};
</script>

<style lang="less" scoped>
.course-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .course-manage {
    padding: 8px 0;
  }
}

.page-title {
  margin: 0;
  font-size: 20px;
}

.course-thumb,
.course-preview {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #909399;
}

.course-uploader {
  /deep/ .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    width: 96px;
    height: 96px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.uploader-icon {
  font-size: 24px;
  color: #8c939d;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
