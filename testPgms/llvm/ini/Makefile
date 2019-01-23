default:: buildFull

buildLL: hello.ll
	llc hello.ll -o hello.s
	gcc hello.s -o hello.x

buildCut: cut.ll
	llc cut.ll -o cut.s
	gcc cut.s -o cut.x

buildS: hello.c
	clang -S -emit-llvm hello.c -o hello.ll
	llc hello.ll -o hello.s
	gcc hello.s -o hello.x

buildC: hello.c
	gcc -o hello.o hello.c

buildFull: hello.c
	make buildS
	make buildC

runll: hello.x
	./hello.x

rungcc: hello.o
	./hello.o

clean:
	rm hello.ll
	rm *.s
	rm *.x
	rm *.o
#clang -S -emit-llvm hello.c
#llc hello.ll
#gcc hello.s -o hello
#./hello
