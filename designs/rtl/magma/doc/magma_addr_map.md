### Magma MPSoC address map

HW block | Start address | End address | Size | Type | Description
-------- | ------------- | ----------- | ---- | ---- | -----------
sigma tile 0 | 0x00000000 | 0x0FFFFFFF | 256 MB | rw | Sigma tile 0 space
sigma tile 1 | 0x10000000 | 0x1FFFFFFF | 256 MB | rw | Sigma tile 1 space
sigma tile 2 | 0x20000000 | 0x2FFFFFFF | 256 MB | rw | Sigma tile 2 space
sigma tile 3 | 0x30000000 | 0x3FFFFFFF | 256 MB | rw | Sigma tile 3 space
IO_LED 0 | 0x40000000 | 0x40000000 | 4 B | rw | LED register 0 [3:0]
IO_LED 1 | 0x40000004 | 0x40000004 | 4 B | rw | LED register 1 [3:0]
IO_LED 2 | 0x40000008 | 0x40000008 | 4 B | rw | LED register 2 [3:0]
IO_LED 3 | 0x4000000C | 0x4000000C | 4 B | rw | LED register 3 [3:0]
IO_SW | 0x40000010 | 0x40000010 | 4 B | r | Switches register
