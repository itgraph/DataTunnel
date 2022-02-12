## 数据通道

### 1.功能：
提供数据库表访问导出为CSV文件服务

### 2.IDEA 项目测试，配置Tomcat步骤

- 1. IDEA 导入项目后，点击Edit Configurations ， 新建一个Tomcat Server
- 2. server 下 URL 填写如：http://localhost:8080
- 3. deployment 下添加artifact为该项目
- 4. 启动Tomcat后自动跳到首页，说明项目启动OK
- 5. 在浏览器输入如：http://localhost:8080/dataTunnel?sql=select%20*%20from%20t_user&headless，会自动下载文件

