#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>

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

int64_t malloc_exe(int64_t size){
    return (int64_t) malloc(size);
}

void free_exe(int64_t pointer){
    free((void*)pointer);
}