<template>
  <div class="wrapper">
    <header class="hero">
      <h1 class="title">Course-Arrange</h1>
      <p class="subtitle">高效 · 简洁 · 可靠</p>
      <div class="actions">
        <router-link to="admin/login" class="btn primary">管理员/讲师入口</router-link>
        <router-link to="student/login" class="btn ghost">学生入口</router-link>
      </div>
    </header>

    <footer class="footer">
      <router-link to="/" class="link">联系作者</router-link>
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
  .wrapper {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding: 48px 24px;
    background: linear-gradient(180deg, #f9fafb 0%, #ffffff 100%);
    color: #0f172a;
  }

  .hero {
    text-align: center;
    margin-top: 40px;
  }

  .title {
    font-size: 36px;
    font-weight: 700;
    letter-spacing: .5px;
    margin: 0 0 8px 0;
  }

  .subtitle {
    color: #64748b;
    margin: 0 0 24px 0;
  }

  .actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }

  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 18px;
    border-radius: 8px;
    font-weight: 600;
    text-decoration: none;
    transition: all .2s ease;
    border: 1px solid transparent;
  }

  .btn.primary {
    background: #2563eb;
    color: #ffffff;
  }

  .btn.primary:hover {
    background: #1d4ed8;
    transform: translateY(-1px);
  }

  .btn.ghost {
    background: transparent;
    color: #2563eb;
    border-color: #2563eb;
  }

  .btn.ghost:hover {
    background: rgba(37, 99, 235, 0.06);
    transform: translateY(-1px);
  }

  .footer {
    text-align: center;
    color: #94a3b8;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .link {
    color: #0ea5e9;
    text-decoration: none;
  }

  .link:hover {
    text-decoration: underline;
  }

  .copy {
    font-size: 12px;
  }
</style>