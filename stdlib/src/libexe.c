#include <inttypes.h>
#include <stdio.h>


void print_int(int64_t a)
{
   printf("%" PRId64 "\n", a);
}

int64_t scan_int()
{
    int64_t a;
    scanf("%" PRId64, &a);
    return a;
}

