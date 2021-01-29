#ifndef SIGMA_SFR_H
#define SIGMA_SFR_H

#define SFR_IDCODE          (*(volatile unsigned int *)(0x00100001))
#define SFR_CTRL            (*(volatile unsigned int *)(0x00100004))
#define SFR_CORENUM         (*(volatile unsigned int *)(0x00100008))

#define SFR_IRQ_EN          (*(volatile unsigned int *)(0x00100010))
#define SFR_SGI             (*(volatile unsigned int *)(0x00100014))

#define SFR_TIMER_CTRL      (*(volatile unsigned int *)(0x00100020))
#define SFR_TIMER_PERIOD    (*(volatile unsigned int *)(0x00100024))
#define SFR_TIMER_VALUE     (*(volatile unsigned int *)(0x00100028))

#define TIMER_START_FLAG    0x1
#define TIMER_RELOAD_FLAG   0x2

#define TIMER_IRQ_NUM    0x1
#define BTN_IRQ_NUM      0x3

#endif // SIGMA_SFR_H