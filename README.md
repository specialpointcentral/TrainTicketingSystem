# TrainTicketingSystem

多核与并发数据结构-用于列车售票的可线性化并发数据结构

![Travis (.com)](https://img.shields.io/travis/com/specialpointcentral/TrainTicketingSystem?logo=travis-ci&logoColor=white&style=flat-square)
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

1. `TicketingSystem.java`是规范文件，不能更改。
2. `Trace.java`是trace生成程序，用于正确性验证，不能更改。
3. `trace.sh`是trace生成脚本，用于正确性验证，不能更改。
4. `TicketingDS.java`是并发数据结构的实现。
5. `Test.java`实现多线程性能测试。
    - `Test.java`为测试的主类
    - `UnitTest.java`为系统的单元测试，为单线程运行
    - `RandomTest.java`为系统的随机测试，通过多线程，随机购、退票完成
