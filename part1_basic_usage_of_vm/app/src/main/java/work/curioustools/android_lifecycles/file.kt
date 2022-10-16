package work.curioustools.android_lifecycles

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources.Theme
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

@SuppressLint("UseRequireInsteadOfGet")
fun file(context: Context, activity: AppCompatActivity, application: Application, view: View,
         fragment: Fragment, service: Service, broadcastReceiver: BroadcastReceiver,
         contentProvider: ContentProvider
){
    val ctx = context
    val ctxsAppCtx = context.applicationContext

    val viewCtx : Context = view.context

    // activity is a context as well as have context variables
    val activityAsCtx: Context = activity
    val activitysBaseCtx: Context = activity.baseContext
    val activitysAppCtx : Context = activity.applicationContext
    val activitysAppAsCtx : Context = activity.application

    // application is a context as well as have context variables
    val appAsCTx : Context = application
    val appsAppCtx: Context = application.applicationContext
    val appsBaseCtx: Context = application.baseContext

    // service is a context as well as have context variables
    val serviceAsContext: Context = service
    val servicesBaseCtx: Context = service.baseContext
    val servicesAppCtx : Context = service.applicationContext
    val servicesAppAsCtx : Context = service.application


    // fragment CANNOT BE CASTED as Context but has context variables
    val fragmentAsCtx : Context? = null //NOT POSSIBLE
    val fragmentCtx1 : Context? = fragment.context
    val fragmentCtx2: Context = fragment.requireContext()
    fragment.activity

    // broadcast reciever does NOT have access to context
    val broadcastReceiverCtx : Context? = null //broadcastReceiver.NOTHING

    // contentProvider : similar to  fragment, it CANNOT BE CASTED as Context but has context variables
    val cpAsCtx : Context? = null //NOT POSSIBLE
    val cpCtx1 : Context? = contentProvider.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val cpCtx2: Context = contentProvider.requireContext()
    }


}

abstract class Context{
    open fun abc(){}
    open fun getTheme():Theme? = null
}
open class ContextWrapper : work.curioustools.android_lifecycles.Context()
open class  ContextThemedWrapper : work.curioustools.android_lifecycles.ContextWrapper(){
    override fun getTheme(): Theme?  = Theme::class.objectInstance
}
class Actvity : ContextThemedWrapper()
class App : work.curioustools.android_lifecycles.ContextWrapper()
