### Sigma_tile address map

HW block | Start address | End address | Size | Type | Description
-------- | ------------- | ----------- | ---- | ---- | -----------
RAM | 0x00000000 | 0x000FFFFF | 1 MB | rw | Local CPU RAM
SFR: IDCODE | 0x00100000 | 0x00100000 | 4 B | r | Constant: 0xdeadbeef
SFR: CTRL | 0x00100004 | 0x00100004 | 4 B | rw | Control register: [0] - software reset
SFR: CORENUM | 0x00100008 | 0x00100008 | 4 B | r | Sigma tile ID
SFR: SGI | 0x0010000C | 0x0010000C | 4 B | w | Software generated interrupt: [3:0] - interrupt number
SFR: TIMER_CTRL | 0x00100010 | 0x00100014 | 4 B | rw | Timer control register: [0] - start; [1] - autoreload
SFR: TIMER_PERIOD | 0x00100014 | 0x00100014 | 4 B | rw | Timer period
XIF | 0x80000000 | 0xFFFFFFFF | 2 GB | rw | Expansion interface
