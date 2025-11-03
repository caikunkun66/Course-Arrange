<template>
  <div class="wrapper">
    <!-- 装饰性背景 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <header class="hero">
      <div class="logo-badge">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="title">Course-Arrange</h1>
      <p class="subtitle">智能排课系统 · 让教学管理更简单</p>
      <div class="features">
        <div class="feature-item">
          <span class="feature-icon">⚡</span>
          <span>高效排课</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">🎯</span>
          <span>精准匹配</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">🔒</span>
          <span>安全可靠</span>
        </div>
      </div>
      <div class="actions">
        <router-link to="admin/login?type=admin" class="btn admin">
          <span class="btn-icon">👨‍💼</span>
          <span>管理员入口</span>
        </router-link>
        <router-link to="admin/login?type=teacher" class="btn teacher">
          <span class="btn-icon">👨‍🏫</span>
          <span>教师入口</span>
        </router-link>
        <router-link to="student/login" class="btn student">
          <span class="btn-icon">🎓</span>
          <span>学生入口</span>
        </router-link>
      </div>
    </header>

    <footer class="footer">
      <router-link to="/" class="link">联系作者</router-link>
      <span class="separator">·</span>
      <span class="copy">© 2020–2025 Course Arrange System</span>
    </footer>
  </div>
  
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style lang="less" scoped>
  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-20px); }
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.05); }
  }

  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  .wrapper {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding: 48px 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
    position: relative;
    overflow: hidden;
  }

  .bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 0;
    overflow: hidden;

    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
    }

    .circle-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
      animation: float 8s ease-in-out infinite;
    }

    .circle-2 {
      width: 300px;
      height: 300px;
      bottom: -80px;
      left: -80px;
      animation: float 10s ease-in-out infinite reverse;
    }

    .circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 10%;
      animation: float 12s ease-in-out infinite;
    }
  }

  .hero {
    text-align: center;
    margin-top: 40px;
    position: relative;
    z-index: 1;
    animation: fadeInUp 0.8s ease-out;
  }

  .logo-badge {
    width: 80px;
    height: 80px;
    margin: 0 auto 24px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    animation: pulse 3s ease-in-out infinite;

    svg {
      width: 48px;
      height: 48px;
      color: #ffffff;
    }
  }

  .title {
    font-size: 48px;
    font-weight: 800;
    letter-spacing: -0.5px;
    margin: 0 0 16px 0;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    background: linear-gradient(to right, #ffffff, #e0e7ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .subtitle {
    color: rgba(255, 255, 255, 0.9);
    margin: 0 0 40px 0;
    font-size: 18px;
    font-weight: 400;
  }

  .features {
    display: flex;
    gap: 32px;
    justify-content: center;
    margin-bottom: 48px;
    flex-wrap: wrap;
  }

  .actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
    max-width: 800px;
    margin: 0 auto;
  }

  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 14px 28px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 15px;
    text-decoration: none;
    transition: all .3s ease;
    border: 2px solid transparent;
    position: relative;
    overflow: hidden;
    flex: 1;
    min-width: 180px;

    .btn-icon {
      font-size: 22px;
    }

    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 0;
      height: 0;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.15);
      transform: translate(-50%, -50%);
      transition: width 0.6s, height 0.6s;
    }

    &:hover::before {
      width: 300px;
      height: 300px;
    }

    span {
      position: relative;
      z-index: 1;
    }
  }

  .btn.admin {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    border-color: rgba(255, 255, 255, 0.3);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 30px rgba(102, 126, 234, 0.5);
      border-color: rgba(255, 255, 255, 0.6);
    }

    &:active {
      transform: translateY(-1px);
    }
  }

  .btn.teacher {
    background: rgba(255, 255, 255, 0.95);
    color: #667eea;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25);
      background: #ffffff;
    }

    &:active {
      transform: translateY(-1px);
    }
  }

  .btn.student {
    background: transparent;
    color: #ffffff;
    border-color: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      border-color: #ffffff;
      transform: translateY(-3px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
    }

    &:active {
      transform: translateY(-1px);
    }
  }

  .footer {
    text-align: center;
    color: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    gap: 12px;
    position: relative;
    z-index: 1;
    flex-wrap: wrap;
    justify-content: center;
  }

  .separator {
    color: rgba(255, 255, 255, 0.5);
  }

  .link {
    color: #ffffff;
    text-decoration: none;
    font-weight: 500;
    transition: all 0.3s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 2px;
      background: #ffffff;
      transition: width 0.3s ease;
    }

    &:hover::after {
      width: 100%;
    }

    &:hover {
      opacity: 0.9;
    }
  }

  .copy {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .title {
      font-size: 36px;
    }

    .subtitle {
      font-size: 16px;
    }

    .features {
      gap: 16px;
      
      .feature-item {
        font-size: 13px;
        padding: 10px 16px;
      }
    }

    .actions {
      flex-direction: column;
      width: 100%;
      max-width: 320px;
      gap: 12px;
    }

    .btn {
      width: 100%;
      min-width: unset;
      padding: 16px 24px;
    }

    .bg-decoration {
      .circle-1 {
        width: 250px;
        height: 250px;
      }

      .circle-2 {
        width: 200px;
        height: 200px;
      }

      .circle-3 {
        display: none;
      }
    }
  }

  @media (min-width: 769px) and (max-width: 1024px) {
    .actions {
      max-width: 700px;
    }

    .btn {
      min-width: 200px;
    }
  }
</style>