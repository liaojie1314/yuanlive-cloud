# yuanlive项目部署文档

## 1. seata 配置

- 首先通过compose.yml启动下载容器 (***注意nacos与seata可能会启动失败，等待几秒即可***)
- 登录[nacos](http://localhost:8034),初始账号密码均为`nacos`
- 在nacos中创建seata的配置
  - 命名空间默认`public`即可
  - Data ID: `seata-server.properties`
  - Group: `SEATA_GROUP`
  - 配置格式: `Properties`
  - 配置内容:
    
    ```properties
    store.mode=db

    store.db.datasource=druid
    store.db.dbType=mysql
    store.db.driverClassName=com.mysql.cj.jdbc.Driver
    store.db.url=jdbc:mysql://mysql:3306/seata?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
    store.db.user=root
    store.db.password=yuanlive
    store.db.globalTable=global_table
    store.db.branchTable=branch_table
    store.db.lockTable=lock_table
    store.db.distributedLockTable=distributed_lock
    store.db.queryLimit=100

    ```
## 2. redis 配置
- 按上述过程打开[nacos](http://localhost:8034)
- 在nacos中添加`redis`配置
  - 命名空间默认`public`
  - Data ID: `redis.yaml`
  - Group: `REDIS_GROUP`
  - 配置格式: `YAML`
  - 配置内容:

    ```yaml
    spring:
    data:
    redis:
    host: localhost
    port: 6378
    timeout: 10s
    database: 0
    lettuce:
    pool:
    max-active: 200
    max-wait: -1ms
    max-idle: 10
    min-idle: 0
    ```

## 3. sa-token 配置
- 按上述过程打开[nacos](http://localhost:8034)
- 在nacos中添加`sa-token`配置
  - 命名空间默认`public`
  - Data ID: `sa-token.yaml`
  - Group: `SA_TOKEN_GROUP`
  - 配置格式: `YAML`
  - 配置内容:

    ```yaml
    sa-token:
    token-name: Authorization
    timeout: 7200
    is-concurrent: false
    is-share: false
    token-style: simple-uuid
    active-timeout: -1
    is-log: true
    ```