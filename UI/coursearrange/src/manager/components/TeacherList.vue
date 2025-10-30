<template>
  <div>
    <!-- 功能 -->
    <div class="header-menu">
      <el-input placeholder="搜索讲师" v-model="keyword" @clear="inputListener" clearable>
        <el-button slot="append" type="primary" icon="el-icon-search" @click="searchTeacher">搜索</el-button>
      </el-input>
      <el-button type="primary" @click="addTeacher()" class="addbtn">添加讲师</el-button>
    </div>
    <!-- 讲师列表 -->
    <el-table :data="teacherData" size="mini" :stripe="true" :highlight-current-row="true">
      <el-table-column label="序号" type="selection"></el-table-column>
      <el-table-column prop="teacherNo" label="编号" fixed width="100"></el-table-column>
      <el-table-column prop="username" label="用户名" fixed width="100"></el-table-column>
      <el-table-column prop="realname" label="姓名" fixed width="100"></el-table-column>
      <el-table-column prop="jobtitle" label="职称" fixed width="100"></el-table-column>
      <el-table-column prop="teach" label="学科" fixed width="80"></el-table-column>
      <el-table-column prop="age" label="年龄" fixed width="50"></el-table-column>
      <el-table-column prop="telephone" label="电话" fixed width="100"></el-table-column>
      <el-table-column prop="email" label="邮件" fixed width="150"></el-table-column>
      <el-table-column prop="address" label="地址"></el-table-column>
      <el-table-column prop="license" label="证件照">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.license != null "
            fit="contain"
            style="width: 50px; height: 20px"
            :src="scope.row.license"
            :preview-src-list="scope.row.license.split('#')"
          ></el-image>
          <el-upload
            v-else
            :action="'/teacher/upload/' + scope.row.id"
            :on-success="handleUploadSuccess"
          >
            <i class="el-icon-upload2" style="cursor:pointer"></i>
          </el-upload>
        </template>
      </el-table-column>

      <el-table-column prop="operation" label="操作">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹出表单编辑讲师 -->
    <el-dialog title="编辑讲师" :visible.sync="visibleForm">
      <el-form
        :model="editFormData"
        label-position="left"
        label-width="80px"
        :rules="addTeacherRules"
      >
        <el-form-item label="编号" prop="teacherNo">
          <el-input v-model="editFormData.teacherNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realname">
          <el-input v-model="editFormData.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="职称" prop="jobtitle">
          <el-input v-model="editFormData.jobtitle" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="科目" prop="teach">
          <el-input v-model="editFormData.teach" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="telephone">
          <el-input v-model="editFormData.telephone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮件" prop="email">
          <el-input v-model="editFormData.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editFormData.address" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="editFormData.age" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 弹出表单添加讲师 -->
    <el-dialog title="添加讲师" :visible.sync="visibleAddForm">
      <el-form
        :model="addTeacherForm"
        label-position="left"
        label-width="80px"
        :rules="addTeacherRules"
      >
        <el-form-item label="编号" prop="teacherNo">
          <el-input v-model="addTeacherForm.teacherNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="addTeacherForm.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realname">
          <el-input v-model="addTeacherForm.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="职称" prop="jobtitle">
          <el-input v-model="addTeacherForm.jobtitle" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="科目" prop="teach">
          <el-input v-model="addTeacherForm.teach" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="telephone">
          <el-input v-model="addTeacherForm.telephone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮件" prop="email">
          <el-input v-model="addTeacherForm.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="addTeacherForm.address" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="addTeacherForm.age" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleAddForm = false">取 消</el-button>
        <el-button type="primary" @click="addCommit()">提 交</el-button>
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
  name: "TeacherList",
  data() {
    return {
      teacherData: [],
      keyword: "",
      page: 1,
      pageSize: 10,
      total: 0,
      visibleForm: false,
      visibleAddForm: false,
      editFormData: [],
      addTeacherRules: {
        username: [
          { required: true, message: "请输入昵称", trigger: "blur" },
          { min: 2, max: 12, message: "长度在 2 到 12 个字符", trigger: "blur" }
        ],
        realname: [
          { required: true, message: "请输入真实姓名", trigger: "blur" }
        ],
        jobtitle: [{ required: true, message: "请输入职称", trigger: "blur" }],
        teach: [{ required: true, message: "请输入教授科目", trigger: "blur" }],
        telephone: [
          { required: true, message: "请输入手机号码", trigger: "blur" }
        ],
        email: [{ required: true, message: "请输入邮件", trigger: "blur" }],
        address: [{ required: true, message: "请输入地址", trigger: "blur" }],
        age: [{ required: true, message: "请输入年龄", trigger: "blur" }]
      },
      addTeacherForm: {
        teacherNo: "",
        username: "",
        realname: "",
        jobtitle: "",
        email: "",
        teach: "",
        telephone: "",
        address: "",
        age: ""
      }
    };
  },
  mounted() {
    this.allTeacher();
  },

  methods: {
    addTeacher() {
      // 在弹出添加表单之前从后台获取讲师的编号
      this.$axios
        .get("/teacher/no")
        .then(res => {
          if (res.data.code == 0) {
            let number = parseInt(res.data.message) + 1;
            // 给讲师编号赋值
            this.addTeacherForm.teacherNo = number.toString();
          }
        })
        .catch(error => {});
      this.visibleAddForm = true;
    },

    // 提交添加讲师的表单
    addCommit() {
      console.log(this.addTeacherForm);
      this.$axios
        .post("/teacher/add", this.addTeacherForm)
        .then(res => {
          if (res.data.code == 0) {
            this.allTeacher();
            this.visibleAddForm = false;
            this.$message({ message: "添加讲师成功", type: "success" });
          }
        })
        .catch(error => {
          this.$message.error("添加讲师失败");
        });
    },

    // 提交更新讲师信息
    commit() {
      // let modifyData = this.editFormData
      this.modifyTeacher(this.editFormData);
    },

    inputListener() {
      this.allTeacher();
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v;
      if (this.keyword == "") {
        this.allTeacher();
      } else {
        this.searchTeacher();
      }
    },

    deleteById(index, row) {
      this.deleteTeacherById(row.id);
    },

    editById(index, row) {
      let modifyId = row.id;
      this.editFormData = row;
      this.visibleForm = true;
    },

    /**
     * 根据ID更新讲师
     */
    modifyTeacher(modifyData) {
      this.$axios
        .post("/teacher/modify", modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" });
          this.allTeacher();
          this.visibleForm = false;
        })
        .catch(error => {
          this.$message.error("更新失败");
        });
    },

    /**
     * 关键词搜索讲师
     */
    searchTeacher() {
      this.page = 1;
      this.$axios
        .get(
          "/teacher/search/" +
            this.page +
            "/" +
            this.keyword
        )
        .then(res => {
          console.log(res);
          let ret = res.data.data;
          this.teacherData = ret.records;
          this.total = ret.total;
          this.$message({ message: "查询成功", type: "success" });
        })
        .catch(error => {
          this.$message.error("查询失败");
        });
    },

    /**
     * 根据ID删除讲师
     */
    deleteTeacherById(id) {
      this.$axios
        .delete("/teacher/delete/" + id)
        .then(res => {
          this.allTeacher();
          this.$message({ message: "删除成功", type: "success" });
        })
        .catch(error => {
          this.$message.error("删除失败");
        });
    },

    /**
     * 获取所有讲师，带分页
     */
    allTeacher() {
      this.$axios
        .get("/teacher/query/" + this.page)
        .then(res => {
          let ret = res.data.data;
          this.teacherData = ret.records;
          this.total = ret.total;
          // this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("查询讲师列表失败");
        });
    },
    handleUploadSuccess() {
      this.allTeacher();
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
    min-width: 250px;
    
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
  
  .el-image {
    border-radius: 6px;
    overflow: hidden;
  }
  
  .el-icon-upload2 {
    font-size: 20px;
    color: #6366f1;
    transition: all 0.3s ease;
    
    &:hover {
      color: #4f46e5;
      transform: scale(1.1);
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
      
      .el-input__inner {
        border-radius: 10px;
        border: 1px solid #e5e7eb;
        transition: all 0.3s ease;
        
        &:focus {
          border-color: #6366f1;
          box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
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