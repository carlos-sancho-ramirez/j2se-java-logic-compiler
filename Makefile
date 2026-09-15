build/output: build/output.c build/output.h
	cc $< -o $@

build:
	mkdir build
