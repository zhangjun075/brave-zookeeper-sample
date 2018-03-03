# watcher的注册
watcher只能触发一次，不能永久使用。所以，在更改节点的信息的时候，需要再次注册Watcher。

# ip的注册
在ip目录下注册本机IP,调用modify往ip节点写入ip信息，如果用作分片的话，这个data就写分片的结果。

# 信息修改后的触发操作
* 写完了，会触发客户端watcher的process方法，这个process方法在处理的时候，需要注意：1、判断是nodedatachangeed时间，其次判断下是否是path是否是本机ip对应的path。
* 根据第一点，然后执行相应的后续操作。后去到本节点的value即是分片的结果。

