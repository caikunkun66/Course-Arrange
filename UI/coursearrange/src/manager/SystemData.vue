<template>
  <div id="chart" class="wrapper">
    <!-- 系统数据 -->
  </div>
</template>

<script>
export default {
  name: "SystemData",
  data() {
    return {
      sysData: []
    };
  },

  mounted() {
    this.systemData()
  
  },
  computed: {

  },
  methods: {
    // 获取系统数据
    systemData() {
      this.$axios
        .get("/systemdata")
        .then(res => {
          if (res.data.code == 0) {
            let ret = res.data.data
            this.sysData = ret
            this.draw()
          } else {
            console.log(res.data.message);
          }
        })
        .catch(error => {});
    },

    // 画图
    draw(sys) {
      let chart = this.$echarts.init(document.getElementById("chart"));

      chart.setOption({
        title: { 
          text: "系统数据统计",
          textStyle: {
            color: '#1f2937',
            fontSize: 24,
            fontWeight: 600
          },
          left: 'center',
          top: 20
        },
        tooltip: {
          trigger: "axis",
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e5e7eb',
          borderWidth: 1,
          textStyle: {
            color: '#374151'
          },
          axisPointer: {
            type: "shadow",
            shadowStyle: {
              color: 'rgba(99, 102, 241, 0.1)'
            }
          }
        },
        color: ["#667eea"],
        grid: {
          left: "3%",
          right: "4%",
          bottom: "3%",
          top: "80px",
          containLabel: true
        },
        xAxis: [
          {
            type: "category",
            data: ["讲师", "学生", "班级", "教室", "教学楼", "教材", "文档", "网课", "题库", "排课任务", "新增学生", "新增讲师"],
            axisTick: {
              alignWithLabel: true,
              lineStyle: {
                color: '#e5e7eb'
              }
            },
            axisLine: {
              lineStyle: {
                color: '#e5e7eb'
              }
            },
            axisLabel: {
              color: '#6b7280',
              fontSize: 12,
              rotate: 30
            }
          }
        ],
        yAxis: [
          {
            type: "value",
            axisLine: {
              show: false
            },
            axisTick: {
              show: false
            },
            axisLabel: {
              color: '#6b7280',
              fontSize: 12
            },
            splitLine: {
              lineStyle: {
                color: '#f3f4f6',
                type: 'dashed'
              }
            }
          }
        ],
        series: [
          {
            name: "数量",
            type: "bar",
            barWidth: "50%",
            itemStyle: {
              borderRadius: [8, 8, 0, 0],
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: '#667eea'
                }, {
                  offset: 1, color: '#764ba2'
                }]
              }
            },
            emphasis: {
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [{
                    offset: 0, color: '#818cf8'
                  }, {
                    offset: 1, color: '#8b5cf6'
                  }]
                }
              }
            },
            data: [
              this.sysData.teachers,
              this.sysData.students,
              this.sysData.classes,
              this.sysData.classrooms,
              this.sysData.teachbuilds,
              this.sysData.courses,
              this.sysData.docs,
              this.sysData.onlineCourse,
              this.sysData.exercises,
              this.sysData.classtasks,
              this.sysData.studentReg,
              this.sysData.teacherReg,
            ]
          }
        ]
      });
    }
  }
};
</script>

<style lang="less" scoped>
.wrapper {
  margin: 0;
  padding: 24px;
  width: 100%;
  height: calc(100vh - 150px);
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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

#chart {
  width: 100%;
  height: 100%;
}
</style>