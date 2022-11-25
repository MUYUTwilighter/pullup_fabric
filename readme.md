# Pullup 飞行警报

一个高度定制化的模组，玩家在 Minecraft 鞘翅飞行时添加飞行警报。该模组开放了接口，可以让用户自定义各种检测条件与播放的音效。  

多人游戏：如果客户端安装，服务端未安装，客户端只加载上一次加载的条件组；如果客户端安装，且服务端安装，客户端自动加载来自服务端的条件组（可以自行更改逻辑）

## 选项与配置

### 指令

#### 客户端指令
- `/pullupClient load <文件名>` : 加载指定的条件组，该操作会覆盖来自服务端的条件组文件
- `/pullupClient load default` : 加载默认条件组，该操作会覆盖来自服务端的条件组文件
- `/pullupClient enable` : 开启飞行警报
- `/pullupClient disable` : 关闭飞行警报
- `/pullupClient enableServer` : 允许加载来自服务端的条件组
- `/pullupClient disableServer` : 不允许加载来自服务端的条件组
- `/pullupClient grab` : 向服务端请求条件组文件（需要先允许加载来自服务端的条件组）

#### 服务端指令
- /pullupServer load <文件名> : 加载指定的条件组，该操作会覆盖来自服务端的条件组文件
- /pullupServer load default : 加载默认条件组，该操作会覆盖来自服务端的条件组文件
- /pullupServer enableSend : 允许服务端自动发送当前加载的条件组文件
- /pullupServer disableSend : 不允许服务端自动发送当前加载的条件组文件

### 配置文件

配置文件位于目录 ".minecraft/config/pullup.json" 或 ".minecraft/versions/<版本名>/config/pullup.json" 中。

- `enable` : 是否开启飞行警报  
- `loadServer`: 是否允许加载来自服务端的条件组  
- `sendServer`: 是否允许服务端向客户端发送服务端的条件组，如果为 `false`，客户端的请求会被回绝  
- `maxDistance` : 部分参数的检测距离上限  
- `sendDelay` : 服务端响应客户端拉取条件组的最小间隔时间，在该间隔内超过 1 次请求，该请求会被回绝  
- `loadSet` : 加载的条件组文件名

