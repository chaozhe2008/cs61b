# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Class 1: Repository
处理command的class 用静态方法

#### Fields

1. Field 1
2. Field 2


### Class 2: Commit

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

## Intro video 1 介绍
1. 用任何命令方法都是运行Main 后面加参数
2. 如果修改了代码 要删除原有.gitlet文件夹 重新生成可执行文件并且把可执行文件复制过去
## Intro video 2 介绍
1. .gitlet结构：三个区域：staging area/ commit area/ blob area
2. add: 把文件加入staging area，并且根据其内容生成一个blob并且绑定(blob是由文件内容决定的 如果文件内容变了 就会又一个新的blob)
3. commit: 
   1. 读取head commit和staging area
   2. 新的commit先clone旧的head 然后把parent设置成旧的head head指向新的commit
   3. 根据输入和时间modify 新的commit 的metadata
   4. 把当前staging area for commit 的文件生成一个commit 放入commit area
   5. 这个commit存的是一系列指向blob的指针(即最新版本)
   6. commit后清空staging area
   7. 最后要把在这过程中所有修改过或者新建的对象存储(Serialize)
   8. 为了避免serialize所有之前的对象导致文件过大 commit的object variable应该都变成string起到指针的作用
   9. 每个commit 的唯一id就是SHA-1 id
   10. git的做法：把每个object 存储为一个文件 文件名字是SHA-1 id


## 3/11进度
* 把commit里的set和map改成tree 解决了write再read后sha-1改变的问题
* 测试通过以下功能：每次commit 更新存储 head ID, 

