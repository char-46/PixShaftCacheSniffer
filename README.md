aka，pixiv在境内没办法正常访问，于是有了一众第三方App能够实现直连pixiv的操作。通过绕过SNI审查的方式实现直连，其中我所提到的Shaft就是其中之一。
ref: https://github.com/CeuiLiSA/Pixiv-Shaft/issues/243
ref: https://github.com/CeuiLiSA/Pixiv-Shaft

## 背景介绍
有一台旧手机，里面有一些Shaft的缓存的图片和小说，解了bl锁但是没有root，没有办法直接获取缓存文件。
## 过程简介（仅包含原理） TL; DR
 1. Shaft是开源项目，可以直接阅读其源代码。
 2. 根据其源码，得知其使用了`sqlite3`来保存源网址和文件名的对照关系。缓存文件位于`/data`目录下、
 3. 数据库可以通过MIUI的备份和还原提取，但是缓存无法提取。
 4. 修改Shaft源代码，增加通过`tar`命令打包缓存到正常可访问目录的代码。
 5. 编译新版本apk，安装。
 6. 将第五步提取所得到的可读取`tar`文件导出。
至此，缓存提取完成。
## 详解
### 1. 分析
分析其通过MIUI备份提取出的文件，可以得到多个数据库，如下：
 + `google_app_measurement_local.db`，
 + `roomDemo-database`。
其中，`roomDemo-database`包含了文件名与网址的对应关系，较为重要。
该数据库架构如下：
```
CREATE TABLE android_metadata (locale TEXT)
CREATE TABLE `feature_table` (`uuid` TEXT NOT NULL, `dateTime` INTEGER NOT NULL, `starType` TEXT NOT NULL, `userID` INTEGER NOT NULL, `illustID` INTEGER NOT NULL, `illustTitle` TEXT NOT NULL, `isShowToolbar` INTEGER NOT NULL, `name` TEXT NOT NULL, `dataType` TEXT NOT NULL, `illustJson` TEXT NOT NULL, `seriesId` INTEGER NOT NULL, PRIMARY KEY(`uuid`))
CREATE TABLE `illust_download_table` (`fileName` TEXT NOT NULL, `filePath` TEXT, `taskGson` TEXT, `illustGson` TEXT, `downloadTime` INTEGER NOT NULL, PRIMARY KEY(`fileName`))
CREATE TABLE `illust_downloading_table` (`fileName` TEXT NOT NULL, `uuid` TEXT, `taskGson` TEXT, PRIMARY KEY(`fileName`))
CREATE TABLE `illust_recmd_table` (`illustID` INTEGER NOT NULL, `illustJson` TEXT, `time` INTEGER NOT NULL, PRIMARY KEY(`illustID`))
CREATE TABLE `illust_table` (`illustID` INTEGER NOT NULL, `illustJson` TEXT, `time` INTEGER NOT NULL, `type` INTEGER NOT NULL, PRIMARY KEY(`illustID`))
CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)
CREATE TABLE `search_table` (`id` INTEGER NOT NULL, `keyword` TEXT, `searchTime` INTEGER NOT NULL, `searchType` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, PRIMARY KEY(`id`))
CREATE TABLE `tag_mute_table` (`id` INTEGER NOT NULL, `tagJson` TEXT, `searchTime` INTEGER NOT NULL, `type` INTEGER NOT NULL, PRIMARY KEY(`id`, `type`))
CREATE TABLE `upload_image_table` (`id` INTEGER NOT NULL, `fileName` TEXT, `filePath` TEXT, `uploadTime` INTEGER NOT NULL, PRIMARY KEY(`id`))
CREATE TABLE `user_table` (`userID` INTEGER NOT NULL, `userGson` TEXT, `loginTime` INTEGER NOT NULL, PRIMARY KEY(`userID`))
CREATE TABLE `uuid_list_table` (`uuid` TEXT NOT NULL, `listJson` TEXT, PRIMARY KEY(`uuid`))
```
对我们有用的是`illust_recmd_table`，里面包含了根据pixiv id向`https://www.pixiv.net/ajax/illust/{id}`发送请求所返回的JSON。~~*（捂脸）我初三的时候写的代码质量好高*~~ btw，我不确定这个URL现在还能不能使用。
水平有限，结合代码能从数据库里面得到的有效信息只有这些。
### 2. 提取
因为项目开源的时候包含了其签名文件，避免了很多不必要的麻烦。从build.gradle可以得知签名文件的信息。
签名文件自c227b1b3a3a494a1688a11f8070fc4b4f8d0d23d加入。
> ref: https://github.com/CeuiLiSA/Pixiv-Shaft/blob/c227b1b3a3a494a1688a11f8070fc4b4f8d0d23d/keystore.jks
> 3.2.1
>  

