<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索学生" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchStudent">搜索</el-button>
      </el-input>
      <!-- 管理员可以按教师筛选 -->
      <el-select v-if="isAdmin" v-model="value3" placeholder="教师" @change="queryStudentByTeacher" @clear="teacherListener" clearable>
        <el-option
          v-for="item in teacherList"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
      <!-- 保留原有的年级班级筛选（可选） -->
      <el-select v-model="value1" placeholder="年级" @change="queryClass" @clear="gradeListener" clearable>
        <el-option v-for="item in grade" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-select v-model="value2" placeholder="班级" @change="queryStudentByClass" @clear="classListener" clearable>
        <el-option
          v-for="item in classNo"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
      <!-- 添加学生按钮 -->
      <el-button type="primary" icon="el-icon-plus" @click="openAddDialog">添加学生</el-button>
    </div>
    <!-- 数据显示 -->
    <el-table :data="studentData" size="mini" :stripe="true" :highlight-current-row="true" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="username" label="昵称" min-width="100"></el-table-column>
      <el-table-column prop="teacherName" label="所属教师" min-width="100"></el-table-column>
      <el-table-column label="课时进度" min-width="200" align="center">
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
      <el-table-column prop="courseRemark" label="课程备注" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="studentRemark" label="学生备注" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="140"></el-table-column>

      <el-table-column prop="operation" label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单编辑学生 -->
    <el-dialog title="编辑学生" :visible.sync="visibleForm" width="600px">
      <el-form
        :model="editFormData"
        label-position="left"
        label-width="100px"
        :rules="editFormRules"
      >
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="所属教师" prop="teacherId">
          <el-select v-model="editFormData.teacherId" placeholder="选择教师" clearable :disabled="!isAdmin">
            <el-option
              v-for="item in teacherList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="总课时" prop="totalHours">
          <el-input-number v-model="editFormData.totalHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="已上课时" prop="completedHours">
          <el-input-number v-model="editFormData.completedHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="剩余课时">
          <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
            {{ (editFormData.totalHours || 0) - (editFormData.completedHours || 0) }} 课时
          </span>
        </el-form-item>
        <el-form-item label="课程备注" prop="courseRemark">
          <el-input v-model="editFormData.courseRemark" type="textarea" :rows="3" placeholder="请输入课程备注" maxlength="500" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="学生备注" prop="studentRemark">
          <el-input v-model="editFormData.studentRemark" type="textarea" :rows="3" placeholder="请输入学生备注" maxlength="500" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 添加学生对话框 -->
    <el-dialog title="添加学生" :visible.sync="visibleAddForm" width="600px">
      <el-form
        :model="addFormData"
        label-position="left"
        label-width="100px"
        :rules="addFormRules"
        ref="addForm"
      >
        <el-form-item label="所属教师" prop="teacherId">
          <el-select v-model="addFormData.teacherId" placeholder="选择教师" clearable :disabled="!isAdmin">
            <el-option
              v-for="item in teacherList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="addFormData.username" autocomplete="off" placeholder="请输入学生昵称"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addFormData.password" type="password" autocomplete="off" placeholder="默认123456"></el-input>
        </el-form-item>
        <el-form-item label="总课时" prop="totalHours">
          <el-input-number v-model="addFormData.totalHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="已上课时" prop="completedHours">
          <el-input-number v-model="addFormData.completedHours" :min="0" :max="9999" :step="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="剩余课时">
          <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
            {{ (addFormData.totalHours || 0) - (addFormData.completedHours || 0) }} 课时
          </span>
        </el-form-item>
        <el-form-item label="课程备注" prop="courseRemark">
          <el-input v-model="addFormData.courseRemark" type="textarea" :rows="3" placeholder="请输入课程备注（选填）" maxlength="500" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="学生备注" prop="studentRemark">
          <el-input v-model="addFormData.studentRemark" type="textarea" :rows="3" placeholder="请输入学生备注（选填）" maxlength="500" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleAddForm = false">取 消</el-button>
        <el-button type="primary" @click="commitAdd()">提 交</el-button>
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
</template>

