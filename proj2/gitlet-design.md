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
* TODO: 全局Blob容器 Stage for removal 如何persist

## 3/12进度
* 把removal变成一个staging area里的文件夹 里面的文件名字就是要删除的 和commit一起测试通过
* 完成commit commend检查，以下三种情况全都会报相应的错误信息不会执行新的command
  1. staging area和staging for removal都为空，
  2. 没有输入message
  3. message只含有空白字符
* log: 完成不涉及branch的部分（注意删除多余toString信息 目前保留为了方便测试)
* global-log: 完成
* find: 完成
* status: 打算搭好branch的框架以后再写
* Branch Intro Video Takeaways:
  1. A branch is a pointer to a commit
  2. The default branch is MASTER
  3. Adding a branch just adds a new pointer to the current HEAD
  4. The HEAD pointer points to the active branch
  5. When we create a new commit, only the active branch and the HEAD moves.
  6. The only thing checkout does is switch che Head pointer
  7. There are two branches != there are tree-like divergings structure
  8. Possible situation where the tree diverges:
     * checkout to a previous commit, make change and make new commit

## 3/13进度
* 开始处理branch 基本思路：
    1. 在commit文件夹里存一个文件夹叫branch
    2. branch内部放三个文件: master, new_branch 和head
    3. master和new_branch存放各自branch的最新commit的ID head存当前active branch的名字
    4. 刚开始的时候 默认head里是"master"
    5. head任何时候都是两个branch中的一个
    6. 把调取branch的操作放到一个新的class里 同样是static
* 要针对原有代码进行的修改：
    1. 不要Head file 新建master文件 也放在branch文件夹里 
    2. 将一部分initCommand的工作放到Branch 里面的initBranch函数里完成
    3. Branch里完成的任务：
       * create branch
       * get current head
       * get current branch file
* 修改结束后对已有功能进行测试(不包括branch和checkout) 未发现异常
* 实现branch命令 测试通过
* 实现rm-branch命令 测试通过
* 实现checkout命令:
    1. checkout -- fileName
       * Takes the version of the file as it exists in the head commit and puts it in the working directory, 
       * Overwriting the version of the file that’s already there if there is one. 
       * The new version of the file is not staged.
       * FAILURE: If the file does not exist in the previous commit, print "File does not exist in that commit." Do not change the CWD.
    2. checkout commitID -- fileName
       * Takes the version of the file in the commit with the given id, and puts it in the working directory
       * FAILURE:
         1. If no commit with the given id exists, print No commit with that id exists.
         2. if the file does not exist in the given commit, print the same message as for failure case 1.
    3. checkout branch
       * Takes all files in the commit at the head of the given branch, and puts them in the working directory.
       * At the end of this command, the given branch will now be considered the current branch (HEAD)
       * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
       * The staging area is cleared.
       * FAILURE: 
         * If no branch with that name exists, print No such branch exists.
         * If that branch is the current branch, print No need to checkout the current branch.
         * If a working file is untracked in the current branch and would be overwritten by the checkout, print "..."
* 测试checkout branch 出现问题 理清表述:
    * TODO: Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
    * FAILURE: If a working file is untracked in the current branch and would be overwritten by the checkout, print...
* 翻译成中文:
    * CWD中被当前branch追踪 却不会被要checkout的branch追踪的文件 会被删除
    * 如果一个文件会被checkout 重写(与checkout branch中某个文件名字相同) 但是还没有被current branch追踪 则会报错
    * 所以可以推知错误条件是：
      1. current branch里没有当前文件的sha1
      2. checkout branch中有当前文件的名字
      3. checkout branch中的版本sha1和当前版本不一样
    * 进一步得出结论：错误的条件是CWD中有某个文件的名字存在在checkout branch的追踪文件里 但其目前版本和checkout branch中的版本和current branch版本都不一样
* 理清思路后解决上述问题 
* 遇到新的问题：checkout可以正确修改branch里内容但是没有改变CWD中文件
* 解决上述问题： 必须先switch head再check out file 因为我的 checkoutFile里面是先getHead来决定checkout哪个版本
* checkout 测试通过 明天开始写status和merge


## 3/13进度
* 完成status 按照四种情况分别实现 测试通过





