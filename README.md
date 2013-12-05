spacemonitor
============

A runnable jar that spits out stats about what's going on in GSCs,
using the Admin api.

Designed for a mirror pu, the following stats are monitored:

- pid
- hostname
- timestamp
- totalMemory
- heapUsedMemory
- nonHeapUsedMemory
- totalThreads
- cpuPercent
- redologSize
- redologSendBytesPerSecond
- gcCollectionCount
- mirrorTotalOperations
- mirrorSuccessfulOperations
- mirrorFailedOperations
- readCountPerSecond
- updateCountPerSecond
- writeCountPerSecond
- changePerSecond
- executePerSecond
- processorQueueSize
- activeTransactionCount

All stats are written into a csv file, with a corresponding header row.

The following parameters can be set on the command line as java properties (e.g. -Dfoo=bar):

+ spaceMonitor.fileOutputPath // default = /logs/stress/spacemonitor.log
+ spaceMonitor.adminUser // default = deployer
+ spaceMonitor.adminPassword // default = password
+ spaceMonitor.secured // default = true
+ spaceMonitor.locators // default = localhost:4170

Following "maven clean package", the startup command would be:

"java -jar target/spacemonitor-1.0-SNAPSHOT.one-jar.jar"