签名文件信息在e7e4ce0dd7e5c8a38506fc38077ba6ff9ce754ac加入。
```
...
    signingConfigs {
        release {
            storeFile file("../keystore.jks")
            storePassword "123456"
            keyAlias 'CeuiLiSA'
            keyPassword '123456'
        }
    }
...
```
> ref: https://github.com/CeuiLiSA/Pixiv-Shaft/blob/e7e4ce0dd7e5c8a38506fc38077ba6ff9ce754ac/app/build.gradle
> 3.2.1
> 

据此，我们可以利用这个签名文件对手上的包名相同的包签名进行直接安装。
另，有以下方法。须通过打开手机的USB调试选项并通过adb连接到手机。
```
pm uninstall -k <package name> # 移除软件包后保留数据和缓存目录。
pm install -r <apk file>       # 重新安装现有应用，并保留其数据。
```
另，`pm install`有以下选项值得注意。
`-t`：允许安装测试 APK。仅当您运行或调试了应用或者使用了 Android Studio 的 Build > Build APK 命令时，Gradle 才会生成测试 APK。如果是使用开发者预览版 SDK 构建的 APK，那么安装测试 APK 时必须在 install 命令中包含 -t 选项。
`-d`：允许版本代码降级。
ref: https://developer.android.google.cn/studio/command-line/adb?hl=zh-cn

因为缓存文件数量较大，所以我使用应用程序代码执行tar命令打包到可被用户直接读取的位置后复制到电脑上。
执行命令的kotlin方法如下。
```
fun shellExec(cmd: String?): Any {
    val mRuntime = Runtime.getRuntime() //执行命令的方法
    try {
        //Process中封装了返回的结果和执行错误的结果
        val mProcess = mRuntime.exec(cmd) //加入参数
        //使用BufferReader缓冲各个字符，实现高效读取
        //InputStreamReader将执行命令后得到的字节流数据转化为字符流
        //mProcess.getInputStream()获取命令执行后的的字节流结果
        val mReader = BufferedReader(InputStreamReader(mProcess.inputStream))
        //实例化一个字符缓冲区
        val mRespBuff = StringBuffer()
        //实例化并初始化一个大小为1024的字符缓冲区，char类型
        val buff = CharArray(1024)
        var ch = 0
        //read()方法读取内容到buff缓冲区中，大小为buff的大小，返回一个整型值，即内容的长度
        //如果长度不为null
        while (mReader.read(buff).also { ch = it } != -1) {
            //就将缓冲区buff的内容填进字符缓冲区
            mRespBuff.append(buff, 0, ch)
        }
        //结束缓冲
        mReader.close()
        Log.i("shell", "shellExec: $mRespBuff")
        //弹出结果
//            Log.i("shell", "执行命令: " + cmd + "执行成功");
        return mRespBuff
    } catch (e: IOException) {
        // 异常处理
        // TODO Auto-generated catch block
        e.printStackTrace()
        return "e:\n${e}"
    }
}
```
ref: https://github.com/char-46/PixShaftCacheSniffer/blob/master/app/src/main/java/ceui/lisa/pixiv/ShellExec.kt
需执行的命令如下。
```
tar cvvvf /path/to/target.tar ${PathUtils.getInternalAppCachePath()}
```
