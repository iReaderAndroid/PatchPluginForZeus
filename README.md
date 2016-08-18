#[ZeusPlugin](https://github.com/iReaderAndroid/ZeusPlugin)插件框架热修复gradle插件源码

## 项目说明

本项目为[ZeusPlugin](https://github.com/iReaderAndroid/ZeusPlugin)所使用的热修复gradle插件源码，应用该插件可以动态地在编译出来的APK的每个类的构造函数中注入代码：

```java
if (Boolean.FALSE.booleanValue())System.out.println(Predicate.class);
```



## 使用方式

在项目根目录build.gradle依赖插件：

```groovy
 classpath 'zeusplugin:patch-gradle-plugin:1.0.0'
```

在项目module中build.gradle中应用插件

```groovy
apply plugin: 'patch-gradle-plugin'
patchPlugin{
    enable = true//如果enable为true则表明打出的包会在每个类的构造函数中注入代码
}
```

##鸣谢
[NuwaGradle](https://github.com/jasonross/NuwaGradle)
