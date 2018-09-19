# IRetry
**IRetry**是一个基于**rxjava**编写的重试封装库
##功能
能够管理**rxjava**重试思路，解决并发请求触发重试逻辑重复调用
例如token的刷新处理，做到n个请求只会触发一个token刷新，然后通知源请求重试

##思维图

 ![Alt](/resource/logic_logo.png)
 
## 使用

 参照**Demo**中 **SimpleNetRetryManager**与**SimpleTokenRetryManager**自己扩展与编写
 demo中也模拟了实际请求或者自己扩展

##扩展（token为例子）

**IRetry**默认引入了**rxjava**和**rxandroid**

IRetryManager是扩展基类,所有扩展需要继承该类 并且完成对应的方法
```java

```
```java
/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/17
 * Function  模仿token失效拦截
 */

public class SimpleTokenRetryManager extends IRetryManager<BaseEntry> {
    //测试使用 正常开发应该自己保存  这里只是为了测试使用
    //外部模仿请求的时候 根据此值来判断
    //1 为成功
    public static int token;
    //随即成功
    Random random = new Random();
    private volatile static SimpleTokenRetryManager tokenManagerDef;

    public static synchronized SimpleTokenRetryManager newInstance() {
        if (tokenManagerDef == null)
            tokenManagerDef = new SimpleTokenRetryManager();
        return tokenManagerDef;
    }

    public SimpleTokenRetryManager() {
        //设置结果回调  保存成功数据超时时间
        setRetryResult(IRetry.makeIRetryResult(5000));
    }

    /**
     * 创建error对象 这个只是用来标记 直接new即可
     */
    @Override
    protected BaseEntry createError() {
        return new BaseEntry();
    }

    /**
     * 创建success对象 这个只是用来标记 直接new即可
     */
    @Override
    protected BaseEntry createSuccess() {
        return new BaseEntry();
    }

    /**
     * 检查结果
     */
    @Override
    public BaseEntry checked(BaseEntry data) throws Exception {
        //这里我只是判断了code  大家可以根据实际情况来分别
        //这里只要抛出异常即可  可以自定义异常
        if (!data.isSuccess()) throw new Exception("token过期");
        return data;
    }

    /**
     * 创建请求
     */
    @Override
    public void createTokenObservableAndSend() {
        //取消上一次监听
        clear();
        addDisposable(Single.create((SingleOnSubscribe<Integer>) emitter -> {
            IRetryLog.d("开始获取token");
            Thread.sleep(random.nextInt(2) * 1000);
            if (random.nextInt(2) == 1) {
                IRetryLog.d("获取token失败");
                emitter.onSuccess(0);
            } else {
                IRetryLog.d("获取token成功");
                emitter.onSuccess(1);
            }
        }).doOnSuccess(s -> {
            if (s == 1) {
                token = 1;
                sendSuccess();
            } else {
                token = 0;
                sendError(new Throwable("获取token失败了"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    /**
     * 拦截异常
     *
     * @param throwable 异常
     * @return 将会触发重新请求
     */
    @Override
    public boolean intercept(Throwable throwable) {
        return throwable.getMessage().equals("token过期");
    }
}

```

##注意

 在使用过程中work方法里面的观察者创建后面不能追加任何操作符,可以在work方法之后追加,如下：
 
 ![Alt](/resource/warning.png)
 
## 引入

* Add it in your root build.gradle at the end of repositories: 
   
   ```

   allprojects {
   	repositories {
   	...
   	maven { url 'https://jitpack.io' }
   	}
  } 
   	
   ```
   
* Add the dependency
	
	```
	dependencies {
	       ...
    	 implementation 'com.github.CFlingchen:IRetry:v1.0'
      }
	
	```
 
