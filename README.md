# TrainTicketingSystem

多核与并发数据结构-用于列车售票的可线性化并发数据结构

![Travis (.com)](https://img.shields.io/travis/com/specialpointcentral/TrainTicketingSystem?logo=travis-ci&logoColor=white&style=flat-square&link=https://travis-ci.com/specialpointcentral/TrainTicketingSystem)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/specialpointcentral/TrainTicketingSystem/Java%20CI%20with%20Maven?logo=github&logoColor=whhite&style=flat-square)
![GitHub](https://img.shields.io/github/license/specialpointcentral/TrainTicketingSystem?style=flat-square)
![GitHub last commit](https://img.shields.io/github/last-commit/specialpointcentral/TrainTicketingSystem?style=flat-square)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/specialpointcentral/TrainTicketingSystem?style=flat-square)

## 数据结构说明

给定`Ticket`类：

```java
class Ticket{
    long tid;
    String passenger;
    int route;
    int coach;
    int seat;
    int departure;
    int arrival;
}
```

其中，`tid`是车票编号，`passenger`是乘客名字，`route`是列车车次，`coach`是车厢号，`seat`是座位号，`departure`是出发站编号，`arrival`是到达站编号。

给定`TicketingSystem`接口：

```java
public interface TicketingSystem {
    Ticket buyTicket(String passenger, int route, int departure, int arrival);
    int inquiry(int route, int departure, int arrival);
    boolean refundTicket(Ticket ticket);
}
```

其中：

- `buyTicket`是购票方法，即乘客`passenger`购买`route`车次从`departure`站到`arrival`站的车票1张。若购票成功，返回有效的`Ticket`对象；若失败（即无余票），返回无效的`Ticket`对象（即`return null`）。
- `refundTicket`是退票方法，对有效的`Ticket`对象返回`true`，对错误或无效的`Ticket`对象返回`false`。
- `inquriy`是查询余票方法，即查询`route`车次从`departure`站到`arrival`站的余票数。

## 完成`TicketingDS`类

完成一个用于列车售票的可线性化并发数据结构：`TicketingDS`类：

1. 实现`TicketingSystem`接口，
2. 提供`TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);`构造函数。

其中：

- `routenum`是车次总数（缺省为5个），
- `coachnum`是列车的车厢数目（缺省为8个），
- `seatnum`是每节车厢的座位数（缺省为100个），
- `stationnum`是每个车次经停站的数量（缺省为10个，含始发站和终点站），
- `threadnum`是并发购票的线程数（缺省为16个）。

为简单起见，假设每个车次的`coachnum`、`seatnum`和`stationnum`都相同。
车票涉及的各项参数均从1开始计数，例如车厢从1到8号，车站从1到10编号等。

## 完成多线程测试程序

需编写多线程测试程序，在`main`方法中用下述语句创建`TicketingDS`类的一个实例。

```java
final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
```

系统中同时存在`threadnum`个线程（缺省为16个），每个线程是一个票务代理，需要：

1. 按照60%查询余票，30%购票和10%退票的比率反复调用`TicketingDS`类的三种方法若干次（缺省为总共10000次）；
2. 按照线程数为4，8，16，32，64个的情况分别调用。

需要最后给出：

1. 给出每种方法调用的平均执行时间；
2. 同时计算系统的总吞吐率（单位时间内完成的方法调用总数）。

## 正确性要求

需要保证以下正确性：

- 每张车票都有一个唯一的编号`tid`，不能重复。
- 每一个`tid`的车票只能出售一次。退票后，原车票的`tid`作废。
- 每个区段有余票时，系统必须满足该区段的购票请求。
- 车票不能超卖，系统不能卖无座车票。
- 买票、退票和查询余票方法均需满足可线性化要求。

## 文件清单

所有Java程序放在`ticketingsystem`目录中，`trace.sh`文件放在`ticketingsystem`目录的上层目录中。
如果程序有多重目录，那么将主Java程序放在`ticketingsystem`目录中。

文件清单如下：

- `trace.sh`是trace生成脚本，用于正确性验证，不能更改。
- `pom.xml`是依赖配置文件，使用`mvn`。
- `.travis.yml`是CI配置文件，用于自动化测试。
- 文件夹`.github`是github自动化测试配置文件。
- 文件夹`src/main/java`为代码文件夹。
    1. `TicketingSystem.java`是规范文件，不能更改。
    2. `Trace.java`是trace生成程序，用于正确性验证，不能更改。
    3. `TicketingDS.java`是并发数据结构的实现。
    4. ... 其他的自建类。
    5. `PerformanceBenchmark.java`是JMH基准测试程序。
    6. `jmh.benchmark.PerformanceBenchmarkRunner.java`是JMH基准测试启动文件。

- 文件夹`src/test/java`为测试文件夹。
    1. `ticketingsystem`存放基本测试单元。
        - `UnitTest.java`为系统的单元测试，为单线程运行。
        - `RandomTest.java`为系统的随机测试，通过多线程，随机购、退、查票。
        - `MultiThreadTest.java`为多线程买、退票测试程序，通过多线程随机购、退票。
        - `TraceVerifyTest.java`为trace单线程可线性化比对测试。
    2. `verify`文件夹存放trace单线程可线性化比对测试资源文件
        - `Trace.java.copy`为Trace调用文件，会自动替换原先的Trace.java。
        - `verify.jar`为单线程线性化测试包。
    3. `linerChecker`文件夹存放trace多线程可线性化比对测试。
        - `check.sh`为启动脚本。
        - `checker.jar`为多线程线性化测试包。

## 使用说明

### 文件目录

项目文件主体在`src/main/java`下，你需要将你的文件放在`src/main/java/ticketingsystem`文件夹内，`PerformanceBenchmark.java`以及`jmh`文件夹用于基准测试不能删除。

1. 保证整个项目的结构。
2. 在`src/main/java/ticketingsystem`替换自己的实现。
3. 如果使用非`java-11`版本，请调整`pom.xml`。更改`your java version`为自己版本。

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <java.version>your java version</java.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version}</maven.compiler.target>
    ...
</properties>
```

### 使用`maven`

项目使用`maven`构建，运行前请安装`maven`。没有改变基本操作，常用的命令如下：

- 通过`mvn clean`清理生成文件
- 通过`mvn package`打成jar包
- 通过`mvn test`执行测试
- ...

### 使用`Junit`进行正确性测试

> 注意：你需要安装`maven`才能执行，并且在执行过程中会自动安装相应依赖。

项目使用`Junit`进行正确性测试，你可以使用：

- `mvn test`命令完成测试
- 查看运行结果，会报告测试数量以及通过测试点数量。

### 使用`JMH`进行性能测试

> 注意：你需要安装`maven`才能执行，并且在执行过程中会自动安装相应依赖。

项目使用`JMH`进行性能测试，你可以使用：

- `mvn package`将项目打包
- 在项目根目录下，运行：
    - `java -cp .\target\trainTicketingSystem-1.0-SNAPSHOT.jar ticketingsystem.jmh.benchmark.PerformanceBenchmarkRunner`。
    - 查看运行结果，结果单位为`ops/s`，即每秒操作数。这里的操作数与真实数量有差距，需要对数据乘上每次操作执行的买、退、查票动作数，即需要乘上64000。

### 使用CI自动化测试

项目支持`github workflow`以及`travis-ci`自动化测试，开箱即用。
每次`push`都会自动触发测试。

## 联系方式

![GitHub issues](https://img.shields.io/github/issues/specialpointcentral/TrainTicketingSystem?style=flat-square&link=https://github.com/specialpointcentral/TrainTicketingSystem/issues)
![GitHub followers](https://img.shields.io/github/followers/specialpointcentral?label=specialpointcentral&style=social&link=https://github.com/specialpointcentral)

任何问题欢迎提交issue。
