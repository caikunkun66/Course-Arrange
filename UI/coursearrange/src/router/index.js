import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/home/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Admin from '@/pages/Admin';
import ManagerMain from '@/manager/ManagerMain';
import CourseTable from '@/manager/components/CourseTable';
import StudentCenter from '@/home/components/StudentCenter';
import StudentPassword from '@/home/components/Password';
import StudentList from '@/manager/components/StudentList';
import UpdatePass from '@/pages/components/UpdatePass';
import StudentMain from '@/home/StudentMain';

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student/login',
      name: 'Login',
      component: Login,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student/register',
      name: 'Register',
      component: Register,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: Admin,
      meta: { 
        noRequireAuth: true
      },
    },
    {
      path: '/student',
      component: StudentMain,
      meta: { 
        noRequireAuth: true
      },
      children: [
        {
          path: '',
          name: 'Student',
          component: CourseTable
        },
        {
          path: '/courseList',
          name: 'CourseList',
          component: CourseTable
        },
        {
          path: '/center',
          name: 'StudentCenter',
          component: StudentCenter
        },
        {
          path: '/password',
          name: 'Password',
          component: StudentPassword
        }
      ]
    },
    {
      path: '/admin',
      name: 'Admin',
      component: ManagerMain,
      redirect: '/coursetable',
      children: [
        {
          path: '/updatepass',
          name: 'UpdatePass',
          component: UpdatePass
        },
        {
          path: '/coursetable',
          name: 'CourseTable',
          component: CourseTable
        },
        {
          path: '/studentlist',
          name: 'StudentList',
          component: StudentList
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (!to.matched.some(res => res.meta.noRequireAuth)) {// 判断是否需要登录权限
    if (window.localStorage.getItem('teacher') || window.localStorage.getItem("admin") ) {// 判断是否登录
      next()
    } else {
      next({
        path: '/',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    next()
  }
})


export default router
