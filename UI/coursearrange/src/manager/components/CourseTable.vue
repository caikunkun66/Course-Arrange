<template>
  <div class="training-schedule">
    <div class="page-header">
      <h2>学生课程安排</h2>
      <p class="sub-title">为学生安排个性化的上课时间</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-card shadow="hover">
        <div class="filter-row">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            @change="handleMonthChange"
            style="width: 200px;"
          >
          </el-date-picker>

          <el-select
            v-model="selectedStudent"
            placeholder="选择学生（可选）"
            filterable
            clearable
            @change="handleStudentChange"
            style="width: 200px;"
          >
            <el-option
              v-for="student in studentList"
              :key="student.id"
              :label="student.username"
              :value="student.id"
            >
              <span style="float: left">{{ student.username }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ student.teacherName || '未分配' }}
              </span>
            </el-option>
          </el-select>

          <el-button type="primary" icon="el-icon-refresh" @click="loadMonthCourses">刷新</el-button>
          <el-button type="success" icon="el-icon-plus" @click="openAddDialog">添加课程</el-button>
        </div>

        <!-- 统计信息 -->
        <div class="stats-card">
          <div class="stat-item">
            <div class="stat-label">本月课程</div>
            <div class="stat-value">{{ monthCoursesCount }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">本月学生</div>
            <div class="stat-value">{{ monthStudentsCount }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">总课时</div>
            <div class="stat-value">{{ monthTotalHours.toFixed(1) }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">已完成</div>
            <div class="stat-value" style="color: #67c23a;">{{ monthCompletedCount }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 日历视图 -->
    <div class="calendar-section">
      <el-card shadow="hover" v-loading="loadingCourses">
        <el-calendar v-model="calendarValue">
          <template slot="dateCell" slot-scope="{date, data}">
            <div class="calendar-day">
              <div class="day-number" :class="{'today': isToday(data.day), 'selected-month': isSelectedMonth(data.day)}">
                <span class="day-text">{{ data.day.split('-').slice(2).join('-') }}</span>
                <button 
                  v-if="isSelectedMonth(data.day)"
                  class="add-course-btn" 
                  @click.stop="openAddDialogForDate(data.day)"
                  title="添加课程"
                >
                  <i class="el-icon-plus"></i>
                </button>
              </div>
              <div class="day-courses" v-if="getDayCourses(data.day).length > 0">
                <div
                  v-for="course in getDayCourses(data.day)"
                  :key="course.id"
                  class="course-item"
                  :class="'status-' + getCourseStatus(course)"
                  @click="showCourseDetail(course)"
                  :title="`${course.startTime}-${course.endTime} ${getStudentName(course.studentId)} (${course.courseName})`"
                >
                  <span class="course-time">{{ course.startTime }}-{{ course.endTime }}</span>
                  <span class="course-student">{{ getStudentName(course.studentId) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-calendar>
      </el-card>
    </div>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      :visible.sync="detailDialogVisible"
      width="500px"
    >
      <div v-if="selectedCourse" class="course-detail">
        <div class="detail-row">
          <label>学生：</label>
          <span>{{ getStudentName(selectedCourse.studentId) }}</span>
        </div>
        <div class="detail-row">
          <label>课程：</label>
          <span>{{ selectedCourse.courseName }}</span>
        </div>
        <div class="detail-row">
          <label>日期：</label>
          <span>{{ selectedCourse.courseDate }}</span>
        </div>
        <div class="detail-row">
          <label>时间：</label>
          <span>{{ selectedCourse.startTime }} - {{ selectedCourse.endTime }}</span>
        </div>
        <div class="detail-row">
          <label>教师：</label>
          <span>{{ selectedCourse.teacherName || '未指定' }}</span>
        </div>
        <div class="detail-row">
          <label>课时：</label>
          <span>{{ selectedCourse.duration }} 课时</span>
        </div>
        <div class="detail-row" v-if="selectedCourse.remark">
          <label>备注：</label>
          <span>{{ selectedCourse.remark }}</span>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editCourseFromDetail">编辑</el-button>
        <el-button type="danger" @click="deleteCourseFromDetail">删除</el-button>
      </div>
    </el-dialog>

    <!-- 每日课程列表对话框 -->
    <el-dialog
      :title="selectedDayTitle"
      :visible.sync="dayCourseDialogVisible"
      width="700px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="course in selectedDayCourses"
          :key="course.id"
          :timestamp="course.startTime + ' - ' + course.endTime"
          placement="top"
        >
          <el-card shadow="hover" class="course-card-small">
            <div class="course-header-small">
              <div>
                <el-tag size="small" type="primary">{{ getStudentName(course.studentId) }}</el-tag>
                <span style="margin-left: 10px; font-weight: bold;">{{ course.courseName }}</span>
              </div>
              <div>
                <el-button size="mini" type="text" icon="el-icon-edit" @click="editCourse(course)">编辑</el-button>
                <el-button size="mini" type="text" icon="el-icon-delete" style="color: #f56c6c;" @click="deleteCourse(course)">删除</el-button>
              </div>
            </div>
            <div class="course-details-small">
              <span><i class="el-icon-user"></i> {{ course.teacherName }}</span>
              <span><i class="el-icon-timer"></i> {{ course.duration }} 课时</span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="courseForm" :rules="courseRules" ref="courseForm" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="courseForm.studentId"
            placeholder="选择学生"
            filterable
            style="width: 100%;"
            :disabled="!!selectedStudent"
          >
            <el-option
              v-for="student in studentList"
              :key="student.id"
              :label="student.username"
              :value="student.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="课程" prop="courseName">
          <el-input
            v-model="courseForm.courseName"
            placeholder="课程名称"
            :disabled="!isEdit && !!courseForm.courseName"
            style="width: 100%;"
          >
          </el-input>
          <span v-if="!isEdit && courseForm.courseName && currentTeacher" style="color: #909399; font-size: 12px; margin-top: 5px; display: block;">
            来源：教师授课科目（{{ currentTeacher.teach }}）
          </span>
        </el-form-item>

        <el-form-item label="上课日期" prop="courseDate">
          <el-date-picker
            v-model="courseForm.courseDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          >
          </el-date-picker>
        </el-form-item>

        <el-form-item label="上课时间" required>
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-time-picker
                v-model="courseForm.startTime"
                placeholder="开始时间"
                value-format="HH:mm"
                format="HH:mm"
                style="width: 100%;"
              >
              </el-time-picker>
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center;">-</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-time-picker
                v-model="courseForm.endTime"
                placeholder="结束时间"
                value-format="HH:mm"
                format="HH:mm"
                style="width: 100%;"
              >
              </el-time-picker>
            </el-form-item>
          </el-col>
        </el-form-item>

        <el-form-item label="授课教师" prop="teacherId">
          <el-select
            v-model="courseForm.teacherId"
            placeholder="选择教师"
            filterable
            clearable
            style="width: 100%;"
            :disabled="!isEdit && !!courseForm.teacherId"
          >
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="teacher.realname"
              :value="teacher.id"
            ></el-option>
          </el-select>
          <span v-if="!isEdit && currentTeacher && courseForm.teacherId === currentTeacher.id" style="color: #67c23a; font-size: 12px; margin-top: 5px; display: block;">
            <i class="el-icon-success"></i> 已自动选择：{{ currentTeacher.realname }}（当前登录教师）
          </span>
        </el-form-item>

        <el-form-item label="课时数" prop="duration">
          <el-input-number
            v-model="courseForm.duration"
            :min="0.5"
            :max="10"
            :step="0.5"
            :precision="1"
            style="width: 100%;"
          ></el-input-number>
          <span style="color: #909399; font-size: 12px; margin-left: 10px;">（每节课的课时数）</span>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="courseForm.remark"
            type="textarea"
            :rows="3"
            placeholder="课程备注信息"
          ></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitCourse" :loading="submitting">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TrainingSchedule',
  data() {
    return {
      // 筛选条件
      selectedStudent: null,
      selectedMonth: null,
      calendarValue: new Date(),
      
      // 数据列表
      studentList: [],
      courseList: [],
      courseInfoList: [],
      teacherList: [],
      
      // 选中的学生信息
      selectedStudentInfo: null,
      
      // 当前登录教师信息
      currentTeacher: null,
      
      // 加载状态
      loading: false,
      loadingCourses: false,
      
      // 对话框
      dialogVisible: false,
      dialogTitle: '添加课程',
      isEdit: false,
      submitting: false,
      detailDialogVisible: false,
      dayCourseDialogVisible: false,
      
      // 选中的课程和日期
      selectedCourse: null,
      selectedDayCourses: [],
      selectedDayTitle: '',
      
      // 课程表单
      courseForm: {
        id: null,
        studentId: null,
        courseName: '',
        courseDate: '',
        startTime: '',
        endTime: '',
        teacherId: null,
        duration: 1,
        remark: ''
      },
      
      // 默认头像
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      
      // 表单验证规则
      courseRules: {
        studentId: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        courseName: [
          { required: true, message: '请选择或输入课程', trigger: 'blur' }
        ],
        courseDate: [
          { required: true, message: '请选择上课日期', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' }
        ],
        duration: [
          { required: true, message: '请输入课时数', trigger: 'blur' }
        ]
      }
    };
  },
  
  computed: {
    // 本月课程总数
    monthCoursesCount() {
      return this.courseList.length;
    },
    
    // 本月涉及学生数
    monthStudentsCount() {
      const studentIds = new Set(this.courseList.map(c => c.studentId));
      return studentIds.size;
    },
    
    // 本月总课时
    monthTotalHours() {
      return this.courseList.reduce((sum, course) => sum + (course.duration || 0), 0);
    },
    
    // 已完成课程数
    monthCompletedCount() {
      const now = new Date();
      return this.courseList.filter(course => {
        const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
        return now > endDateTime;
      }).length;
    }
  },
  
  async mounted() {
    this.loadCurrentTeacher();
    this.initSelectedMonth();
    await this.loadInitialData();
    // 等待初始数据加载完成后再加载课程
    this.loadMonthCourses();
  },
  
  methods: {
    // 初始化当前月份
    initSelectedMonth() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      this.selectedMonth = `${year}-${month}`;
    },
    // 加载当前登录教师信息
    loadCurrentTeacher() {
      const teacherStr = localStorage.getItem('teacher');
      if (teacherStr) {
        this.currentTeacher = JSON.parse(teacherStr);
        console.log('当前教师信息:', this.currentTeacher);
      }
    },
    
    // 加载初始数据
    async loadInitialData() {
      await this.loadStudents();
      await this.loadCourseInfo();
      await this.loadTeachers();
    },
    
    // 加载学生列表
    async loadStudents() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('user'));
        let url = '/student/students/1';
        
        // 如果是教师角色，只加载自己的学生
        if (userInfo && userInfo.userType === 2) {
          url = `/teacher/students/${userInfo.id}/1`;
        }
        
        const res = await this.$axios.get(url);
        if (res.data.code === 0) {
          this.studentList = res.data.data.records || res.data.data || [];
        }
      } catch (error) {
        this.$message.error('加载学生列表失败');
        console.error(error);
      }
    },
    
    // 加载课程信息
    async loadCourseInfo() {
      try {
        const res = await this.$axios.get('/courseinfo/1?limit=1000');
        if (res.data.code === 0) {
          this.courseInfoList = res.data.data.records || [];
        }
      } catch (error) {
        this.$message.error('加载课程列表失败');
        console.error(error);
      }
    },
    
    // 加载教师列表
    async loadTeachers() {
      try {
        const res = await this.$axios.get('/teacher/all');
        if (res.data.code === 0) {
          this.teacherList = res.data.data || [];
        }
      } catch (error) {
        this.$message.error('加载教师列表失败');
        console.error(error);
      }
    },
    
    // 月份变化
    handleMonthChange() {
      if (this.selectedMonth) {
        const [year, month] = this.selectedMonth.split('-');
        this.calendarValue = new Date(year, month - 1, 1);
        this.loadMonthCourses();
      }
    },
    
    // 学生选择变化
    handleStudentChange() {
      this.loadMonthCourses();
    },
    
    // 加载月度课程
    async loadMonthCourses() {
      if (!this.selectedMonth) {
        return;
      }
      
      this.loadingCourses = true;
      try {
        // 计算月份的起止日期
        const [year, month] = this.selectedMonth.split('-');
        const startDate = `${year}-${month}-01`;
        const lastDay = new Date(year, month, 0).getDate();
        const endDate = `${year}-${month}-${String(lastDay).padStart(2, '0')}`;
        
        // 如果选择了特定学生，只查询该学生的课程
        if (this.selectedStudent) {
          const res = await this.$axios.get(`/student-course/${this.selectedStudent}`, {
            params: { startDate, endDate }
          });
          if (res.data && res.data.code === 0) {
            this.courseList = res.data.data || [];
          }
        } else {
          // 查询当前教师所有学生的课程
          if (!this.currentTeacher) {
            this.$message.warning('未获取到教师信息');
            return;
          }
          
          // 获取当前教师的所有学生
          const allCourses = [];
          for (const student of this.studentList) {
            try {
              const res = await this.$axios.get(`/student-course/${student.id}`, {
                params: { startDate, endDate }
              });
              if (res.data && res.data.code === 0 && res.data.data) {
                allCourses.push(...res.data.data);
              }
            } catch (error) {
              console.warn(`查询学生${student.id}的课程失败`, error);
            }
          }
          this.courseList = allCourses;
        }
        
        // 按日期和时间排序
        this.courseList.sort((a, b) => {
          const dateCompare = a.courseDate.localeCompare(b.courseDate);
          if (dateCompare !== 0) return dateCompare;
          return a.startTime.localeCompare(b.startTime);
        });
        
      } catch (error) {
        console.error('查询月度课程失败:', error);
        this.$message.error('查询课程失败');
        this.courseList = [];
      } finally {
        this.loadingCourses = false;
      }
    },
    
    // 获取指定日期的课程
    getDayCourses(day) {
      return this.courseList.filter(course => course.courseDate === day);
    },
    
    // 判断是否为今天
    isToday(day) {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const date = String(today.getDate()).padStart(2, '0');
      return day === `${year}-${month}-${date}`;
    },
    
    // 判断是否为选中的月份
    isSelectedMonth(day) {
      if (!this.selectedMonth) return true;
      return day.startsWith(this.selectedMonth);
    },
    
    // 获取课程状态
    getCourseStatus(course) {
      const now = new Date();
      const courseDateTime = new Date(`${course.courseDate} ${course.startTime}`);
      const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
      
      if (now > endDateTime) {
        return 'completed';
      } else if (now >= courseDateTime && now <= endDateTime) {
        return 'ongoing';
      } else {
        return 'pending';
      }
    },
    
    // 获取学生姓名
    getStudentName(studentId) {
      const student = this.studentList.find(s => s.id === studentId);
      return student ? student.username : '未知学生';
    },
    
    // 显示课程详情
    showCourseDetail(course) {
      this.selectedCourse = course;
      this.detailDialogVisible = true;
    },
    
    // 显示某一天的所有课程
    showDayCourses(day) {
      this.selectedDayCourses = this.getDayCourses(day);
      const dateObj = new Date(day);
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      const weekDay = weekDays[dateObj.getDay()];
      this.selectedDayTitle = `${day} ${weekDay} 的课程安排`;
      this.dayCourseDialogVisible = true;
    },
    
    // 从详情对话框编辑
    editCourseFromDetail() {
      this.detailDialogVisible = false;
      this.editCourse(this.selectedCourse);
    },
    
    // 从详情对话框删除
    deleteCourseFromDetail() {
      this.detailDialogVisible = false;
      this.deleteCourse(this.selectedCourse);
    },
    
    // 打开添加对话框
    openAddDialog() {
      this.isEdit = false;
      this.dialogTitle = '添加课程';
      this.resetForm();
      
      // 自动设置学生
      if (this.selectedStudent) {
        this.courseForm.studentId = this.selectedStudent;
      }
      
      // 自动设置当前教师和对应的课程
      if (this.currentTeacher) {
        this.courseForm.teacherId = this.currentTeacher.id;
        
        // 直接使用教师的teach字段作为课程名称
        if (this.currentTeacher.teach) {
          this.courseForm.courseName = this.currentTeacher.teach;
          console.log('已自动填充课程:', this.currentTeacher.teach);
        }
      }
      
      this.dialogVisible = true;
    },
    
    // 为指定日期打开添加对话框
    openAddDialogForDate(date) {
      this.isEdit = false;
      this.dialogTitle = `添加课程 - ${date}`;
      this.resetForm();
      
      // 自动设置日期
      this.courseForm.courseDate = date;
      
      // 自动设置学生
      if (this.selectedStudent) {
        this.courseForm.studentId = this.selectedStudent;
      }
      
      // 自动设置当前教师和对应的课程
      if (this.currentTeacher) {
        this.courseForm.teacherId = this.currentTeacher.id;
        
        // 直接使用教师的teach字段作为课程名称
        if (this.currentTeacher.teach) {
          this.courseForm.courseName = this.currentTeacher.teach;
        }
      }
      
      this.dialogVisible = true;
    },
    
    // 编辑课程
    editCourse(course) {
      this.isEdit = true;
      this.dialogTitle = '编辑课程';
      this.courseForm = Object.assign({}, course);
      this.dialogVisible = true;
    },
    
    // 删除课程
    deleteCourse(course) {
      this.$confirm(`确定要删除 ${course.courseName} 这节课吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$axios.delete(`/student-course/${course.id}`);
          if (res.data.code === 0) {
            this.$message.success('删除成功');
            this.dayCourseDialogVisible = false;
            this.loadMonthCourses();
          } else {
            this.$message.error(res.data.message || '删除失败');
          }
        } catch (error) {
          this.$message.error('删除失败');
          console.error(error);
        }
      }).catch(() => {});
    },
    
    // 提交课程
    submitCourse() {
      this.$refs.courseForm.validate(async (valid) => {
        if (valid) {
          // 验证时间
          if (this.courseForm.startTime >= this.courseForm.endTime) {
            this.$message.error('结束时间必须晚于开始时间');
            return;
          }
          
          this.submitting = true;
          try {
            const url = this.isEdit ? '/student-course/update' : '/student-course/add';
            const res = await this.$axios.post(url, this.courseForm);
            
            if (res.data.code === 0) {
              this.$message.success(this.isEdit ? '修改成功' : '添加成功');
              this.dialogVisible = false;
              this.loadMonthCourses();
            } else {
              this.$message.error(res.data.message || '操作失败');
            }
          } catch (error) {
            this.$message.error('操作失败');
            console.error(error);
          } finally {
            this.submitting = false;
          }
        }
      });
    },
    
    // 重置表单
    resetForm() {
      this.courseForm = {
        id: null,
        studentId: null,
        courseName: '',
        courseDate: '',
        startTime: '',
        endTime: '',
        teacherId: null,
        duration: 1,
        remark: ''
      };
      if (this.$refs.courseForm) {
        this.$refs.courseForm.resetFields();
      }
    },
    
    
    // 格式化日期时间
    formatDateTime(date, time) {
      if (!date) return '';
      const dateObj = new Date(date);
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      const weekDay = weekDays[dateObj.getDay()];
      return `${date} ${weekDay} ${time}`;
    },
    
    // 获取状态颜色
    getStatusColor(course) {
      const now = new Date();
      const courseDateTime = new Date(`${course.courseDate} ${course.startTime}`);
      const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
      
      if (now > endDateTime) {
        return '#909399'; // 已完成
      } else if (now >= courseDateTime && now <= endDateTime) {
        return '#67c23a'; // 进行中
      } else {
        return '#409eff'; // 未开始
      }
    },
    
    // 获取状态标签类型
    getStatusTagType(course) {
      const now = new Date();
      const courseDateTime = new Date(`${course.courseDate} ${course.startTime}`);
      const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
      
      if (now > endDateTime) {
        return 'info'; // 已完成
      } else if (now >= courseDateTime && now <= endDateTime) {
        return 'success'; // 进行中
      } else {
        return ''; // 未开始
      }
    },
    
    // 获取状态文本
    getStatusText(course) {
      const now = new Date();
      const courseDateTime = new Date(`${course.courseDate} ${course.startTime}`);
      const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
      
      if (now > endDateTime) {
        return '已完成';
      } else if (now >= courseDateTime && now <= endDateTime) {
        return '进行中';
      } else {
        return '未开始';
      }
    },
    
    // 计算进度百分比
    calculateProgress(completed, total) {
      if (!total || total === 0) return 0;
      return Math.round((completed / total) * 100);
    },
    
    // 获取进度条颜色
    getProgressColor(completed, total) {
      const percentage = this.calculateProgress(completed, total);
      if (percentage >= 80) return '#f56c6c';
      if (percentage >= 50) return '#e6a23c';
      return '#67c23a';
    }
  }
};
</script>

<style lang="less" scoped>
.training-schedule {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);

  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
    
    .sub-title {
      margin: 5px 0 0 0;
      font-size: 14px;
      color: #909399;
    }
  }

  .filter-section {
    margin-bottom: 20px;
    
    .filter-row {
      display: flex;
      gap: 15px;
      align-items: center;
      flex-wrap: wrap;
      margin-bottom: 20px;
    }
    
    .stats-card {
      display: flex;
      gap: 30px;
      padding: 20px;
      border-top: 1px solid #ebeef5;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 4px;
      
      .stat-item {
        flex: 1;
        text-align: center;
        
        .stat-label {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.8);
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #ffffff;
        }
      }
    }
  }

  .calendar-section {
    margin-bottom: 20px;
    
    /deep/ .el-calendar-table {
      .el-calendar-day {
        height: 150px;
        padding: 0;
      }
    }
    
    .calendar-day {
      height: 100%;
      display: flex;
      flex-direction: column;
      position: relative;
      
      &:hover {
        .day-number .add-course-btn {
          opacity: 1 !important;
          visibility: visible !important;
        }
      }
      
      .day-number {
        padding: 3px;
        text-align: left;
        font-weight: bold;
        color: #606266;
        font-size: 14px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        gap: 5px;
        
        .day-text {
          flex-shrink: 0;
        }
        
        &.today {
          .day-text {
            color: #409eff;
          }
        }
        
        &:not(.selected-month) {
          .day-text {
            color: #c0c4cc;
          }
        }
        
        .add-course-btn {
          opacity: 0;
          visibility: hidden;
          transition: all 0.3s;
          margin-left: auto;
          border: none;
          background: #409eff;
          color: #ffffff;
          border-radius: 50%;
          width: 20px;
          height: 20px;
          min-width: 20px;
          padding: 0;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          i {
            font-size: 10px;
          }
          
          &:hover {
            background: #66b1ff;
          }
          
          &:active {
            background: #3a8ee6;
          }
        }
      }
      
      .day-courses {
        flex: 1;
        overflow-y: auto;
        padding: 2px;
        
        // 自定义滚动条
        &::-webkit-scrollbar {
          width: 4px;
        }
        
        &::-webkit-scrollbar-thumb {
          background: #dcdfe6;
          border-radius: 2px;
          
          &:hover {
            background: #c0c4cc;
          }
        }
        
        .course-item {
          margin-bottom: 2px;
          padding: 2px 4px;
          border-radius: 2px;
          font-size: 11px;
          cursor: pointer;
          transition: all 0.2s;
          border-left: 2px solid;
          display: flex;
          align-items: center;
          gap: 4px;
          white-space: nowrap;
          overflow: hidden;
          
          &.status-pending {
            background: #ecf5ff;
            border-color: #409eff;
            
            .course-time {
              color: #409eff;
            }
            
            &:hover {
              background: #d9ecff;
              transform: translateX(2px);
            }
          }
          
          &.status-ongoing {
            background: #f0f9ff;
            border-color: #67c23a;
            
            .course-time {
              color: #67c23a;
            }
            
            &:hover {
              background: #e1f3d8;
              transform: translateX(2px);
            }
          }
          
          &.status-completed {
            background: #f4f4f5;
            border-color: #909399;
            opacity: 0.7;
            
            .course-time {
              color: #909399;
            }
            
            &:hover {
              background: #e9e9eb;
              transform: translateX(2px);
            }
          }
          
          .course-time {
            font-weight: bold;
            font-size: 10px;
            flex-shrink: 0;
            min-width: 65px;
          }
          
          .course-student {
            color: #303133;
            font-weight: bold;
            font-size: 11px;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
      }
    }
  }

  // 课程详情对话框
  .course-detail {
    .detail-row {
      display: flex;
      margin-bottom: 15px;
      
      label {
        width: 80px;
        font-weight: bold;
        color: #606266;
      }
      
      span {
        flex: 1;
        color: #303133;
      }
    }
  }

  // 每日课程卡片
  .course-card-small {
    .course-header-small {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
    }
    
    .course-details-small {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: #606266;
      
      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }

  .dialog-footer {
    text-align: right;
  }
}

// 响应式设计
@media screen and (max-width: 768px) {
  .training-schedule {
    padding: 10px;
    
    .filter-row {
      flex-direction: column;
      align-items: stretch !important;
      
      .el-select,
      .el-date-picker,
      .el-button {
        width: 100% !important;
      }
    }
    
    .student-info-card {
      flex-direction: column;
      align-items: flex-start !important;
      gap: 15px !important;
    }
  }
}
</style>