## 自定义警报
### 格式
自定义警报需要自行创建 条件组 文件到 ".minecraft/pullup/" 中，并且文件需要符合 [JSON 格式](https://www.runoob.com/json/json-tutorial.html) 。  
文件名本身将作为命名空间，所以请注意符合 Identifier 格式（只能使用小写字母、数字和下划线，数字不可作为开头）。  
条件组文件格式如下

```Json
[
    {
        "name": "example",
        "sound": "pullup:test",
        "loop_play": true,
        "play_delay": 20,
        "check_delay": 40,
        "arguments": {
            "a": "pullup:absolute_height",
            "b": "pullup:yaw"
        },
        "expressions": [
            "a - 120",
            "5 - | b - 5 |"
        ]
    }
]
```

最外层是由一层列表符号包裹，里面的每一项是一个条件。  
在条件中，每一个键的意义如下：  
- `name`: 条件组的命名，同样需要符合 [`Identifier` 条件](#格式)
- `sound`: 触发该条件时播放的资源包中的音频
- `loop_play`: 是否循环播放
- `play_delay`: 循环播放警报的间隔
- `check_delay`: 检测频率
- `arguments`: 检测表达式中所需要用到参数的对照表  
- `expressions`: 检测表达式

### 参数
参数是模组预先制定好的，在写表达式的时候可以直接引用。  
在条件组文件中，每一个条件单独设置一组对照表，在检测表达式中使用的时候不需要引用下面的参数全称，直接使用对照的变量名即可。  
例如，在 [上文]( #自定义警报 ) 中的例子，由于在 arguments 中已设立对照关系 `"a": "pullup:absolute_height"` ，在检测表达式中，用 `a` 即可表示 `pullup:absolute_height`。  
需要注意的是，用在检测表达式中的变量名需要符合 [`Identifier` 条件](#格式) 。  

#### 参数列表
- pullup:absolute_height : 玩家的绝对高度，即坐标 Y 值
- pullup:relative_height : 玩家距离脚下方块的距离
- pullup:speed : 玩家的速度
- pullup:horizontal_speed : 玩家的水平速度
- pullup:vertical_speed : 玩家的垂直速度
- pullup:yaw : 玩家的水平（即左右）角度
- pullup:pitch : 玩家的垂直（即上下）角度
- pullup:distance_ahead : 玩家所指向方块（包括流体）的距离
- pullup:distance_pitched_10 : 玩家所指向方向下方 10° 的指向方块（包括流体）的距离  
该参数在 0.0.4 版本中暂时禁用（将在未来以更高级的方式加回）  
- pullup:distance_pitched_m10 : 玩家所指向方向上方 10° 的指向方块（包括流体）的距离  
该参数在 0.0.4 版本中暂时禁用（将在未来以更高级的方式加回）
- pullup:flight_ticks : 从开始飞行到当前时间经过的 tick 数量（1 tick = 50 ms）

### 检测表达式
在一个条件内，可以设置多个检测表达式。检测表达式是一个数学表达式，条件表达式符号的规则有所改动；当一个条件内所有的表达式的计算结果都大于 0 时，该条件才会被认定通过，并播放警报。  

#### 运算符列表
运算符的优先级由上到下依次递减。  
GitHub 中查看此条目可能无法正常显示，请使用能够加载内嵌 LaTeX 的方式浏览。

- A ^ B  
$ A ^ B $ 求乘方  
***
- +A  
$ +A $ 求本数  
- -A  
$ -A $ 求相反数  
***
- A * B  
$ A \times B $ 求积  
- A / B  
$ A \div B $ 求商  
- A % B  
$ A \% B $ 求余  
***
- A + B  
$ A + B $ 求和  
- A - B  
$ A - B $ 求差  
***
- A > B  
\[
    \begin{cases}
        1, A > B \\
        -1, A \leq B
    \end{cases}
\] 
- A >= B  
\[
    \begin{cases}
        1, A \geq B \\
        -1, A < B
    \end{cases}
\]  
- A < B  
\[
    \begin{cases}
        1, A < B \\
        -1, A \geq B
    \end{cases}
\]  
- A <= B  
\[
    \begin{cases}
        1, A \leq B \\
        -1, A > B
    \end{cases}
\]  
- A == B  
\[
    \begin{cases}
        1, A = B \\
        -1, A \neq B
    \end{cases}
\]  
***
- A & B  
\[
    \begin{cases}
        1, A \geq 0 且 B \geq 0 \\
        -1, A < B 可兼或 B < 0
    \end{cases}
\]  
***
- A | B  
\[
    \begin{cases}
        1, A \leq 0 且 B \leq 0 \\
        -1, A > B 可兼或 B > 0
    \end{cases}
\]

#### 函数列表
函数的调用需要按照 "函数名(参数, 参数, ...)" 的格式

- abs(A)  
$ |A| $ 求绝对值  
- pow(A, B)  
$ A ^ B $ 求幂  
- signum(A)  
\[
    \begin{cases}
        1, A > 0 \\
        0, A = 0\\
        -1, A < 0
    \end{cases}
\]
- sin(A)  
$ \sin(A) $ 求正弦  
- cos(A)  
$ \cos(A) $ 求余弦  
- tan(A)  
$ \tan{A} $ 求正切  
- asin(A)  
$ \arcsin(A) $ 求反正弦  
- acos(A)  
$ \arccos(A) $ 求反余弦  
- atan(A)  
$ \arctan(A) $ 求反正切  
- sinh(A)  
$ \sinh(A) $ 求双曲正弦  
- cosh(A)  
$ \cosh(A) $ 求双曲余弦  
- tanh(A)  
$ \tanh(A) $ 求双曲正切  
- sqrt(A)  
$ \sqrt{A} $ 求平方根  
- cbrt(A)  
$ ^3 \sqrt{A} $ 求立方根  
- ceil(A)  
$ \lceil A \rceil $ 向上取整  
- floor(A)  
$ \lfloor A \rfloor $ 向下取整  
- exp(A)  
$ e^A $ 求自然指数  
- expm1(A)  
$ e^A - 1 $ 求平滑自然指数  
- log(A)  
$ \ln{A} $ 求对数  
- log2(A)  
$ \log_{2}{A} $ 求以 2 为低的对数  
- log10(A)  
$ \log_{10}{A} $ 求以 2 为低的对数  
- log1p(A)  
$ \ln{(A + 1)} $ 求平滑自然对数  