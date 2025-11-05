<template>
  <div class="training-schedule">
    <!-- 日历视图 -->
    <div class="calendar-section">
      <el-card shadow="hover">
        <div class="calendar-header-wrapper">
          <!-- 左侧控制区 -->
          <div class="calendar-controls">
            <!-- 月份选择器 -->
            <div class="calendar-month-selector">
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                value-format="yyyy-MM"
                @change="handleMonthChange"
                size="small"
                prefix-icon="el-icon-date"
              >
              </el-date-picker>
              <span class="month-nav">
                <el-button 
                  icon="el-icon-arrow-left" 
                  size="mini" 
                  circle
                  @click="prevMonth"
                  title="上个月"
                ></el-button>
                <el-button 
                  icon="el-icon-arrow-right" 
                  size="mini" 
                  circle
                  @click="nextMonth"
                  title="下个月"
                ></el-button>
              </span>
            </div>
            
            <!-- 教师选择器（仅管理员可见） -->
            <div v-if="isAdmin" class="teacher-selector">
              <el-select
                v-model="selectedTeacher"
                placeholder="全部教师"
                filterable
                clearable
                @change="handleTeacherChange"
                size="small"
              >
                <el-option
                  v-for="teacher in teacherList"
                  :key="teacher.id"
                  :label="teacher.username"
                  :value="teacher.id"
                >
                  <span style="float: left">{{ teacher.username }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ teacher.teach || '未设置科目' }}
                  </span>
                </el-option>
              </el-select>
            </div>
            
            <!-- 学生选择器（管理员和教师可见） -->
            <div v-if="!isStudent" class="student-selector">
              <el-select
                v-model="selectedStudent"
                placeholder="全部学生"
                filterable
                clearable
                @change="handleStudentChange"
                size="small"
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
            </div>
            
            <!-- 当前学生信息（仅学生可见） -->
            <div v-if="isStudent" class="current-student-info">
              <span class="student-name">{{ currentStudent ? currentStudent.username : '学生' }} 的课程表</span>
            </div>
            
            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-refresh" size="small" @click="handleReset" circle title="重置"></el-button>
              <el-button v-if="!isStudent" type="success" icon="el-icon-plus" size="small" @click="openAddDialog" circle title="添加课程"></el-button>
            </div>
          </div>
          
          <!-- 统计信息 -->
          <div class="stats-card">
            <div class="stat-item">
              <div class="stat-label">本月课程</div>
              <div class="stat-value">{{ monthCoursesCount }}</div>
            </div>
            <div v-if="!isStudent" class="stat-item">
              <div class="stat-label">本月学生</div>
              <div class="stat-value">{{ monthStudentsCount }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">总课时</div>
              <div class="stat-value">{{ formatHours(monthTotalHours) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已完成</div>
              <div class="stat-value completed-count">{{ monthCompletedCount }}</div>
            </div>
          </div>
        </div>
        
        <el-calendar v-model="calendarValue">
          <template slot="dateCell" slot-scope="{date, data}">
            <div class="calendar-day">
              <div class="day-number" :class="{'today': isToday(data.day), 'selected-month': isSelectedMonth(data.day)}">
                <span class="day-text">{{ data.day.split('-').slice(2).join('-') }}</span>
                <button 
                  v-if="!isStudent && isSelectedMonth(data.day)"
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
                  :title="getCourseTooltip(course)"
                >
                  <span class="course-time">{{ course.startTime }}-{{ course.endTime }}</span>
                  <span class="course-student">{{ getStudentName(course.studentId) }}</span>
                  <span v-if="!isStudent" class="course-actions">
                    <el-button
                      v-if="getCourseStatus(course) === 'pending'"
                      type="success"
                      size="mini"
                      circle
                      plain
                      icon="el-icon-check"
                      title="完成课程"
                      @click.stop="markCourseComplete(course)"
                    ></el-button>
                  </span>
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
          <span>{{ formatHours(selectedCourse.duration) }} 课时</span>
        </div>
        <div class="detail-row" v-if="selectedCourse.remark">
          <label>备注：</label>
          <span>{{ selectedCourse.remark }}</span>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="!isStudent" type="primary" @click="editCourseFromDetail">编辑</el-button>
        <el-button 
          v-if="!isStudent && selectedCourse && getCourseStatus(selectedCourse) === 'pending'"
          type="warning" 
          @click="markCourseCancel(selectedCourse)"
        >取消课程</el-button>
        <el-button v-if="!isStudent" type="danger" @click="deleteCourseFromDetail">删除</el-button>
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
              <div v-if="!isStudent">
                <el-button size="mini" type="text" icon="el-icon-edit" @click="editCourse(course)">编辑</el-button>
                <el-button size="mini" type="text" icon="el-icon-delete" style="color: #f56c6c;" @click="deleteCourse(course)">删除</el-button>
              </div>
            </div>
            <div class="course-details-small">
              <span><i class="el-icon-user"></i> {{ course.teacherName }}</span>
              <span><i class="el-icon-timer"></i> {{ formatHours(course.duration) }} 课时</span>
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
              v-for="student in filteredStudentList"
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
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程" prop="courseName">
              <el-input
                v-model="courseForm.courseName"
                placeholder="课程名称"
                :disabled="!isEdit && !!courseForm.courseName"
                style="width: 100%;"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select
                v-model="courseForm.teacherId"
                placeholder="选择教师"
                filterable
                clearable
                style="width: 100%;"
                :disabled="!isAdmin && !isEdit && !!courseForm.teacherId"
              >
                <el-option
                  v-for="teacher in teacherList"
                  :key="teacher.id"
                  :label="teacher.username"
                  :value="teacher.id"
                >
                  <span style="float: left">{{ teacher.username }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ teacher.teach || '未设置科目' }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 上课日期（从日历点击添加时隐藏） -->
        <el-form-item v-if="!isDatePreset" label="上课日期" prop="courseDate">
          <el-date-picker
            v-model="courseForm.courseDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          >
          </el-date-picker>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <div class="time-input-wrapper">
                <el-select
                  v-model="selectedHour"
                  placeholder="小时"
                  class="time-hour-select"
                  filterable
                  allow-create
                  default-first-option
                  @change="updateStartTime"
                >
                  <el-option
                    v-for="hour in hourOptions"
                    :key="hour"
                    :label="hour"
                    :value="hour"
                  ></el-option>
                </el-select>
                <span class="time-separator">:</span>
                <el-select
                  v-model="selectedMinute"
                  placeholder="分钟"
                  class="time-minute-select"
                  filterable
                  allow-create
                  default-first-option
                  @change="updateStartTime"
                >
                  <el-option
                    v-for="minute in minuteOptions"
                    :key="minute"
                    :label="minute"
                    :value="minute"
                  ></el-option>
                </el-select>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课时数" prop="duration">
              <el-input-number
                v-model="courseForm.duration"
                :min="0.5"
                :max="10"
                :step="0.5"
                :precision="1"
                style="width: 100%;"
                @change="calculateEndTime"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-input
                v-model="courseForm.endTime"
                placeholder="自动计算"
                disabled
                class="end-time-input"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总时长">
              <el-input
                :value="courseForm.duration ? totalMinutes + ' 分钟' : ''"
                placeholder="自动计算"
                disabled
                class="end-time-input"
              >
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="courseForm.remark"
            type="textarea"
            :rows="3"
            placeholder="课程备注信息"
          ></el-input>
        </el-form-item>

        <!-- 重复模式和重复次数（仅在添加时显示） -->
        <el-row v-if="!isEdit" :gutter="20">
          <el-col :span="12">
            <el-form-item label="重复模式">
              <el-select
                v-model="courseForm.repeatMode"
                placeholder="选择重复模式"
                style="width: 100%;"
                @change="handleRepeatModeChange"
              >
                <el-option label="不重复（仅添加一次）" value="none"></el-option>
                <el-option label="每天" value="daily"></el-option>
                <el-option label="每周" value="weekly"></el-option>
                <el-option label="工作日（周一至周五）" value="weekday"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item 
              v-if="courseForm.repeatMode && courseForm.repeatMode !== 'none'" 
              label="重复次数"
              prop="repeatCount"
            >
              <el-input-number
                v-model="courseForm.repeatCount"
                :min="1"
                :max="100"
                :step="1"
                :precision="0"
                style="width: 100%;"
                placeholder="请输入重复次数"
              ></el-input-number>
              <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                将生成{{ courseForm.repeatCount || 0 }}条记录
              </div>
            </el-form-item>
          </el-col>
        </el-row>
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
      // 用户角色标识
      isStudent: false, // 是否为学生角色
      isAdmin: false, // 是否为管理员角色
      currentStudent: null, // 当前登录的学生信息
      
      // 筛选条件
      selectedTeacher: null, // 选中的教师（管理员使用）
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
      isDatePreset: false, // 标记日期是否预设（从日历点击添加）
      
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
        remark: '',
        repeatMode: 'none', // 重复模式：none-不重复, daily-每天, weekly-每周, weekday-工作日
        repeatCount: 1 // 重复次数
      },
      
      // 默认头像
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      
      // 时间选择器的小时和分钟
      selectedHour: null,
      selectedMinute: null,
      
      // 小时选项（0-23）
      hourOptions: Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0')),
      
      // 分钟选项（0、5、10...55，步长为5）
      minuteOptions: Array.from({ length: 12 }, (_, i) => String(i * 5).padStart(2, '0')),
      
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
        duration: [
          { required: true, message: '请输入课时数', trigger: 'blur' }
        ],
        repeatCount: [
          { 
            validator: (rule, value, callback) => {
              if (this.courseForm.repeatMode && this.courseForm.repeatMode !== 'none') {
                if (!value || value < 1) {
                  callback(new Error('请输入重复次数'));
                } else if (value > 100) {
                  callback(new Error('重复次数不能超过100次'));
                } else {
                  callback();
                }
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
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
      return this.courseList.filter(course => Number(course.status) === 2).length;
    },
    
    // 选中学生的课程时长（分钟）
    selectedStudentDuration() {
      if (!this.courseForm.studentId) {
        return 45; // 默认45分钟
      }
      const student = this.studentList.find(s => s.id === this.courseForm.studentId);
      return student && student.classDuration ? student.classDuration : 45;
    },
    
    // 总分钟数
    totalMinutes() {
      const minutes = this.selectedStudentDuration * (this.courseForm.duration || 0);
      // 保留整数，因为分钟数通常不需要小数
      return Math.round(minutes);
    },
    
    // 过滤后的学生列表（添加课程对话框中使用）
    filteredStudentList() {
      // 如果在表单中选择了教师，只显示该教师的学生
      if (this.dialogVisible && this.courseForm.teacherId) {
        return this.studentList.filter(s => s.teacherId === this.courseForm.teacherId);
      }
      // 否则显示所有学生
      return this.studentList;
    }
  },
  
  watch: {
    // 监听学生选择变化，重新计算结束时间并自动设置教师（仅管理员）
    'courseForm.studentId'(newVal) {
      if (newVal) {
        this.calculateEndTime();
        
        // 管理员选择学生后，自动设置该学生的授课教师
        if (this.isAdmin && this.studentList.length > 0) {
          const student = this.studentList.find(s => s.id === newVal);
          if (student && student.teacherId) {
            this.courseForm.teacherId = student.teacherId;
            // 同时设置课程名称
            const teacher = this.teacherList.find(t => t.id === student.teacherId);
            if (teacher && teacher.teach) {
              this.courseForm.courseName = teacher.teach;
            }
          }
        }
      }
    },
    
    // 监听教师选择变化，自动填充课程名称（仅管理员）
    'courseForm.teacherId'(newVal, oldVal) {
      if (this.isAdmin && newVal && this.teacherList.length > 0) {
        const teacher = this.teacherList.find(t => t.id === newVal);
        if (teacher && teacher.teach) {
          this.courseForm.courseName = teacher.teach;
        }
        
        // 如果当前选择的学生不属于新选择的教师，清空学生选择
        if (this.courseForm.studentId && oldVal !== newVal) {
          const currentStudent = this.studentList.find(s => s.id === this.courseForm.studentId);
          if (currentStudent && currentStudent.teacherId !== newVal) {
            this.courseForm.studentId = null;
          }
        }
      }
    }
  },
  
  async mounted() {
    this.checkUserRole();
    this.initSelectedMonth();
    await this.loadInitialData();
    // 等待初始数据加载完成后再加载课程
    this.loadMonthCourses();
  },
  
  methods: {
    // 格式化课时显示（支持小数）
    formatHours(hours) {
      if (!hours || hours === 0) return '0';
      // 如果是整数，显示整数；否则显示一位小数
      return hours % 1 === 0 ? hours.toString() : hours.toFixed(1);
    },
    
    // 根据选中的小时和分钟更新开始时间
    updateStartTime() {
      if (this.selectedHour !== null && this.selectedMinute !== null) {
        // 验证并格式化小时
        let hour = String(this.selectedHour).replace(/\D/g, '');
        if (hour === '') {
          this.selectedHour = null;
          return;
        }
        
        let hourNum = parseInt(hour, 10);
        if (isNaN(hourNum)) {
          this.selectedHour = null;
          this.$message.warning('请输入有效的小时数（0-23）');
          return;
        }
        
        // 限制在0-23范围
        if (hourNum < 0) {
          hourNum = 0;
        } else if (hourNum > 23) {
          hourNum = 23;
          this.$message.warning('小时数已调整为23');
        }
        
        this.selectedHour = String(hourNum).padStart(2, '0');
        
        // 验证并格式化分钟（必须是5的倍数）
        let minute = String(this.selectedMinute).replace(/\D/g, '');
        if (minute === '') {
          this.selectedMinute = null;
          return;
        }
        
        let minuteNum = parseInt(minute, 10);
        if (isNaN(minuteNum)) {
          this.selectedMinute = null;
          this.$message.warning('请输入有效的分钟数（0-55，5的倍数）');
          return;
        }
        
        // 限制在0-55范围
        if (minuteNum < 0) {
          minuteNum = 0;
        } else if (minuteNum > 55) {
          minuteNum = 55;
          this.$message.warning('分钟数已调整为55');
        }
        
        // 调整为5的倍数（四舍五入）
        const originalMinute = minuteNum;
        minuteNum = Math.round(minuteNum / 5) * 5;
        
        // 如果调整后大于55，改为55
        if (minuteNum > 55) {
          minuteNum = 55;
        }
        
        this.selectedMinute = String(minuteNum).padStart(2, '0');
        
        // 如果调整了分钟，给用户提示
        if (originalMinute !== minuteNum) {
          this.$message.info(`分钟已自动调整为：${this.selectedMinute}（5的倍数）`);
        }
        
        // 更新表单
        this.courseForm.startTime = `${this.selectedHour}:${this.selectedMinute}`;
        this.calculateEndTime();
      }
    },
    
    // 解析时间字符串到小时和分钟选择器
    parseTimeToSelectors(timeString) {
      if (!timeString) {
        this.selectedHour = null;
        this.selectedMinute = null;
        return;
      }
      
      try {
        const [hours, minutes] = timeString.split(':');
        this.selectedHour = hours;
        this.selectedMinute = minutes;
      } catch (error) {
        console.error('解析时间失败:', error);
        this.selectedHour = null;
        this.selectedMinute = null;
      }
    },
    
    // 计算结束时间
    calculateEndTime() {
      if (!this.courseForm.startTime || !this.courseForm.duration) {
        this.courseForm.endTime = '';
        return;
      }
      
      try {
        // 解析开始时间
        const [hours, minutes] = this.courseForm.startTime.split(':').map(Number);
        
        // 计算总分钟数
        const totalMinutes = this.selectedStudentDuration * this.courseForm.duration;
        
        // 创建日期对象（使用任意日期，只关心时间）
        const startDate = new Date(2000, 0, 1, hours, minutes);
        
        // 添加分钟数
        const endDate = new Date(startDate.getTime() + totalMinutes * 60000);
        
        // 格式化结束时间
        const endHours = String(endDate.getHours()).padStart(2, '0');
        const endMinutes = String(endDate.getMinutes()).padStart(2, '0');
        
        this.courseForm.endTime = `${endHours}:${endMinutes}`;
      } catch (error) {
        console.error('计算结束时间失败:', error);
        this.courseForm.endTime = '';
      }
    },
    
    // 初始化当前月份
    initSelectedMonth() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      this.selectedMonth = `${year}-${month}`;
    },
    
    // 检查用户角色
    checkUserRole() {
      const studentStr = localStorage.getItem('student');
      const teacherStr = localStorage.getItem('teacher');
      const adminStr = localStorage.getItem('admin');
      
      if (studentStr) {
        // 学生角色
        this.isStudent = true;
        this.isAdmin = false;
        this.currentStudent = JSON.parse(studentStr);
        this.selectedStudent = this.currentStudent.id; // 自动选中当前学生
      } else if (teacherStr) {
        // 教师角色
        this.isStudent = false;
        this.isAdmin = false;
        this.currentTeacher = JSON.parse(teacherStr);
      } else if (adminStr) {
        // 管理员角色
        this.isStudent = false;
        this.isAdmin = true;
      }
    },
    
    // 加载初始数据
    async loadInitialData() {
      await this.loadTeachers(); // 先加载教师列表
      await this.loadStudents(); // 再加载学生列表并匹配教师名称
      await this.loadCourseInfo();
    },
    
    // 加载学生列表
    async loadStudents() {
      try {
        // 如果是学生角色，只加载自己的信息
        if (this.isStudent && this.currentStudent) {
          this.studentList = [this.currentStudent];
          this.enrichStudentData();
          return;
        }
        
        // 教师角色的逻辑 - 使用已经在checkUserRole中设置的currentTeacher
        if (this.currentTeacher) {
          // 只加载当前教师的学生
          const url = `/teacher/students/${this.currentTeacher.id}/1`;
          const res = await this.$axios.get(url);
          if (res.data.code === 0) {
            this.studentList = res.data.data.records || res.data.data || [];
            // 丰富学生数据，添加教师名称
            this.enrichStudentData();
          }
        } else if (this.isAdmin) {
          // 管理员角色
          if (this.selectedTeacher) {
            // 如果选择了教师，只加载该教师的学生
            const url = `/teacher/students/${this.selectedTeacher}/1`;
            const res = await this.$axios.get(url);
            if (res.data.code === 0) {
              this.studentList = res.data.data.records || res.data.data || [];
              // 丰富学生数据，添加教师名称
              this.enrichStudentData();
            }
          } else {
            // 未选择教师，加载所有学生
            const res = await this.$axios.get('/student/students/1');
            if (res.data.code === 0) {
              this.studentList = res.data.data.records || res.data.data || [];
              // 丰富学生数据，添加教师名称
              this.enrichStudentData();
            }
          }
        }
      } catch (error) {
        this.$message.error('加载学生列表失败');
        console.error(error);
      }
    },
    
    // 丰富学生数据（添加教师名称）
    enrichStudentData() {
      this.studentList = this.studentList.map(student => {
        // 根据 teacherId 查找教师名称
        if (student.teacherId && this.teacherList.length > 0) {
          const teacher = this.teacherList.find(t => t.id === student.teacherId);
          student.teacherName = teacher ? teacher.username : '未分配';
        } else {
          student.teacherName = '未分配';
        }
        return student;
      });
    },
    
    // 加载课程信息
    async loadCourseInfo() {
      // 系统不再使用课程信息表，保持兼容返回空列表
      this.courseInfoList = [];
      return [];
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
    
    // 上个月
    prevMonth() {
      const [year, month] = this.selectedMonth.split('-');
      const date = new Date(year, month - 1, 1);
      date.setMonth(date.getMonth() - 1);
      const newYear = date.getFullYear();
      const newMonth = String(date.getMonth() + 1).padStart(2, '0');
      this.selectedMonth = `${newYear}-${newMonth}`;
      this.handleMonthChange();
    },
    
    // 下个月
    nextMonth() {
      const [year, month] = this.selectedMonth.split('-');
      const date = new Date(year, month - 1, 1);
      date.setMonth(date.getMonth() + 1);
      const newYear = date.getFullYear();
      const newMonth = String(date.getMonth() + 1).padStart(2, '0');
      this.selectedMonth = `${newYear}-${newMonth}`;
      this.handleMonthChange();
    },
    
    // 教师选择变化（仅管理员）
    async handleTeacherChange() {
      // 清空已选择的学生
      this.selectedStudent = null;
      // 重新加载该教师的学生列表（等待完成）
      await this.loadStudents();
      // 重新加载课程
      await this.loadMonthCourses();
    },
    
    // 学生选择变化
    handleStudentChange() {
      this.loadMonthCourses();
    },
    
    // 重置搜索条件
    async handleReset() {
      // 清空选中的教师（管理员）
      this.selectedTeacher = null;
      // 清空选中的学生
      this.selectedStudent = null;
      // 重置月份为当前月份
      this.initSelectedMonth();
      // 重置日历显示
      this.calendarValue = new Date();
      // 重新加载学生列表（管理员需要）
      if (this.isAdmin) {
        await this.loadStudents();
      }
      // 重新加载课程数据
      await this.loadMonthCourses();
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
        
        // 学生角色：只查询自己的课程
        if (this.isStudent && this.currentStudent) {
          const res = await this.$axios.get(`/student-course/${this.currentStudent.id}`, {
            params: { startDate, endDate }
          });
          if (res.data && res.data.code === 0) {
            this.courseList = res.data.data || [];
          }
        }
        // 选择了特定学生：只查询该学生的课程
        else if (this.selectedStudent) {
          const res = await this.$axios.get(`/student-course/${this.selectedStudent}`, {
            params: { startDate, endDate }
          });
          if (res.data && res.data.code === 0) {
            this.courseList = res.data.data || [];
          }
        } else {
          // 未选择学生：查询所有学生的课程
          // 教师角色：只查询自己的学生
          // 管理员角色：查询所有学生
          const allCourses = [];
          for (const student of this.studentList) {
            try {
              const res = await this.$axios.get(`/student-course/${student.id}`, {
                params: { startDate, endDate }
              });
              if (res.data && res.data.code === 0 && res.data.data) {
                allCourses.push.apply(allCourses, res.data.data);
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
    
    // 获取课程悬浮提示
    getCourseTooltip(course) {
      let tooltip = `${course.startTime}-${course.endTime} ${this.getStudentName(course.studentId)} (${course.courseName})`;
      if (course.remark) {
        tooltip += `\n备注：${course.remark}`;
      }
      return tooltip;
    },
    
    // 获取课程状态（优先使用后端status：0未开始 1进行中 2已完成 3已取消）
    getCourseStatus(course) {
      if (course && course.status !== undefined && course.status !== null) {
        switch (course.status) {
          case 0: return 'pending';
          case 1: return 'ongoing';
          case 2: return 'completed';
          case 3: return 'canceled';
        }
      }
      const now = new Date();
      const courseDateTime = new Date(`${course.courseDate} ${course.startTime}`);
      const endDateTime = new Date(`${course.courseDate} ${course.endTime}`);
      if (now > endDateTime) return 'completed';
      if (now >= courseDateTime && now <= endDateTime) return 'ongoing';
      return 'pending';
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
      this.isDatePreset = false; // 从顶部按钮添加，需要显示日期选择
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
      this.isDatePreset = true; // 从日历点击添加，日期已预设，不显示日期选择
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
      this.isDatePreset = false; // 编辑模式下显示日期字段
      this.dialogTitle = '编辑课程';
      this.courseForm = Object.assign({}, course);
      // 编辑模式下，重置重复相关字段（不会显示，但确保数据干净）
      this.courseForm.repeatMode = 'none';
      this.courseForm.repeatCount = 1;
      // 解析开始时间到选择器
      this.parseTimeToSelectors(course.startTime);
      this.dialogVisible = true;
      // 编辑时也可以重新计算结束时间
      this.$nextTick(() => {
        this.calculateEndTime();
      });
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
    
    // 生成重复日期列表
    generateRepeatDates(startDate, repeatMode, repeatCount) {
      const dates = [];
      const start = new Date(startDate);
      
      if (repeatMode === 'none') {
        return [startDate];
      }
      
      let currentDate = new Date(start);
      let count = 0;
      
      while (count < repeatCount) {
        if (repeatMode === 'daily') {
          // 每天
          dates.push(this.formatDate(currentDate));
          currentDate.setDate(currentDate.getDate() + 1);
          count++;
        } else if (repeatMode === 'weekly') {
          // 每周
          dates.push(this.formatDate(currentDate));
          currentDate.setDate(currentDate.getDate() + 7);
          count++;
        } else if (repeatMode === 'weekday') {
          // 工作日（周一至周五）
          const dayOfWeek = currentDate.getDay(); // 0=周日, 1=周一, ..., 6=周六
          
          // 如果当前是周末，跳到下一个周一
          if (dayOfWeek === 0) {
            currentDate.setDate(currentDate.getDate() + 1); // 跳到周一
          } else if (dayOfWeek === 6) {
            currentDate.setDate(currentDate.getDate() + 2); // 跳到周一
          }
          
          dates.push(this.formatDate(currentDate));
          // 移动到下一天
          currentDate.setDate(currentDate.getDate() + 1);
          
          // 如果下一天是周末，跳过周末
          const nextDayOfWeek = currentDate.getDay();
          if (nextDayOfWeek === 0) {
            currentDate.setDate(currentDate.getDate() + 1); // 跳到周一
          } else if (nextDayOfWeek === 6) {
            currentDate.setDate(currentDate.getDate() + 2); // 跳到周一
          }
          
          count++;
        }
      }
      
      return dates;
    },
    
    // 格式化日期为 yyyy-MM-dd
    formatDate(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    // 提交课程
    submitCourse() {
      this.$refs.courseForm.validate(async (valid) => {
        if (valid) {
          // 验证结束时间是否已计算
          if (!this.courseForm.endTime) {
            this.$message.error('请先选择开始时间和课时数');
            return;
          }
          
          // 验证时间
          if (this.courseForm.startTime >= this.courseForm.endTime) {
            this.$message.error('结束时间必须晚于开始时间');
            return;
          }
          
          // 如果是编辑模式，直接提交
          if (this.isEdit) {
            this.submitting = true;
            try {
              const res = await this.$axios.post('/student-course/update', this.courseForm);
              
              if (res.data.code === 0) {
                this.$message.success('修改成功');
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
            return;
          }
          
          // 添加模式：检查是否需要批量添加
          const repeatMode = this.courseForm.repeatMode || 'none';
          const repeatCount = this.courseForm.repeatCount || 1;
          
          if (repeatMode === 'none') {
            // 不重复，单次添加
            this.submitting = true;
            try {
              const courseData = Object.assign({}, this.courseForm);
              delete courseData.repeatMode;
              delete courseData.repeatCount;
              
              const res = await this.$axios.post('/student-course/add', courseData);
              
              if (res.data.code === 0) {
                this.$message.success('添加成功');
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
          } else {
            // 批量添加
            const dates = this.generateRepeatDates(
              this.courseForm.courseDate,
              repeatMode,
              repeatCount
            );
            
            if (dates.length === 0) {
              this.$message.error('未能生成有效的课程日期');
              return;
            }
            
            // 批量添加确认提示
            try {
              const modeText = {
                'daily': '每天',
                'weekly': '每周',
                'weekday': '工作日'
              }[repeatMode] || '重复';
              
              const confirmMessage = `确定要批量添加 ${dates.length} 条课程记录吗？\n\n重复模式：${modeText}\n重复次数：${repeatCount}次\n日期范围：${dates[0]} 至 ${dates[dates.length - 1]}`;
              
              await this.$confirm(confirmMessage, '批量添加确认', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
              });
            } catch (e) {
              // 用户取消
              if (e !== 'cancel') {
                this.$message.error('操作失败');
              }
              return;
            }
            
            this.submitting = true;
            let successCount = 0;
            let failCount = 0;
            
            try {
              // 批量提交课程
              for (const date of dates) {
                try {
                  const courseData = {
                    studentId: this.courseForm.studentId,
                    courseName: this.courseForm.courseName,
                    courseDate: date,
                    startTime: this.courseForm.startTime,
                    endTime: this.courseForm.endTime,
                    teacherId: this.courseForm.teacherId,
                    duration: this.courseForm.duration,
                    remark: this.courseForm.remark
                  };
                  
                  const res = await this.$axios.post('/student-course/add', courseData);
                  
                  if (res.data.code === 0) {
                    successCount++;
                  } else {
                    failCount++;
                    console.error(`添加课程失败（日期：${date}）：`, res.data.message);
                  }
                } catch (error) {
                  failCount++;
                  console.error(`添加课程失败（日期：${date}）：`, error);
                }
              }
              
              // 显示结果
              if (successCount === dates.length) {
                this.$message.success(`成功添加 ${successCount} 条课程记录`);
                this.dialogVisible = false;
                this.loadMonthCourses();
              } else if (successCount > 0) {
                this.$message.warning(`成功添加 ${successCount} 条，失败 ${failCount} 条`);
                this.loadMonthCourses();
              } else {
                this.$message.error('所有课程添加失败，请检查后重试');
              }
            } catch (error) {
              this.$message.error('批量添加失败');
              console.error(error);
            } finally {
              this.submitting = false;
            }
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
        remark: '',
        repeatMode: 'none',
        repeatCount: 1
      };
      // 重置时间选择器
      this.selectedHour = null;
      this.selectedMinute = null;
      // 注意：不在这里重置 isDatePreset，由各个打开对话框的方法控制
      if (this.$refs.courseForm) {
        this.$refs.courseForm.resetFields();
      }
    },
    
    // 处理重复模式变化
    handleRepeatModeChange() {
      if (this.courseForm.repeatMode === 'none' || !this.courseForm.repeatMode) {
        this.courseForm.repeatCount = 1;
      } else if (!this.courseForm.repeatCount || this.courseForm.repeatCount < 1) {
        this.courseForm.repeatCount = 1;
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
    
    // 获取状态颜色（同步course.status）
    getStatusColor(course) {
      const status = this.getCourseStatus(course);
      if (status === 'completed') return '#67c23a'; // 已完成：绿色
      if (status === 'ongoing') return '#67c23a';   // 进行中：绿色
      if (status === 'canceled') return '#909399';  // 已取消：灰色
      return '#409eff';                              // 未开始：蓝色
    },
    
    // 获取状态标签类型（同步course.status）
    getStatusTagType(course) {
      const status = this.getCourseStatus(course);
      if (status === 'completed') return 'success'; // 已完成：绿色
      if (status === 'ongoing') return 'success';   // 进行中：绿色
      if (status === 'canceled') return 'info';     // 已取消：灰色
      return '';                                    // 未开始：默认（蓝色）
    },
    
    // 获取状态文本（同步course.status）
    getStatusText(course) {
      const status = this.getCourseStatus(course);
      if (status === 'completed') return '已完成';
      if (status === 'ongoing') return '进行中';
      if (status === 'canceled') return '已取消';
      return '未开始';
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
        },

        // 标记课程为完成（二次确认）
        async markCourseComplete(course) {
          try {
            const studentName = this.getStudentName(course.studentId);
            const dateText = `${course.courseDate}`;
            const timeRangeText = `${course.startTime}-${course.endTime}`;
            const html = `
              <div style="line-height:1.8; font-size:14px; text-align:left;">
                <div style="margin-bottom:6px; color:#606266;">请确认是否将当前课程打卡完成：</div>
                <div><span style="color:#909399;">日期：</span><strong style="color:#303133;">${dateText}</strong></div>
                <div><span style="color:#909399;">时间：</span><strong style="color:#303133;">${timeRangeText}</strong></div>
                <div><span style="color:#909399;">学生：</span><strong style="color:#303133;">${studentName}</strong></div>
                <div><span style="color:#909399;">课程：</span><strong style="color:#303133;">${course.courseName || '-'} </strong></div>
              </div>`;
            await this.$confirm(
              html,
              '确认操作',
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
                dangerouslyUseHTMLString: true,
                center: true,
                closeOnClickModal: false
              }
            );
            const res = await this.$axios.post(`/student-course/${course.id}/complete`);
            if (res.data && res.data.code === 0) {
              this.$message.success('已标记为完成');
              this.loadMonthCourses();
            } else {
              this.$message.error(res.data.message || '操作失败');
            }
          } catch (e) {
            // 取消确认不提示错误，仅在真正请求异常时提示
            if (e && e !== 'cancel' && e !== 'close') {
              this.$message.error('操作失败');
              console.error(e);
            }
          }
        },

        // 取消课程
        async markCourseCancel(course) {
          try {
            const res = await this.$axios.post(`/student-course/${course.id}/cancel`);
            if (res.data && res.data.code === 0) {
              this.$message.success('已取消课程');
              // 关闭课程详情弹窗（如果在详情中触发）
              this.detailDialogVisible = false;
              this.loadMonthCourses();
            } else {
              this.$message.error(res.data.message || '操作失败');
            }
          } catch (e) {
            this.$message.error('操作失败');
            console.error(e);
          }
        }
  }
};
</script>

<style lang="less" scoped>
.training-schedule {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);

  .calendar-section {
    margin-bottom: 20px;
    position: relative;
    
    // 日历头部包装器
    .calendar-header-wrapper {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 0;
      z-index: 5;
      pointer-events: none;
      
      // 左侧控制区
      .calendar-controls {
        position: absolute;
        top: 10px;
        left: 20px;
        display: flex;
        align-items: center;
        gap: 15px;
        pointer-events: auto;
      }
      
      // 月份选择器
      .calendar-month-selector {
        display: flex;
        align-items: center;
        gap: 12px;
        
        /deep/ .el-date-editor {
          width: 280px;
          
          .el-input__inner {
            border: 2px solid #dcdfe6;
            background: #ffffff;
            font-size: 20px;
            font-weight: 600;
            color: #303133;
            padding-left: 45px;
            padding-right: 15px;
            height: 50px;
            line-height: 50px;
            cursor: pointer;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            
            &:hover {
              border-color: #409eff;
              box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
            }
            
            &:focus {
              border-color: #409eff;
              box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
            }
          }
          
          .el-input__prefix {
            left: 12px;
            color: #409eff;
            font-size: 20px;
          }
          
          .el-input__suffix {
            right: 12px;
            font-size: 18px;
          }
        }
        
        .month-nav {
          display: flex;
          gap: 8px;
          
          .el-button {
            width: 48px;
            height: 48px;
            padding: 0;
            border-width: 2px;
            
            i {
              font-size: 18px;
              font-weight: bold;
            }
          }
        }
      }
      
      // 教师选择器（管理员）
      .teacher-selector {
        /deep/ .el-select {
          width: 220px;
          
          .el-input__inner {
            height: 38px;
            line-height: 38px;
            font-size: 15px;
            border-radius: 6px;
          }
          
          .el-input__suffix {
            .el-input__icon {
              line-height: 38px;
            }
          }
        }
      }
      
      // 学生选择器
      .student-selector {
        /deep/ .el-select {
          width: 220px;
          
          .el-input__inner {
            height: 38px;
            line-height: 38px;
            font-size: 15px;
            border-radius: 6px;
          }
          
          .el-input__suffix {
            .el-input__icon {
              line-height: 38px;
            }
          }
        }
      }
      
      // 当前学生信息（学生端显示）
      .current-student-info {
        display: flex;
        align-items: center;
        padding: 8px 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
        
        .student-name {
          font-size: 15px;
          font-weight: 600;
          color: #ffffff;
          letter-spacing: 0.5px;
        }
      }
      
      // 操作按钮
      .action-buttons {
        display: flex;
        gap: 8px;
        
        .el-button {
          width: 38px;
          height: 38px;
          padding: 0;
          
          i {
            font-size: 16px;
          }
        }
      }
      
      // 统计卡片定位到日历标题右侧
      .stats-card {
        position: absolute;
        top: 8px;
        right: 8px;
        display: flex;
        gap: 0;
        padding: 6px 0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6px;
        box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
        pointer-events: auto;
        backdrop-filter: blur(10px);
        
        .stat-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 0 10px;
          border-right: 1px solid rgba(255, 255, 255, 0.25);
          min-width: 55px;
          width: 55px;
          
          &:first-child {
            padding-left: 12px;
          }
          
          &:last-child {
            border-right: none;
            padding-right: 12px;
          }
          
          .stat-label {
            font-size: 10px;
            color: rgba(255, 255, 255, 0.95);
            margin-bottom: 3px;
            white-space: nowrap;
            font-weight: 500;
            letter-spacing: 0.3px;
            text-align: center;
            line-height: 1.2;
          }
          
          .stat-value {
            font-size: 16px;
            font-weight: bold;
            color: #ffffff;
            line-height: 1;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
            text-align: center;
            font-family: 'Arial', 'Microsoft YaHei', sans-serif;
            
            &.completed-count {
              color: #a8ff9e;
              text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
            }
          }
        }
      }
    }
    
    // 优化日历头部样式，为统计卡片留出空间
    /deep/ .el-calendar__header {
      padding: 25px 20px;
      padding-right: 340px; // 为统计卡片留出空间（缩小后）
      padding-left: 750px; // 为左侧控制区留出空间（时间选择器变大）
      border-bottom: 1px solid #ebeef5;
    }
    
    // 隐藏默认的日历标题和按钮
    /deep/ .el-calendar__title {
      display: none;
    }
    
    /deep/ .el-calendar__button-group {
      display: none;
    }
    
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
            }
          }
          
          &.status-ongoing {
            background: #f0f9eb; // 绿色系背景
            border-color: #67c23a;
            
            .course-time {
              color: #67c23a;
            }
            
            &:hover {
              background: #e1f3d8;
            }
          }
          
          &.status-completed {
            background: #f0f9eb; // 已完成：绿色系
            border-color: #67c23a;
            
            .course-time {
              color: #67c23a;
            }
            
            &:hover {
              background: #e1f3d8;
            }
          }

          &.status-canceled {
            background: #f4f4f5; // 已取消：灰色系
            border-color: #909399;
            
            .course-time,
            .course-student {
              color: #909399;
              text-decoration: line-through;
            }
            
            &:hover {
              background: #e9e9eb;
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

          .course-actions {
            display: flex;
            gap: 4px;
            flex-shrink: 0;
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.15s ease-in-out, visibility 0.15s ease-in-out;

            /deep/ .el-button--mini {
              padding: 0 4px;
              font-size: 11px;
              min-width: 22px;
              width: 22px;
              height: 22px;
              display: inline-flex;
              align-items: center;
              justify-content: center;
            }
          }

          &:hover {
            .course-actions {
              opacity: 1;
              visibility: visible;
            }
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
  
  // 结束时间输入框样式
  .end-time-input {
    /deep/ .el-input__inner {
      text-align: center;
      padding-left: 15px;
      padding-right: 15px;
      border-radius: 8px;
    }
  }
  
  // 时间输入框样式
  .time-input-wrapper {
    display: flex;
    align-items: center;
    width: 100%;
    height: 40px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    background-color: #fff;
    transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
    padding: 0 15px;
    box-sizing: border-box;
    overflow: hidden;
    
    &:hover {
      border-color: #c0c4cc;
    }
    
    &:focus-within {
      border-color: #409eff;
    }
    
    .time-hour-select,
    .time-minute-select {
      flex: 1;
      
      /deep/ .el-input {
        .el-input__inner {
          border: none !important;
          padding: 0 !important;
          height: 38px;
          line-height: 38px;
          box-shadow: none !important;
          background-color: transparent !important;
          text-align: center;
          
          &:hover,
          &:focus {
            border: none !important;
            box-shadow: none !important;
          }
        }
        
        .el-input__suffix {
          display: none !important;
        }
        
        .el-input__prefix {
          display: none !important;
        }
      }
      
      /deep/ .el-select__caret {
        display: none !important;
      }
    }
    
    .time-separator {
      color: #606266;
      font-size: 14px;
      font-weight: normal;
      padding: 0 2px;
      user-select: none;
      flex-shrink: 0;
    }
  }
}

// 中等屏幕响应式设计
@media screen and (max-width: 1200px) {
  .training-schedule {
    .calendar-section {
      /deep/ .el-calendar__header {
        padding-right: 280px !important; // 中等屏幕调整右侧padding
        padding-left: 620px !important;
      }
      
      .calendar-header-wrapper {
        .calendar-controls {
          gap: 12px;
        }
        
        .calendar-month-selector {
          gap: 8px;
          
          /deep/ .el-date-editor {
            width: 220px;
            
            .el-input__inner {
              font-size: 16px;
              height: 42px;
              line-height: 42px;
              border-width: 2px;
            }
            
            .el-input__prefix {
              font-size: 18px;
            }
          }
          
          .month-nav {
            gap: 6px;
            
            .el-button {
              width: 40px;
              height: 40px;
              border-width: 2px;
              
              i {
                font-size: 16px;
              }
            }
          }
        }
        
        .teacher-selector {
          /deep/ .el-select {
            width: 180px;
            
            .el-input__inner {
              height: 34px;
              line-height: 34px;
              font-size: 14px;
            }
          }
        }
        
        .student-selector {
          /deep/ .el-select {
            width: 180px;
            
            .el-input__inner {
              height: 34px;
              line-height: 34px;
              font-size: 14px;
            }
          }
        }
        
        .current-student-info {
          padding: 6px 12px;
          
          .student-name {
            font-size: 14px;
          }
        }
        
        .action-buttons {
          gap: 6px;
          
          .el-button {
            width: 34px;
            height: 34px;
            
            i {
              font-size: 14px;
            }
          }
        }
        
        .stats-card {
          gap: 0;
          padding: 5px 0;
          
          .stat-item {
            padding: 0 8px;
            min-width: 50px;
            width: 50px;
            
            &:first-child {
              padding-left: 10px;
            }
            
            &:last-child {
              padding-right: 10px;
            }
            
            .stat-label {
              font-size: 9px;
            }
            
            .stat-value {
              font-size: 14px;
            }
          }
        }
      }
    }
  }
}

// 小屏幕响应式设计
@media screen and (max-width: 768px) {
  .training-schedule {
    padding: 10px;
    
    .calendar-section {
      .calendar-header-wrapper {
        position: relative;
        height: auto;
        margin-bottom: 10px;
        display: flex;
        flex-direction: column;
        gap: 10px;
        
        .calendar-controls {
          position: relative;
          top: 0;
          left: 0;
          flex-direction: column;
          gap: 10px;
          width: 100%;
          
          .calendar-month-selector {
            justify-content: center;
            width: 100%;
            gap: 10px;
            
            /deep/ .el-date-editor {
              flex: 1;
              max-width: 260px;
              
              .el-input__inner {
                font-size: 18px;
                text-align: center;
                height: 46px;
                line-height: 46px;
                border-width: 2px;
              }
              
              .el-input__prefix {
                font-size: 18px;
              }
            }
            
            .month-nav {
              gap: 6px;
              
              .el-button {
                width: 44px;
                height: 44px;
                border-width: 2px;
                
                i {
                  font-size: 16px;
                }
              }
            }
          }
          
          .teacher-selector {
            width: 100%;
            
            /deep/ .el-select {
              width: 100%;
              
              .el-input__inner {
                height: 36px;
                line-height: 36px;
                font-size: 15px;
              }
            }
          }
          
          .student-selector {
            width: 100%;
            
            /deep/ .el-select {
              width: 100%;
              
              .el-input__inner {
                height: 36px;
                line-height: 36px;
                font-size: 15px;
              }
            }
          }
          
          .current-student-info {
            width: 100%;
            padding: 8px 16px;
            justify-content: center;
            
            .student-name {
              font-size: 15px;
            }
          }
          
          .action-buttons {
            justify-content: center;
            
            .el-button {
              width: 36px;
              height: 36px;
              
              i {
                font-size: 15px;
              }
            }
          }
        }
        
        .stats-card {
          position: relative;
          top: 0;
          right: 0;
          gap: 0;
          padding: 6px 0;
          
          .stat-item {
            flex: 1;
            padding: 0 6px;
            min-width: auto;
            width: auto;
            
            &:first-child {
              padding-left: 10px;
            }
            
            &:last-child {
              padding-right: 10px;
            }
            
            .stat-label {
              font-size: 10px;
            }
            
            .stat-value {
              font-size: 15px;
            }
          }
        }
      }
      
      /deep/ .el-calendar__header {
        padding: 8px 10px !important;
      }
    }
  }
}
</style>
