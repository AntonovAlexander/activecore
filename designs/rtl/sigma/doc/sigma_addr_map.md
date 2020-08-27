### Sigma MCU address map

HW block | Start address | End address | Size | Type | Description
-------- | ------------- | ----------- | ---- | ---- | -----------
sigma tile | 0x00000000 | 0x001FFFFF | 2 MB | rw | Sigma tile space
IO_LED | 0x80000000 | 0x80000000 | 4 B | rw | LED register
IO_SW | 0x80000004 | 0x80000004 | 4 B | r | Switches register
