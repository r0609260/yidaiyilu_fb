将websiteName读入时

1. 加入所有的external lib（如果更换新电脑）

2. table中的ispage变量需要手动修改

3. 新建table时，schema名字为fb_crawl,存储websiteName的table为table_name
Utf8 的设置详见
https://stackoverflow.com/questions/22572558/how-to-set-character-set-database-and-collation-database-to-utf8-in-my-ini


steps：

1. 将driver路径改为webdriver所在路径

2. 运行