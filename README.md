## 前言
该项目由[https://github.com/yaowen369/DownloadHelper](https://github.com/yaowen369/DownloadHelper) fork而来，并在最大程度保有原项目代码及用法的基础上做了一些调整、修改，修改内容如下：
+ 取消广播，改用接口回调进行下载监听
+ 增加取消下载功能
+ 增加通知栏显示下载进度条

## 功能特性

+ 断点续传
+ 支持多线程 *(目前版本仅支持单任务单线程，多任务才多线程，未来会继续完善单任务的多线程执行)*

## 使用本项目的理由

- **可靠稳定** *(我们拥有近百万用户的某个app项目，迭代了近二十个版本，该下载模块久经考验,工作正常)*
*(修改者os：由于该模块被本人进行了修改，未经过市场的真正考验，所以其稳定性可能不能与原项目相提并论)*
- **体积很小** *(总计只有数十个java文件)*
- **无其他依赖** *(仅使用sdk本身的api，没有依赖任何第三方库)*
- **接入方式简单**

![download_demo](https://github.com/yaowen369/DownloadHelper/blob/master/docs/img/download_three.gif)

## 使用方式
#### 1, **注册**
`AndroidManifest.xml`中需要注册权限和`service`.(读写文件的权限要在代码中动态申请，不要忘记这点了)
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

```

#### 2,  **开始/暂停/取消/重启 下载任务。**

+ 开始,可传入监听回调，或者null

```java
//先获得这个单例对象
        mDownloadHelper = DownloadHelper.getInstance();

//执行两个下载任务
        mDownloadHelper.addTask(firstUrl, firstFile,new FirstDownloadListener())
                .addTask(secondUrl, secondFile,new SecondDownloadListener())
                .submit(this);

//当然，下面这样分开写，自然也可以
       mDownloadHelper.addTask(firstUrl, firstFile,new FirstDownloadListener())
                .submit(this);

        mDownloadHelper.addTask(secondUrl, secondFile, new SecondDownloadListener())
                .submit(this);
```

+ 暂停,不需要传入监听回调
```java
        mDownloadHelper.pauseTask(firstUrl, firstFile)
                .submit(this);
```
+ 取消,不需要传入监听回调
```java
        mDownloadHelper.cancleTask(firstUrl, firstFile)
                .submit(this);
```

+ 重启

当下载任务被暂停/结束后，想要重新启动时，和开始下载操作相同，直接 `addTask().submit`即可从上一次下载断点处开始下载。

+ 开启通知栏显示下载，用法很简单
注意：1，addTask的时候，参数有四个，第一个为下载的链接，第二个为存放文件，第三个则为通知显示栏的title，第四个则为通知显示栏的content，均为字符串类型；
2，提交任务使用submitNotify而不是submit，这两个提交方式决定两种不同的启动服务的方式，submit为绑定服务，submitNotify为启动服务，不与页面绑定。
3，不论是submit或是submitNotify哪种提交任务，需要暂停/取消任务的时候，如上文所示，正常的使用即可

```
//执行两个下载任务
        mDownloadHelper.addTask(firstUrl, firstFile, firstName, "")
                .addTask(secondUrl, secondFile,secondName, "")
                .submitNotify(this);
```

当下载任务被暂停/结束后，想要重新启动时，和开始下载操作相同，直接 `addTask().submit`即可从上一次下载断点处开始下载。


> 当我们执行多个下载任务时，内部会维护任务列表。线程池维护多个线程，执行下载任务。 单个任务只采用单一线程执行任务。

#### 3，上文所提及的监听回调，示例如下：
```
class FirstDownloadListener implements DownloadListener {


        @Override
        public void onPepare() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstTitle.setText("准备下载");
                }
            });
        }

        @Override
        public void onWait() {

        }

        @Override
        public void onLoading(final FileInfo fileInfo) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateTextview(firstTitle, firstProgressBar, fileInfo, firstName, firstBtn);
                }
            });
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstTitle.setText("pause");
                    firstTitle.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onComplete() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstBtn.setText("下载完成");
                    firstBtn.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onCanceled() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstBtn.setText(START);
                    firstBtn.setBackgroundResource(R.drawable.shape_btn_blue);
                    firstTitle.setText("cancle");
                    firstProgressBar.setProgress(0);
                }
            });
        }
    }
