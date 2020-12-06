# PatchUpdate
增量更新NDK

生成差分包和合并差分包都可以在app实现
如果只要合并功能，可以把bsdiff.c删除，并删除相应native方法

还有另一种方法就是生成差分dll文件，适用于更多场景
