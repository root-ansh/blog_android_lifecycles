# Context

(PS: just found [another good article on this topic](https://web.archive.org/web/20150329210012/https://possiblemobile.com/2013/06/context/),  you might wanna read that if you don't udnerstand the stuff below)

<details>
<summary>Android provides a lot of places for accessing context : </summary>

```kotlin
fun file(context: Context, activity: AppCompatActivity, application: Application, view: View, 
         fragment: Fragment, service: Service, broadcastReceiver: BroadcastReceiver,
         contentProvider: ContentProvider
){
    val ctx = context
    val ctxsAppCtx = context.applicationContext

    val viewCtx : Context = view.context

    // activity is a context as well as have context variables
    val activityAsCtx:Context = activity
    val activitysBaseCtx:Context = activity.baseContext
    val activitysAppCtx : Context = activity.applicationContext
    val activitysAppAsCtx : Context = activity.application

    // application is a context as well as have context variables
    val appAsCTx :Context = application
    val appsAppCtx:Context = application.applicationContext
    val appsBaseCtx: Context = application.baseContext

    // service is a context as well as have context variables
    val serviceAsContext:Context = service
    val servicesBaseCtx:Context = service.baseContext
    val servicesAppCtx : Context = service.applicationContext
    val servicesAppAsCtx : Context = service.application

  
    // fragment CANNOT BE CASTED as Context but has context variables
    val fragmentAsCtx :Context? = null //NOT POSSIBLE
    val fragmentCtx1 :Context = fragment.context
    val fragmentCtx2: Context = fragment.requireContext()

    // broadcast reciever does NOT have access to context
    val broadcastReceiverCtx : Context? = null //broadcastReceiver.NOTHING
    
    // contentProvider : similar to  fragment, it CANNOT BE CASTED as Context but has context variables
    val cpAsCtx :Context? = null //NOT POSSIBLE
    val cpCtx1 :Context = contentProvider.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val cpCtx2: Context = contentProvider.requireContext()
    }
    
    // and many more
}

```

</details>

but what exactly is a context?

Well as per definition of context, a context :
1. is an **abstract class** which is **initialised by Android OS at runtime** and 
2. **provides access to various OS Resources and operations**(resources like databases, sensor data, process info, system setting etc and operations like launching an intent to open an activity or system component), and
3. **provides access to various app Resources**(like bundled images and assets)

Important point to note here is that **it is created by the OS**, meaning that its lifecycle is managed by the OS itself and **we don't have control over when it is available to be accessed & used and when it isn't** .

A system process called ActivityManagerService creates it for each of the activity and controls its garbage collection. Similarly it creates one for the application class too. in terms of classes, here is an essential hierarchy of how  context , activity and application are related : 

```kotlin
abstract class Context{
    open fun abc(){}
    open fun getTheme():Theme? = null
}
open class ContextWrapper : Context()
open class  ContextThemedWrapper : ContextWrapper(){
    override fun getTheme(): Theme?  = Theme()
}
class Actvity : ContextThemedWrapper()
class App : ContextWrapper()
```

as you see:
- context is an abstract class, meaning the value returned from the functions will be determined by the child instance.
- application and activity classes are also inherting from the context itself that is why we can cast them as context.
- The only difference seems to be the fact that Activity has values setted up for themes, drawables etc. (***This is why we use activity context for [inflating dialog boxes](allowed_to_launch.png) and views, and not application context***). 
- This image and this [article](https://medium.com/android-news/android-application-launch-explained-from-zygote-to-your-activity-oncreate-8a8f036864b) might give more context on how android launches an application/activity from zygote:

![launching an app](app_nd_activity_launch_process.png)

- Essentially the `AcitivityManagerService` class might be calling the 2 calls `val app:Context = com.abc.Application()` and `val activity:Context = com.abc.LaunchActivity()` one by one, in the same process, and we end up getting app and activity running in the same process. 
- And since AMS is controlling and killing activities, therefore context associated with activities and its subsidiaries(fragments and views) are all controlled by AMS and once should not be storing them to prevent memory leaks. Read more about ActivityManagerService here : https://wongzhenyu.cn/2019/10/03/deep-understanding-of-Android-ActivityManagerService/ (Note: ***this is a powerful article and can clear a lot of your doubts if you read it in depth***)

So here are a dew of my claims with regards to the previous example:

<details>
<summary>The example : </summary>

```kotlin
fun file(context: Context, activity: AppCompatActivity, application: Application, view: View, 
         fragment: Fragment, service: Service, broadcastReceiver: BroadcastReceiver,
         contentProvider: ContentProvider
){
    val ctx = context
    val ctxsAppCtx = context.applicationContext

    val viewCtx : Context = view.context

    // activity is a context as well as have context variables
    val activityAsCtx:Context = activity
    val activitysBaseCtx:Context = activity.baseContext
    val activitysAppCtx : Context = activity.applicationContext
    val activitysAppAsCtx : Context = activity.application

    // application is a context as well as have context variables
    val appAsCTx :Context = application
    val appsAppCtx:Context = application.applicationContext
    val appsBaseCtx: Context = application.baseContext

    // service is a context as well as have context variables
    val serviceAsContext:Context = service
    val servicesBaseCtx:Context = service.baseContext
    val servicesAppCtx : Context = service.applicationContext
    val servicesAppAsCtx : Context = service.application

  
    // fragment CANNOT BE CASTED as Context but has context variables
    val fragmentAsCtx :Context? = null //NOT POSSIBLE
    val fragmentCtx1 :Context = fragment.context
    val fragmentCtx2: Context = fragment.requireContext()

    // broadcast reciever does NOT have access to context
    val broadcastReceiverCtx : Context? = null //broadcastReceiver.NOTHING
    
    // contentProvider : similar to  fragment, it CANNOT BE CASTED as Context but has context variables
    val cpAsCtx :Context? = null //NOT POSSIBLE
    val cpCtx1 :Context = contentProvider.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val cpCtx2: Context = contentProvider.requireContext()
    }
    
    // and many more
}

```

</details>

Note that i am not 100% sure about them:
1. application context is different than activity context 
2. application context should only be used to request system services and  networking / initialising 3rd party non ui libs. it will also remain in memory as long as app is running in foreground/background

3. all other components that are calling `xyz.applicationContext` will have the same instance and it will be that of application or null (determined by AMS)
4. `activity`/`view.context`/`fragment.context` context will have the value of activity instance  or null (determined by AMS). this context will contain theme values and therefore should be able to inflate views/fragments and access resources
5. abc.baseContext is something i am still not fully sure about, just read the most stuff about it here: https://stackoverflow.com/questions/51759985/what-is-the-role-of-attachbasecontext/51760058 . i also read somewhere that it is the base implementation of Context i.e an instance of `ContextImpl` .Didn't find any such class in aosp, but it might be a os class. So whatever it is, i think it has even lesser features setup than an instance of  `ContextWrapper`


Next we will look into launch modes and activity lifecycles