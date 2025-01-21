SHELL   = /bin/bash
CC      = gcc
CFLAGS  = -fPIC -pedantic -Wall -Wextra
LDFLAGS = -shared
TARGET  = libexe.so
SOURCES = src/libexe.c

all: $(TARGET)

$(TARGET):
	$(CC) $(LDFLAGS) $(CFLAGS) $(SOURCES) -o $(TARGET)
