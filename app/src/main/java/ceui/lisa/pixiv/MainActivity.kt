package ceui.lisa.pixiv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import ceui.lisa.pixiv.ui.theme.PixShaftCacheSnifferTheme
import com.blankj.utilcode.util.PathUtils

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixShaftCacheSnifferTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column(
//                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        val cmd = "tar -cvf ${PathUtils.getExternalDownloadsPath()}/cacheSniffer/dt${System.currentTimeMillis()}.tar ${PathUtils.getInternalAppCachePath()}/"
//                        val cmd = "tar --help"
//                        val cmd = "touch ${PathUtils.getExternalDownloadsPath()}/cacheSniffer/dt${System.currentTimeMillis()}.txt"

//                        val cmd = "tar -cvf /sdcard/Download/cacheSniffer/dt${System.currentTimeMillis()}.tar ${PathUtils.getInternalAppCachePath()}/"
//                        val cmd = "touch /sdcard/Download/cacheSniffer/dt${System.currentTimeMillis()}.txt"

                        Text(text = cmd)
                        Text(text = TextUtil(shellExec(cmd)))
//                        TextField(shellExec("tar -cvoz ${PathUtils.getInternalAppCachePath()}/* ${PathUtils.getExternalDownloadsPath()}/dt.tar.gz"),
//                                  {})
//                        Text(text = File("${PathUtils.getInternalAppCachePath()}").listFiles().joinToString("\n"))

                    }
                }
            }
        }
    }
}
