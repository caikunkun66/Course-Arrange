<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-select
        v-model="value1"
        placeholder="选择教师"
        @change="queryStudentByTeacher"
        @clear="clearListener"
        clearable
      >
        <el-option v-for="item in teacherList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-button slot="trigger" type="primary" @click="addStudent()">
        新增学生
      </el-button>
    </div>
    <div>
      <el-table :data="studentData" size="mini" :stripe="true" :highlight-current-row="true">
        <el-table-column prop="id" label="ID" width="60" fixed></el-table-column>
        <el-table-column prop="username" label="昵称" width="120" fixed></el-table-column>
        <el-table-column prop="teacherName" label="所属教师" width="120" fixed></el-table-column>
        <el-table-column label="课时进度" width="280" align="center">
          <template slot-scope="scope">
            <div style="display: flex; align-items: center; width: 100%;">
              <div style="flex: 1; min-width: 0; margin-right: 10px;">
                <el-progress 
                  :percentage="calculateProgress(scope.row.completedHours, scope.row.totalHours)" 
                  :color="getProgressColor(scope.row.completedHours, scope.row.totalHours)"
                  :stroke-width="16"
                  :show-text="false">
                </el-progress>
              </div>
              <span style="white-space: nowrap; font-size: 12px; color: #606266; min-width: 60px; text-align: right;">
                {{ scope.row.completedHours || 0 }}/{{ scope.row.totalHours || 0 }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="courseRemark" label="课程备注" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="studentRemark" label="学生备注" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>

        <el-table-column prop="operation" label="操作" width="150px" fixed="right">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加学生 -->
      <el-dialog title="添加学生" :visible.sync="visible" width="600px">
        <el-form :model="addStudentData" label-position="left" label-width="100px" :rules="formRules" ref="addForm">
          <!-- 选择教师 -->
          <el-form-item label="所属教师" prop="teacherId">
            <el-select v-model="addStudentData.teacherId" placeholder="选择教师" clearable>
              <el-option
                v-for="item in teacherList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="昵称" prop="username">
            <el-input v-model="addStudentData.username" autocomplete="off" placeholder="请输入学生昵称"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="addStudentData.password" type="password" autocomplete="off" placeholder="默认123456"></el-input>
          </el-form-item>
          <el-form-item label="总课时" prop="totalHours">
            <el-input-number v-model="addStudentData.totalHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
          </el-form-item>
          <el-form-item label="已上课时" prop="completedHours">
            <el-input-number v-model="addStudentData.completedHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
          </el-form-item>
          <el-form-item label="剩余课时">
            <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
              {{ (addStudentData.totalHours || 0) - (addStudentData.completedHours || 0) }} 课时
            </span>
          </el-form-item>
          <el-form-item label="课程备注" prop="courseRemark">
            <el-input v-model="addStudentData.courseRemark" type="textarea" :rows="3" placeholder="请输入课程备注（选填）" maxlength="500" show-word-limit></el-input>
          </el-form-item>
          <el-form-item label="学生备注" prop="studentRemark">
            <el-input v-model="addStudentData.studentRemark" type="textarea" :rows="3" placeholder="请输入学生备注（选填）" maxlength="500" show-word-limit></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="visible = false">取 消</el-button>
          <el-button type="primary" @click="commit()">提 交</el-button>
        </div>
      </el-dialog>

      <!-- 上一页，当前页，下一页 -->
      <div class="footer-button">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page.sync="page"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ClassManager",
  data() {
    return {
      studentData: [],
      editFormData: [],
      addStudentData: {
        teacherId: null,
        username: '',
        password: '123456',
        totalHours: 0,
        completedHours: 0,
        courseRemark: '',
        studentRemark: ''
      },
      visible: false,
      page: 1,
      pageSize: 10,
      total: 0,
      value1: '',
      teacherList: [],
      formRules: {
        teacherId: [{ required: true, message: "请选择教师", trigger: "change" }],
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }]
      }
    };
  },
  mounted() {
    this.allStudent();
    this.loadTeachers();
  },
  methods: {
    
    // 计算课时进度百分比
    calculateProgress(completed, total) {
      if (!total || total === 0) return 0
      const percentage = Math.round((completed / total) * 100)
      return Math.min(percentage, 100)
    },

    // 根据进度返回颜色
    getProgressColor(completed, total) {
      const percentage = this.calculateProgress(completed, total)
      if (percentage >= 90) return '#F56C6C' // 红色 - 快用完了
      if (percentage >= 70) return '#E6A23C' // 橙色 - 用了大部分
      if (percentage >= 30) return '#409EFF' // 蓝色 - 正常使用中
      return '#67C23A' // 绿色 - 刚开始
    },
    
    // 加载所有教师
    loadTeachers() {
      this.$axios.get("/teacher/all")
      .then(res => {
        if (res.data.code == 0) {
          let ret = res.data.data
          this.teacherList = ret.map(v => ({
            value: v.id,
            label: v.realname
          }))
        } else {
          this.$message.error(res.data.message)
        }
      })
      .catch(error => {
        this.$message.error("加载教师列表失败")
      })
    },

    // 提交添加学生
    commit() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          this.$axios.post("/student/add", this.addStudentData)
          .then(res => {
            if (res.data.code == 0) {
              this.allStudent()
              this.$message({message: "添加学生成功", type: "success"})
              this.visible = false
              this.resetForm()
            } else {
              this.$message.error(res.data.message)
            }
          })
          .catch(error => {
            this.$message.error("添加学生失败")
          })
        }
      })
    },

    // 重置表单
    resetForm() {
      this.addStudentData = {
        teacherId: null,
        username: '',
        password: '123456',
        totalHours: 0,
        completedHours: 0,
        courseRemark: '',
        studentRemark: ''
      }
      if (this.$refs.addForm) {
        this.$refs.addForm.resetFields()
      }
    },

    addStudent() {
      this.resetForm()
      this.visible = true
    },

    // 丰富学生数据（添加教师名称）
    enrichStudentData(students) {
      this.studentData = students.map(student => {
        if (student.teacherId && this.teacherList.length > 0) {
          const teacher = this.teacherList.find(t => t.value === student.teacherId)
          student.teacherName = teacher ? teacher.label : '未分配'
        } else {
          student.teacherName = '未分配'
        }
        return student
      })
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      if (this.value1) {
        this.queryStudentByTeacher()
      } else {
        this.allStudent()
      }
    },

    // 根据教师查询学生
    queryStudentByTeacher() {
      if (!this.value1) {
        this.allStudent()
        return
      }
      this.$axios
        .get(`/student/teacher/${this.value1}/${this.page}`)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.enrichStudentData(ret.records);
            this.total = ret.total;
          }
        })
        .catch(error => {
          this.$message.error("查询失败")
        });
    },

    // 查询所有学生
    allStudent() {
      this.$axios
        .get("/student/students/" + this.page)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data;
            this.enrichStudentData(ret.records);
            this.total = ret.total;
          }
        })
        .catch(error => {
          this.$message.error("查询失败")
        });
    },

    // 清除筛选
    clearListener() {
      this.value1 = "";
      this.allStudent();
    },

    // 根据id删除学生
    deleteById(index, row) {
      this.$confirm('确定要删除该学生吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete("/student/delete/" + row.id)
        .then(res => {
          if (res.data.code == 0) {
            this.$message({message: "删除成功", type: "success"})
            this.allStudent()
          } else {
            this.$message.error(res.data.message)
          }
        })
        .catch(error => {
          this.$message.error("删除失败")
        })
      }).catch(() => {})
    },

    editById(index, row) {
      // 跳转到编辑页面或打开编辑对话框
      this.$message.info("请在学生列表中编辑学生信息")
    }
  }
};
</script>

