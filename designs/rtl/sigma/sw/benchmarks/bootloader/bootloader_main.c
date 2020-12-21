#include "io.h"
#include "loadelf.h"

#define IMAGE_LOCATION IO_BUF_ADDR

int main(int argc, char* argv[])
{
  void (*app_vector)(void);
  unsigned int entry_point = 0;
  
  if (loadelf(IMAGE_LOCATION, &entry_point)) {
    IO_LED = -1;
  } else {
    void (*app_vector)(void) = (void (*)(void))entry_point;
    app_vector();
  }
}