<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索学生" v-model="keyword" @clear="inputListener" clearable style="max-width: 400px;">
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
      <el-button slot="append" type="primary" icon="el-icon-search" @click="searchStudent">搜索</el-button>
      <!-- 添加学生按钮 -->
      <el-button class="add-student-btn" type="primary" icon="el-icon-plus" @click="openAddDialog">添加学生</el-button>
    </div>
    
    <!-- 统计信息 -->
    <div class="statistics-bar">
      <div class="stat-item">
        <i class="el-icon-s-custom"></i>
        <span class="stat-label">学生总数</span>
        <span class="stat-value">{{ total }}</span>
        <span class="stat-unit">人</span>
      </div>
    </div>
    
    <!-- 数据显示 -->
    <el-table :data="studentData" size="mini" :stripe="true" :highlight-current-row="true" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
      <el-table-column label="头像" width="80" align="center">
        <template slot-scope="scope">
          <el-avatar :size="40" :src="scope.row.avatar || defaultAvatar" fit="cover"></el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="昵称" min-width="100" align="center"></el-table-column>
      <el-table-column prop="teacherName" label="所属教师" min-width="100" align="center"></el-table-column>
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
              {{ formatHours(scope.row.completedHours) }}/{{ formatHours(scope.row.totalHours) }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="classDuration" label="课程时长" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.classDuration === 60 ? 'success' : 'primary'" size="small">
            {{ scope.row.classDuration || 45 }}分钟
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="courseRemark" label="课程备注" min-width="100" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="studentRemark" label="学生备注" min-width="100" align="center" show-overflow-tooltip></el-table-column>
      <!-- <el-table-column prop="createTime" label="创建时间" min-width="140"></el-table-column> -->
      <el-table-column label="课程日志" width="110" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" @click="openLog(scope.row)">查看日志</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="operation" label="操作" width="280" align="center" fixed="right">
        <template slot-scope="scope">
          <div class="operation-buttons">
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
            <el-button class="renew-btn" type="success" size="mini" @click="openRenewDialog(scope.row)">续课</el-button>
            <el-button class="delete-btn" type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单编辑学生 -->
    <el-dialog title="编辑学生" :visible.sync="visibleForm" width="600px" :close-on-click-modal="false">
      <el-form
        :model="editFormData"
        label-position="left"
        label-width="100px"
        :rules="editFormRules"
      >
        <el-form-item label="学生头像" prop="avatar">
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleEditAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <img v-if="editFormData.avatar" :src="editFormData.avatar" class="avatar-preview">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="avatar-upload-tip">支持 jpg/png 格式，不超过2MB</div>
          </div>
        </el-form-item>
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
          <el-input-number v-model="editFormData.totalHours" :min="0" :max="9999" :step="0.5" :precision="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="已上课时" prop="completedHours">
          <el-input-number v-model="editFormData.completedHours" :min="0" :max="9999" :step="0.5" :precision="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="剩余课时">
          <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
            {{ formatHours((editFormData.totalHours || 0) - (editFormData.completedHours || 0)) }} 课时
          </span>
        </el-form-item>
        <el-form-item label="课程时长" prop="classDuration">
          <el-radio-group v-model="editFormData.classDuration">
            <el-radio :label="45">45分钟</el-radio>
            <el-radio :label="60">60分钟</el-radio>
          </el-radio-group>
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
    <el-dialog title="添加学生" :visible.sync="visibleAddForm" width="600px" :close-on-click-modal="false">
      <el-form
        :model="addFormData"
        label-position="left"
        label-width="100px"
        :rules="addFormRules"
        ref="addForm"
      >
        <el-form-item label="学生头像" prop="avatar">
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAddAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <img v-if="addFormData.avatar" :src="addFormData.avatar" class="avatar-preview">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="avatar-upload-tip">支持 jpg/png 格式，不超过2MB</div>
          </div>
        </el-form-item>
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
          <el-input-number v-model="addFormData.totalHours" :min="0" :max="9999" :step="0.5" :precision="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="已上课时" prop="completedHours">
          <el-input-number v-model="addFormData.completedHours" :min="0" :max="9999" :step="0.5" :precision="1" controls-position="right" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="剩余课时">
          <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
            {{ formatHours((addFormData.totalHours || 0) - (addFormData.completedHours || 0)) }} 课时
          </span>
        </el-form-item>
        <el-form-item label="课程时长" prop="classDuration">
          <el-radio-group v-model="addFormData.classDuration">
            <el-radio :label="45">45分钟</el-radio>
            <el-radio :label="60">60分钟</el-radio>
          </el-radio-group>
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

    <!-- 课程完成日志对话框 -->
    <el-dialog title="课程完成日志" :visible.sync="visibleLog" width="700px" :close-on-click-modal="false">
      <div v-if="logStudent">
        <div style="margin-bottom: 10px; color: #606266;">
          学生：<strong style="color:#303133;">{{ logStudent.username }}</strong>
        </div>
        <el-empty v-if="currentLogLines.length === 0" description="暂无日志"></el-empty>
        <el-timeline v-else>
          <el-timeline-item
            v-for="(line, idx) in currentLogLines"
            :key="idx"
            :timestamp="extractTimestamp(line)"
            placement="top"
          >
            <div class="log-item">
              <div class="log-action">{{ parseLogLine(line).action }}</div>
              <div class="log-meta">
                <el-tag
                  v-for="(f, i) in parseLogLine(line).fields"
                  :key="i"
                  size="mini"
                  type="info"
                  effect="plain"
                >
                  {{ f.label }}：{{ f.value }}
                </el-tag>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleLog = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 续课对话框 -->
    <el-dialog title="学生续课" :visible.sync="visibleRenew" width="500px" :close-on-click-modal="false">
      <div v-if="renewStudent">
        <el-form :model="renewFormData" label-position="right" label-width="120px" ref="renewForm" :rules="renewFormRules">
          <el-form-item label="学生昵称">
            <span style="font-weight: 600; color: #303133; margin-left: 20px;">{{ renewStudent.username }}</span>
          </el-form-item>
          <el-form-item label="当前总课时">
            <span style="color: #409EFF; font-size: 16px; font-weight: bold; margin-left: 20px;">
              {{ formatHours(renewStudent.totalHours || 0) }} 课时
            </span>
          </el-form-item>
          <el-form-item label="当前剩余课时">
            <span style="color: #67C23A; font-size: 16px; font-weight: bold; margin-left: 20px;">
              {{ formatHours((renewStudent.totalHours || 0) - (renewStudent.completedHours || 0)) }} 课时
            </span>
          </el-form-item>
          <el-form-item label="增加课时" prop="hours">
            <el-input-number v-model="renewFormData.hours" :min="0" :max="9999" :step="1" :precision="1" controls-position="right" style="width: 100px; margin-left: 20px;"></el-input-number>
          </el-form-item>
          <el-form-item label="续课后总课时">
            <span style="color: #E6A23C; font-size: 18px; font-weight: bold; margin-left: 20px;">
              {{ formatHours((renewStudent.totalHours || 0) + (renewFormData.hours || 0)) }} 课时
            </span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleRenew = false">取 消</el-button>
        <el-button type="primary" @click="commitRenew()">确认续课</el-button>
      </div>
    </el-dialog>

    <!-- 上一页，当前页，下一页 -->
    <div class="footer-button">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="page"
        :page-size="pageSize"
        layout="prev, pager, next, total"
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
        avatar: '',
        totalHours: 0,
        completedHours: 0,
        classDuration: 45,
        courseRemark: '',
        studentRemark: ''
      },
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      uploadUrl: 'http://localhost:8008/upload/avatar',
      uploadHeaders: {
        'Authorization': localStorage.getItem('token') || ''
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
      visibleLog: false,
      visibleRenew: false,
      logStudent: null,
      renewStudent: null,
      currentLogLines: [],
      renewFormData: {
        hours: 0
      },
      editFormRules: {
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }]
      },
      addFormRules: {
        teacherId: [{ required: true, message: "请选择所属教师", trigger: "change" }],
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }]
      },
      renewFormRules: {
        hours: [
          { required: true, message: "请输入续课课时", trigger: "blur" },
          { type: 'number', min: 0.1, message: "续课课时必须大于0", trigger: "blur" }
        ]
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
    // 格式化课时显示（支持小数）
    formatHours(hours) {
      if (!hours || hours === 0) return '0';
      // 如果是整数，显示整数；否则显示一位小数
      return hours % 1 === 0 ? hours.toString() : hours.toFixed(1);
    },
    
    // 头像上传前验证
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
        return false
      }
      return true
    },

    // 编辑表单头像上传成功
    handleEditAvatarSuccess(res, file) {
      if (res.code === 0) {
        this.editFormData.avatar = res.data.url
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(res.message || '头像上传失败')
      }
    },

    // 添加表单头像上传成功
    handleAddAvatarSuccess(res, file) {
      if (res.code === 0) {
        this.addFormData.avatar = res.data.url
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(res.message || '头像上传失败')
      }
    },
    
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
        avatar: '',
        totalHours: 0,
        completedHours: 0,
        classDuration: 45,
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
              label: v.username
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
      // 清空搜索时根据用户类型加载数据
      if (!this.isAdmin && this.currentTeacherId) {
        this.loadMyStudents()
      } else {
        this.allStudent()
      }
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
      
      // 教师只能搜索自己的学生
      if (!this.isAdmin && this.currentTeacherId) {
        this.$axios
          .get("/student/search/" + this.keyword)
          .then(res => {
            let ret = res.data.data
            // 过滤只显示属于当前教师的学生
            if (ret.records) {
              ret.records = ret.records.filter(student => student.teacherId === this.currentTeacherId)
              ret.total = ret.records.length
            }
            this.enrichStudentData(ret.records)
            this.total = ret.total
            this.$message({ message: "查询成功", type: "success" })
          })
          .catch(error => {
            this.$message.error("查询失败")
          });
      } else {
        // 管理员可以搜索所有学生
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
      }
    },

    // 打开日志查看
    openLog(row) {
      this.logStudent = row
      const raw = row.completionLog || ''
      const lines = raw.split('\n').map(v => v.trim()).filter(v => v)
      // 最新的在上方展示
      this.currentLogLines = lines.reverse()
      this.visibleLog = true
    },

    // 提取时间戳 [yyyy-MM-dd HH:mm:ss]
    extractTimestamp(line) {
      const m = line.match(/^\[(.+?)\]/)
      return m ? m[1] : ''
    },

    // 提取消息内容
    extractMessage(line) {
      return line.replace(/^\[.+?\]\s*/, '')
    },

    // 解析日志行，拆分为动作和字段数组
    parseLogLine(line) {
      const message = this.extractMessage(line)
      // 优先以“>>>”后内容为基准
      const raw = message.replace(/^>>>\s*/, '')

      // 按中文顿号、逗号切分键值对
      const parts = raw.split(/[，,]/).map(v => v.trim()).filter(Boolean)

      const fields = []
      let action = ''

      parts.forEach((p, idx) => {
        const kv = p.split('：')
        if (kv.length >= 2) {
          const label = kv[0].trim()
          const value = kv.slice(1).join('：').trim()
          if (idx === 0 && /^完成|^新增|^修改|^删除|^完成课程/.test(p)) {
            action = p
          } else if (label && value) {
            fields.push({ label, value })
          }
        } else {
          // 无法识别为 key：value 的，作为 action 补充
          if (!action) action = p
        }
      })

      // 如果没有识别出动作，取第一段作为动作
      if (!action) action = parts[0] || raw

      // 对常见字段做排序优化显示
      const order = ['课程', '课程名', '日期', '时间', '教师', '课时', '课程ID']
      fields.sort((a, b) => {
        const ia = order.indexOf(a.label)
        const ib = order.indexOf(b.label)
        if (ia === -1 && ib === -1) return 0
        if (ia === -1) return 1
        if (ib === -1) return -1
        return ia - ib
      })

      // 规范化常见字段名
      fields.forEach(f => {
        if (f.label === '完成课程' || f.label === '课程名') f.label = '课程'
      })

      return { action, fields }
    },

    /**
     * 根据id删除学生
     */
    deleteById(index, row) {
      this.$confirm(`确定要删除学生"${row.username}"吗？此操作不可恢复！`, '确认删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        this.deleteStudentById(row.id)
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },

    deleteStudentById(id) {
      this.$axios
        .delete("/student/delete/" + id)
        .then(res => {
          this.$message({ message: "删除成功", type: "success" })
          // 根据当前筛选条件重新加载列表
          if (!this.isAdmin && this.currentTeacherId) {
            this.loadMyStudents()
          } else if (this.value3) {
            this.queryStudentByTeacher()
          } else {
            this.allStudent()
          }
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
      // 使用深拷贝避免直接修改原对象
      this.editFormData = JSON.parse(JSON.stringify(row))
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
          this.visibleForm = false
          // 根据当前筛选条件重新加载列表
          if (!this.isAdmin && this.currentTeacherId) {
            this.loadMyStudents()
          } else if (this.value3) {
            this.queryStudentByTeacher()
          } else {
            this.allStudent()
          }
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    // 打开续课对话框
    openRenewDialog(row) {
      this.renewStudent = row
      this.renewFormData = {
        hours: 0
      }
      
      // 如果有表单引用，重置验证
      if (this.$refs.renewForm) {
        this.$refs.renewForm.clearValidate()
      }
      
      this.visibleRenew = true
    },

    // 提交续课
    commitRenew() {
      this.$refs.renewForm.validate(valid => {
        if (valid) {
          if (!this.renewFormData.hours || this.renewFormData.hours <= 0) {
            this.$message.warning('续课课时必须大于0')
            return
          }
          
          this.$confirm(`确认为学生"${this.renewStudent.username}"续课 ${this.formatHours(this.renewFormData.hours)} 课时？`, '确认续课', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$axios.post(`/student/renew/${this.renewStudent.id}`, null, {
              params: {
                hours: this.renewFormData.hours
              }
            })
              .then(res => {
                if (res.data.code === 0) {
                  this.$message({
                    message: `续课成功！已为"${this.renewStudent.username}"增加 ${this.formatHours(this.renewFormData.hours)} 课时`,
                    type: 'success',
                    duration: 3000
                  })
                  this.visibleRenew = false
                  // 重新加载列表
                  if (!this.isAdmin && this.currentTeacherId) {
                    this.loadMyStudents()
                  } else if (this.value3) {
                    this.queryStudentByTeacher()
                  } else {
                    this.allStudent()
                  }
                } else {
                  this.$message.error(res.data.message || '续课失败')
                }
              })
              .catch(error => {
                this.$message.error('续课失败，请稍后重试')
              })
          }).catch(() => {
            this.$message.info('已取消续课')
          })
        }
      })
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
    min-width: 200px;
    
    .el-input__inner {
      height: 40px;
      border-radius: 10px;
      border: 1px solid #e5e7eb;
      transition: all 0.3s ease;
      padding: 0 15px;
      
      &:focus {
        border-color: #6366f1;
        box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
      }
    }
    
    .el-input-group__append {
      background: transparent;
      border: none;
      padding: 0 0 0 8px;
      
      .el-button {
        height: 40px;
        border-radius: 0 10px 10px 0;
        margin: 0 20px;
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
  
  .add-student-btn {
    margin-left: auto;
  }
}

// 统计信息栏样式
.statistics-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  display: flex;
  gap: 20px;
  align-items: center;
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    i {
      font-size: 28px;
      color: #ffffff;
    }
    
    .stat-label {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
    }
    
    .stat-value {
      font-size: 28px;
      color: #ffffff;
      font-weight: 700;
      line-height: 1;
    }
    
    .stat-unit {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
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
      
      .delete-btn,
      .renew-btn {
        opacity: 1;
        visibility: visible;
      }
    }
  }
  
  .operation-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
    flex-wrap: wrap;
    
    .delete-btn,
    .renew-btn {
      opacity: 0;
      visibility: hidden;
      transition: all 0.3s ease;
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
    background: #ddd6fe;
    color:rgb(255, 255, 255);
    border: 1px solid #c4b5fd;
    font-weight: 600;
    
    &:hover {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #ffffff;
      border: none;
    }
  }
  
  .el-button--success {
    background: #d1fae5;
    color: #10b981;
    border: 1px solid #a7f3d0;
    font-weight: 600;
    
    &:hover {
      background: #10b981;
      color: #ffffff;
      border-color: #10b981;
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
  position: sticky;
  bottom: 0;
  margin-top: 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  z-index: 10;
  
  /deep/ .el-pagination {
    .el-pagination__total {
      margin-top: 4px;
    }
    
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

// 头像上传样式
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

// 日志项样式
.log-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-action {
  font-weight: 600;
  color: #303133;
}

.log-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>