<script>
export default {
  name: "StudentList",
  data() {
    return {
      studentData: [],
      editFormData: [],
      addFormData: {
        teacherId: null,
        username: '',
        password: '123456',
        totalHours: 0,
        completedHours: 0,
        courseRemark: '',
        studentRemark: ''
      },
      keyword: "",
      page: 1,
      pageSize: 10,
      total: 0,
      value1: "", // 年级
      value2: "", // 班级
      value3: "", // 教师
      grade: [
        {
          value: "01",
          label: "高一"
        },
        {
          value: "02",
          label: "高二"
        },
        {
          value: "03",
          label: "高三"
        }
      ],
      classNo: [
        {
          value: "",
          label: ""
        }
      ],
      teacherList: [],
      visibleForm: false,
      visibleAddForm: false,
      editFormRules: {
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }]
      },
      addFormRules: {
        teacherId: [{ required: true, message: "请选择所属教师", trigger: "change" }],
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }]
      }
    };
  },

  computed: {
    isAdmin() {
      // 判断是否是管理员
      return window.localStorage.getItem('admin') != null;
    },
    currentTeacherId() {
      // 获取当前教师的ID
      let teacher = window.localStorage.getItem('teacher');
      if (teacher) {
        return JSON.parse(teacher).id;
      }
      return null;
    }
  },

  mounted() {
    // 加载教师列表（所有用户都需要，用于显示下拉选项）
    this.loadAllTeachers();
    
    // 如果是教师，直接加载自己的学生
    if (!this.isAdmin && this.currentTeacherId) {
      this.loadMyStudents();
    } else {
      this.allStudent();
    }
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
    
    // 打开添加学生对话框
    openAddDialog() {
      // 重置表单
      this.addFormData = {
        teacherId: this.isAdmin ? null : this.currentTeacherId, // 教师自动填充自己的ID
        username: '',
        password: '123456',
        totalHours: 0,
        completedHours: 0,
        courseRemark: '',
        studentRemark: ''
      }
      
      // 如果有表单引用，重置验证
      if (this.$refs.addForm) {
        this.$refs.addForm.clearValidate()
      }
      
      this.visibleAddForm = true
    },

    // 提交添加学生
    commitAdd() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          this.$axios.post("/student/add", this.addFormData)
            .then(res => {
              if (res.data.code == 0) {
                this.$message({message: "添加学生成功", type: "success"})
                this.visibleAddForm = false
                // 重新加载列表
                if (!this.isAdmin && this.currentTeacherId) {
                  this.loadMyStudents()
                } else {
                  this.allStudent()
                }
              } else {
                this.$message.error(res.data.message || "添加学生失败")
              }
            })
            .catch(error => {
              this.$message.error("添加学生失败")
            })
        }
      })
    },
    
    // 加载所有教师
    loadAllTeachers() {
      this.$axios.get("/teacher/all")
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.teacherList = ret.map(v => ({
              value: v.id,
              label: v.realname
            }))
          }
        })
        .catch(error => {
          console.error("加载教师列表失败", error)
        })
    },

    // 教师查看自己的学生
    loadMyStudents() {
      this.$axios
        .get(`/teacher/students/${this.currentTeacherId}/${this.page}`)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.enrichStudentData(ret.records)
            this.total = ret.total
          }
        })
        .catch(error => {
          this.$message.error("查询学生列表失败")
        })
    },

    // 根据教师查询学生
    queryStudentByTeacher() {
      if (!this.value3) {
        this.allStudent()
        return
      }
      this.$axios
        .get(`/student/teacher/${this.value3}/${this.page}`)
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.enrichStudentData(ret.records)
            this.total = ret.total
          }
        })
        .catch(error => {
          this.$message.error("查询失败")
        })
    },

    // 丰富学生数据（添加教师名称）
    enrichStudentData(students) {
      this.studentData = students.map(student => {
        // 根据 teacherId 查找教师名称
        if (student.teacherId && this.teacherList.length > 0) {
          const teacher = this.teacherList.find(t => t.value === student.teacherId)
          student.teacherName = teacher ? teacher.label : '未分配'
        } else {
          student.teacherName = '未分配'
        }
        return student
      })
    },

    // 清空年级回到查询所有学生
    gradeListener() {
      if (this.isAdmin) {
        this.allStudent()
      } else {
        this.loadMyStudents()
      }
      this.value2 = ''
    },

    // 清空班级回到查询所有班级
    classListener() {

    },

    // 清空教师筛选
    teacherListener() {
      this.allStudent()
    },

    // 查询班级信息
    queryClass() {
      this.$axios
        .get("/class-grade/" + this.value1)
        .then(res => {
          let ret = res.data.data
          this.classNo.splice(0, this.classNo.length)
          this.value2 = ""
          ret.map(v => {
            this.classNo.push({
              value: v.classNo,
              label: v.className
            });
          });
        })
        .catch(error => {
          
        });
    },

    // 根据班级查询学生信息
    queryStudentByClass() {
      this.$axios
        .get(
          "/student-class/" + this.page + "/" + this.value2
        )
        .then(res => {
          console.log(res)
          if (res.data.code == 0) {
            let ret = res.data.data
            this.studentData = ret.records
            this.total = ret.total
          }
        })
        .catch(error => {

        });
    },

    /***
     * 编辑提交
     */
    commit() {
      this.modifyStudent(this.editFormData)
    },

    inputListener() {
      this.allStudent()
    },

    /**
     * 查询所有学生
     */
    allStudent() {
      this.$axios
        .get("/student/students/" + this.page)
        .then(res => {
          let ret = res.data.data
          this.enrichStudentData(ret.records)
          this.total = ret.total
          // this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("查询学生列表失败")
        });
    },

    /**
     * 关键字查询学生
     */
    searchStudent() {
      if (!this.keyword) {
        this.$message.warning("请输入搜索关键字")
        return
      }
      this.$axios
        .get("/student/search/" + this.keyword)
        .then(res => {
          let ret = res.data.data
          this.enrichStudentData(ret.records)
          this.total = ret.total
          this.$message({ message: "查询成功", type: "success" })
        })
        .catch(error => {
          this.$message.error("查询失败")
        });
    },

    /**
     * 根据id删除学生
     */
    deleteById(index, row) {
      this.deleteStudentById(row.id)
    },

    deleteStudentById(id) {
      this.$axios
        .delete("/student/delete/" + id)
        .then(res => {
          this.$message({ message: "删除成功", type: "success" })
          this.allStudent()
        })
        .catch(error => {
          this.$message.error("删除失败")
        });
    },

    /**
     * 编辑学生
     */
    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
    },

    /**
     * 更新学生
     */
    modifyStudent(modifyData) {
      this.$axios
        .post("/student/modify/" + this.editFormData.id, modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" })
          this.allStudent()
          this.visibleForm = false
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v
      // 根据当前用户类型和筛选条件加载数据
      if (!this.isAdmin && this.currentTeacherId) {
        this.loadMyStudents()
      } else if (this.value3) {
        this.queryStudentByTeacher()
      } else {
        this.allStudent()
      }
    }
  }
};
</script>

