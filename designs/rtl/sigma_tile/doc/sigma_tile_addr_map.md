### Sigma_tile address map

HW block | Start address | End address | Size | Type | Description
-------- | ------------- | ----------- | ---- | ---- | -----------
RAM | 0x00000000 | 0x000FFFFF | 1 MB | rw | Local CPU RAM
CSR: IDCODE | 0x00100000 | 0x00100000 | 4 B | r | Constant: 0xdeadbeef
CSR: CTRL | 0x00100004 | 0x00100004 | 4 B | rw | Control CSR: [0] - soft CPU reset
CSR: corenum | 0x00100008 | 0x00100008 | 4 B | r | Sigma tile ID
CSR: MSI | 0x0010000C | 0x0010000C | 4 B | w | Soft interrupt request: [3:0] - interrupt number
XIF | 0x80000000 | 0xFFFFFFFF | 2 GB | rw | Expansion interface
