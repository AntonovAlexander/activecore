typedef struct {
	unsigned int v[4];
} md5s;

md5s md5(const char* input, int length);