// See LICENSE for license details.

//**************************************************************************
// Software multiplication
//--------------------------------------------------------------------------

int mul_sw( int a, int b )
{
  int sign = 1;
  if (b < 0)
  {
      sign = -sign;
      b = -b;
  }
  if (a < 0)
  {
      sign = -sign;
      a = -a;
  }
  int ret = 0;
  while (b)
  {
    if (b & 1)
    {
      ret = ret + a;
    }
    a <<= 1;
    b >>= 1;
  }
  if (sign < 0)
    return -ret;
  return ret;
}