<style lang="less" scoped>
.el-input-group {
  width: 40%;
  min-width: 300px;
}

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
    
    .el-input-group__append {
      background: transparent;
      border: none;
      padding: 0;
      
      .el-button {
        height: 40px;
        border-radius: 0 10px 10px 0;
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
      transform: scale(1.001);
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
  
  .el-progress {
    .el-progress-bar__outer {
      background: #e5e7eb;
    }
  }
}

/deep/ .el-dialog {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  
  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px 24px;
    border-radius: 16px 16px 0 0;
    
    .el-dialog__title {
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
    }
    
    .el-dialog__headerbtn {
      top: 20px;
      right: 24px;
      
      .el-dialog__close {
        color: #ffffff;
        font-size: 20px;
        font-weight: bold;
        
        &:hover {
          color: #f3f4f6;
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 24px;
    
    .el-form-item {
      margin-bottom: 22px;
      
      .el-form-item__label {
        color: #374151;
        font-weight: 600;
        line-height: 40px;
      }
      
      .el-input__inner,
      .el-textarea__inner {
        border-radius: 10px;
        border: 1px solid #e5e7eb;
        transition: all 0.3s ease;
        
        &:focus {
          border-color: #6366f1;
          box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
        }
      }
      
      .el-input-number {
        width: 100%;
        
        .el-input__inner {
          text-align: left;
        }
      }
    }
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
    
    .el-button {
      height: 40px;
      padding: 0 24px;
      border-radius: 10px;
      font-weight: 500;
      
      &:first-child {
        background: #f3f4f6;
        color: #6b7280;
        border: none;
        
        &:hover {
          background: #e5e7eb;
        }
      }
      
      &.el-button--primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
        }
      }
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