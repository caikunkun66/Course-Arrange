## 后端启动

`mvn spring-boot:run`

## 前端启动

`cd UI/coursearrange`

`npm dev run`


### npm设置镜像

`npm config set registry https://registry.npmmirror.com`

### 版本
- node.js：12.14.0
- jdk:


### 终止后端
```
# 步骤1：查看8008端口被哪些进程占用
netstat -ano | findstr :8008

# 你会看到类似这样的输出：
# TCP    0.0.0.0:8008           0.0.0.0:0              LISTENING       5678
# TCP    [::]:8008              [::]:0                 LISTENING       5678

# 步骤2：杀死PID为5678的进程
taskkill /PID 5678 /F

# 或者直接用一行命令：
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8008') do taskkill /f /pid %a
```

首先安装Chocolatey（如果尚未安装）：

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
使用Chocolatey安装Maven：
```
```powershell
choco install maven
```