<style lang="less" scoped>
.header-menu {
  background: #ffffff;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  
  /deep/ .el-input {
    flex: 1;
    min-width: 200px;
    
    .el-input__inner {
      height: 40px;
      border-radius: 10px;
      border: 1px solid #e5e7eb;
      transition: all 0.3s ease;
      
      &:focus {
        border-color: #6366f1;
        box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
      }
    }
  }
  
  /deep/ .el-select {
    min-width: 140px;
    
    .el-input__inner {
      height: 40px;
      border-radius: 10px;
    }
  }
  
  /deep/ .el-button {
    height: 40px;
    padding: 0 20px;
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &.el-button--primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
      }
    }
  }
}

/deep/ .el-table {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  th {
    background: #f9fafb !important;
    color: #6b7280 !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    padding: 16px 0;
    border-bottom: 2px solid #e5e7eb;
  }
  
  td {
    padding: 16px 0;
    border-bottom: 1px solid #f3f4f6;
  }
  
  .el-table__row {
    transition: all 0.2s ease;
    
    &:hover {
      background: #f9fafb !important;
    }
  }
  
  .el-button--danger {
    background: #fee;
    color: #ef4444;
    border: 1px solid #fecaca;
    
    &:hover {
      background: #ef4444;
      color: #ffffff;
      border-color: #ef4444;
    }
  }
  
  .el-button--primary {
    background: #ede9fe;
    color: #6366f1;
    border: 1px solid #ddd6fe;
    
    &:hover {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #ffffff;
      border: none;
    }
  }
}

.footer-button {
  margin-top: 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  
  /deep/ .el-pagination {
    .el-pager li {
      min-width: 36px;
      height: 36px;
      line-height: 36px;
      border-radius: 8px;
      margin: 0 4px;
      transition: all 0.3s ease;
      
      &:hover {
        background: #f3f4f6;
      }
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
      }
    }
    
    button {
      background: #ffffff;
      border-radius: 8px;
      width: 36px;
      height: 36px;
      transition: all 0.3s ease;
      
      &:hover {
        background: #f3f4f6;
      }
    }
  }
}
</style>