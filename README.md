# lucene-demo

基于`lucene-5.5.4`实现的全文检索demo。

要求：`jdk1.7`，本来是打算用Lucene6.x的，结果发现必须jdk1.8以上，果断放弃改用Lucene5.x了。

详细说明：http://blog.liuxianan.com/lucene-hello-world.html

# 运行

这其实是一个JavaSE项目，非web项目，使用Eclipse打开。这里有2个例子，一个是文件搜索示例，一个是数据库搜索示例。

## 文件搜索

新建目录`E:\lucene`用于存放索引，新建目录`E:\text`，在里面放一些测试文件，最好是文本文件，可以多目录。然后运行`FileSearchDemo.java`即可看到效果。

## 数据库搜索

本地新建一个名为`lucene-demo`的数据库，然后导入本工程根目录的`lucene-demo.sql`，然后新建`E:\lucene-db`目录，打开`DbSearchDemo.java`将数据库用户名密码改成你自己的，然后右键运行即可看到效果。

# 其它说明

使用了`IK Analyzer2012`中文分词工具，这个demo是入门级的，想深入学习的可以跟着这位仁兄的文章去学：http://blog.csdn.net/wuyinggui10000/article/category/3173543
