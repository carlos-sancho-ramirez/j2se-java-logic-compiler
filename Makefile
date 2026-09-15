build/disasm_x86_instruction: build/main.c build/output.c build/output.h
	cc build/main.c build/output.c -o $@

build/main.c: samples/disasm_x86_instruction/c/main.c build
	cp $< $@

build:
	mkdir build
