### Sigma_tile address map

HW block | Start address | End address | Size | Type | Reset value | Description
-------- | ------------- | ----------- | ---- | ---- | ----------- | -----------
RAM | 0x00000000 | 0x000FFFFF | 1 MB | rw | See core params | Tightly coupled scratchpad RAM
SFR: IDCODE | 0x00100000 | 0x00100000 | 4 B | r | 0xdeadbeef | Constant for loopback test
SFR: CTRL | 0x00100004 | 0x00100004 | 4 B | rw | See core params | Control register: [0] - software reset; [1] - software reset auto-clear flag
SFR: CORENUM | 0x00100008 | 0x00100008 | 4 B | r | See core params | Sigma tile ID
SFR: IRQ_EN | 0x00100010 | 0x00100010 | 4 B | rw | 0x00000000 | Interrupt enable flags
SFR: SGI | 0x00100014 | 0x00100014 | 4 B | w | Undefined | Software generated interrupt: [3:0] - interrupt number
SFR: TIMER_CTRL | 0x00100020 | 0x00100020 | 4 B | rw | 0x00000000 | Timer control register: [0] - start; [1] - autoreload
SFR: TIMER_PERIOD | 0x00100024 | 0x00100024 | 4 B | rw | 0x00000000 | Timer period
SFR: TIMER_VALUE | 0x00100028 | 0x00100028 | 4 B | rw | 0x00000000 | Timer value
XIF | 0x80000000 | 0xFFFFFFFF | 2 GB | rw | Undefined | Expansion interface
