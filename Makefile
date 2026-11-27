.PHONY: clean test

test: build/disasm_x86_instruction/c/main build/disasm_x86_instruction/java/build/classes/com/example/Main.class
	$<
	java -cp build/disasm_x86_instruction/java/build/classes com.example.Main

build/disasm_x86_instruction/java/build/classes/com/example/Main.class: build/disasm_x86_instruction/java/src/main/java/com/example/Main.java build/disasm_x86_instruction/java/src/main/java/com/example/Output.java build/disasm_x86_instruction/java/src/main/java/com/example/FunctionIL.java build/disasm_x86_instruction/java/src/main/java/com/example/FunctionLL.java build/disasm_x86_instruction/java/src/main/java/com/example/ImmutableList.java build/disasm_x86_instruction/java/src/main/java/com/example/ImmutableIntList.java
	javac -d build/disasm_x86_instruction/java/build/classes build/disasm_x86_instruction/java/src/main/java/com/example/*.java

build/disasm_x86_instruction/java/src/main/java/com/example/Function%.java: samples/disasm_x86_instruction/java/Function%.java build/disasm_x86_instruction/java/src/main/java/com/example/Output.java
	cp $< $@

build/disasm_x86_instruction/java/src/main/java/com/example/Immutable%.java: samples/disasm_x86_instruction/java/Immutable%.java build/disasm_x86_instruction/java/src/main/java/com/example/Output.java
	cp $< $@

build/disasm_x86_instruction/java/src/main/java/com/example/Main.java: samples/disasm_x86_instruction/java/Main.java build/disasm_x86_instruction/java/src/main/java/com/example/Output.java
	cp $< $@

build/disasm_x86_instruction/java/src/main/java/com/example/Output.java: samples/disasm_x86_instruction/logic/disasm_x86_instruction.logic build/disasm_x86_instruction/java/src/main/java
	./gradlew run --args="--outputLanguage java --outputDirectory build/disasm_x86_instruction/java/src/main/java $<"

build/disasm_x86_instruction/c/main: build/disasm_x86_instruction/c/main.c build/disasm_x86_instruction/c/output.c
	cc build/disasm_x86_instruction/c/main.c build/disasm_x86_instruction/c/output.c -o $@

build/disasm_x86_instruction/c/output.c: samples/disasm_x86_instruction/logic/disasm_x86_instruction.logic build/disasm_x86_instruction/c
	./gradlew run --args="--outputDirectory build/disasm_x86_instruction/c $<"

build/disasm_x86_instruction/c/main.c: samples/disasm_x86_instruction/c/main.c build/disasm_x86_instruction/c
	cp $< $@

build/disasm_x86_instruction/java/src/main/java: build/disasm_x86_instruction/java/src/main
	mkdir -p $@

build/disasm_x86_instruction/java/src/main: build/disasm_x86_instruction/java/src
	mkdir -p $@

build/disasm_x86_instruction/java/src: build/disasm_x86_instruction/java
	mkdir -p $@

build/disasm_x86_instruction/java: build/disasm_x86_instruction
	mkdir -p $@

build/disasm_x86_instruction/c: build/disasm_x86_instruction
	mkdir -p $@

build/disasm_x86_instruction: build
	mkdir -p $@

build:
	mkdir -p $@

clean:
	rm -r build
