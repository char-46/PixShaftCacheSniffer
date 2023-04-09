aka，pixiv在境内没办法正常访问，于是有了一众第三方App能够实现直连pixiv的操作。通过绕过SNI审查的方式实现直连，其中我所提到的Shaft就是其中之一。  
ref: https://github.com/CeuiLiSA/Pixiv-Shaft/issues/243  
ref: https://github.com/CeuiLiSA/Pixiv-Shaft
## TL; DR
 1. Shaft是开源项目，可以直接阅读其源代码。
 2. 根据其源码，得知其使用了`sqlite3`来保存源网址和文件名的对照关系。缓存文件位于`/data`目录下、
 3. 数据库可以通过MIUI的备份和还原提取，但是缓存无法提取。
 4. 修改Shaft源代码，增加通过`tar`命令打包缓存到正常可访问目录的代码。
 5. 编译新版本apk，安装。
 6. 将第五步提取所得到的可读取`tar`文件导出。
