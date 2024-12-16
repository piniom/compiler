#include <stdio.h>

void print_text() {
    printf("Hello, world!\n");
}

void print_bool(int x) {
    if (x) {
        printf("true\n");
    } else {
        printf("false\n");
    }
}

void print_int(int x) {
    printf("%d\n", x);
}