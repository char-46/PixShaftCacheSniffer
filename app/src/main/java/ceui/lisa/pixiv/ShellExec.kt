package ceui.lisa.pixiv

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 执行 adb 命令
 *
 * @param cmd 命令
 * @return
 */
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