```

> 使用方式很简单，这里有两个demo。[执行三个下载任务](https://github.com/yaowen369/DownloadHelper/blob/master/sample/src/main/java/com/yaoxiaowen/download/sample/MainActivity.java)

## API和相关常量
#### **1. DownloadHelper.java**
```java
package com.yaoxiaowen.download;
/**
 * 该类采用 单例 设计模式
 */
public class DownloadHelper {
    /**
     * 提交  下载/暂停  等任务.(提交就意味着开始执行生效)
     * @param context
     */
    public synchronized void submit(Context context);

/**
     * 提交  下载  等任务.(提交就意味着开始执行生效)
     * 通知栏会提示
     *
     * @param context
     */
    public synchronized void submitNotify(Context context);
   /**
     * 添加 新的下载任务
     *
     * @param url              下载的url
     * @param file             存储在某个位置上的文件
     * @param downloadListener 下载过程的监听接口
     * @return DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper addTask(String url, File file, DownloadListener downloadListener);
/**
     * 添加 新的下载任务，主要配合submitNotify进行使用
     *
     * @param url              下载的url
     * @param file             存储在某个位置上的文件
     * @param contentTitle     通知栏显示的title
     * @param contentText      通知栏显示的content
     * @return DownloadHelper  自身 (方便链式调用)
     */
    public DownloadHelper addTask(String url, File file, CharSequence contentTitle, CharSequence contentText) ;
   /**
     * 暂停某个下载任务
     *
     * @param url  下载的url
     * @param file 存储在某个位置上的文件
     * @return DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper pauseTask(String url, File file);

/**
     * 取消某个下载任务，会将之前所下载的进行删除
     *
     * @param url  下载的url
     * @param file 存储在某个位置上的文件
     * @return DownloadHelper 自身 (方便链式调用)
     */
    public DownloadHelper cancleTask(String url, File file);
}

```

#### **2. FileInfo.java**

```java
package com.yaoxiaowen.download;
/**
 * 文件信息，javaBean形式
 */
public class FileInfo implements Serializable{
    private String id;   //文件的唯一标识符 (url+文件存储路径)
    private String downloadUrl;   //下载的url
    private String filePath;  //文件存放的路径位置
    private long size;   //文件的总尺寸
    private long downloadLocation; // 下载的位置(就是当前已经下载过的size，也是断点的位置)

    @IntRange(from = DownloadStatus.WAIT, to = DownloadStatus.FAIL)
    private int downloadStatus = DownloadStatus.PAUSE;   //下载的状态信息
}
```

#### **3. DownloadConstant.java**
```java
package com.yaoxiaowen.download;
public class DownloadConstant {
    /**
     * 下载过程会通过发送广播, 广播通过intent携带文件数据的 信息。
     * intent 的key值就是该字段
     * eg : FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(DownloadConstant.EXTRA_INTENT_DOWNLOAD);
     */
    public static final String EXTRA_INTENT_DOWNLOAD = "yaoxiaowen_download_extra";
}
```


#### **4. DownloadConstant.java**

```java
package com.yaoxiaowen.download;
/**
 * 标示着 下载过程中的状态
 */
public class DownloadStatus {
    // Answer to the Ultimate Question of Life, The Universe, and Everything is 42
    public static final int WAIT = 42;       //等待
    public static final int PREPARE = 43;    //准备
    public static final int LOADING = 44;    //下载中
    public static final int PAUSE = 45;      //暂停
    public static final int COMPLETE = 46;   //完成
    public static final int FAIL = 47;       //失败
    public static final int CANCLE = 48;       //取消下载
}
```

## TODO
+ 对于单一任务的多线程执行。
+ 能够监听网络状态，自动暂停和恢复。
+ 多线程的加锁方式的优化，提高效率。
+ 对外的API仍需优化才行。

## 技术原理简介
   所谓断点下载，其实也不复杂。注意以下几点内容。
   + `httpURLconnection#setRequestProperty`方法通过设置，可以从服务器指定位置读取数据。eg:`conn.setRequestProperty("Range", "bytes=" + 500 + "-" + 1000);`
   + 普通的`File`对象并不支持从指定位置写入数据，我们需要使用`RandomAccessFile`来实现从指定位置给文件写入数据的功能。`void seek(long offset)`
   + 每次下载时，需要记录断点的位置信息。(本项目中使用`sqlite`数据库来实现持久化)
   + 通过`ThreadPoolExecutor`来维护线程池。

> 关于多线程断点续传下载的文章，网上很多了，大家可以参考。


## other
+ *使用过程中有什么问题，可以提交issues或联系本人，尽力予以解决。*

## 后言
该READ.md在原READ.md上根据现代码进行的部分修改