#include "io.h"
#include "loadelf.h"

#define IMAGE_LOCATION 0x1000

int main(int argc, char* argv[])
{
  void (*go_to_prog)(void);
  unsigned int entry_point = 0;
  
  if (loadelf(IMAGE_LOCATION, &entry_point)) {
    IO_LED = -1;
  } else {
    void (*go_to_prog)(void) = (void (*)(void))entry_point;
    go_to_prog();
  }
}