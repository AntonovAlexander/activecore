#include <string.h>
#include "md5.h"


typedef union uwb {
	unsigned int w;
	unsigned char b[4];
} MD5union;
	
unsigned int concat(char a, char b, char c, char d) {


	MD5union un;
	un.b[0] = a;
	un.b[1] = b;
	un.b[2] = c;
	un.b[3] = d;

	return un.w;
}

int calculateMaxLength(int length) {
	const int chunksize = 64;
	int d = (length + 8) / chunksize;
	int o = (length + 8) % chunksize;
	if (0 == o) {
		return d * chunksize;
	}
	return (d + 1) * chunksize;
}

unsigned leftrotate(unsigned r, short N)
{
	unsigned  mask1 = (1 << N) - 1;
	return ((r >> (32 - N)) & mask1) | ((r << N) & ~mask1);
}

md5s md5(const char* input, int length)
{
	// s specifies the per-round shift amounts
	int s[64] = { 7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,
					5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,
					4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,
					6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21 };

	// (Or just use the following precomputed table):
	unsigned int K[64] = { 0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
							0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
							0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
							0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
							0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
							0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
							0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
							0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
							0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
							0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
							0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05,
							0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
							0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
							0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
							0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
							0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391 };

	// Initialize variables:
	unsigned int a0 = 0x67452301;   // A
	unsigned int b0 = 0xefcdab89;   // B
	unsigned int c0 = 0x98badcfe;   // C
	unsigned int d0 = 0x10325476;   // D

	// Pre-processing: adding a single 1 bit
	int maxlength = calculateMaxLength(length);
	int totalChunks = maxlength / 64;


	for (int ai = 0; ai < totalChunks; ++ai) {
		unsigned int M[16];
		for (int j = 0; j < 16; ++j) {
			char values[4];
			for (int k = 0; k < 4; ++k) {
				int index = ai * 64 + 4 * j + k;
				//values[k] = getNextByte(input, length, offset);

				int realIndex = index;
				if (realIndex < length) {
					values[k] = input[realIndex];
				}

				else if (realIndex == length) {
					values[k] = 0x80;
				}
				else if (index < maxlength - 8) {
					values[k] = 0;
				}
				else if (index >= maxlength - 4) {
					values[k] = 0;
				}
				else {
					long int bytenum = maxlength - index - 4;

					unsigned long value = 8 * length;

					union {
						unsigned w;
						unsigned char b[4];
					} lunion;

					lunion.w = value;

					char result = lunion.b[4 - bytenum];

					values[k] = result;
				}

			}
			M[j] = concat(values[0], values[1], values[2], values[3]);
		}

		// Initialize hash value for this chunk:
		unsigned int A = a0;
		unsigned int B = b0;
		unsigned int C = c0;
		unsigned int D = d0;
		// Main loop:
		for (int i = 0; i < 64; ++i) {
			unsigned int F, g;
			if (0 <= i && i <= 15) {
				F = (B & C) | (~B & D);
				g = i;
			}
			else if (16 <= i && i <= 31) {
				F = (D & B) | ((~D) & C);
				g = (5 * i + 1) % 16;
			}
			else if (32 <= i && i <= 47) {
				F = B ^ C ^ D;
				g = (3 * i + 5) % 16;
			}
			else if (48 <= i && i <= 63) {
				F = C ^ (B | (~D));
				g = (7 * i) % 16;
			}
			// Be wary of the below definitions of a,b,c,d
			F = F + A + K[i] + M[g];  // M[g] must be a 32-bits block
			A = D;
			D = C;
			C = B;
			B = B + leftrotate(F, s[i]);
		}
		// Add this chunk's hash to result so far:
		a0 = a0 + A;
		b0 = b0 + B;
		c0 = c0 + C;
		d0 = d0 + D;
	}

	md5s packed;
	static const int size = 4;
	unsigned int unpacked[4] = { a0, b0, c0, d0 };
	
	for (int i = 0; i < size; ++i) {
		MD5union unionPacked, unionUnpacked;
		unionUnpacked.w = unpacked[i];
		
		//byte swap
		unionPacked.b[0] = unionUnpacked.b[3];
		unionPacked.b[1] = unionUnpacked.b[2];
		unionPacked.b[2] = unionUnpacked.b[1];
		unionPacked.b[3] = unionUnpacked.b[0];

		packed.v[i] = unionPacked.w;
	}

	return packed;
}
