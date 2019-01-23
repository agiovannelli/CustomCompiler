default:: buildFull

buildS: hello.c
	clang -S -emit-llvm hello.c
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
	rm *.ll
	rm *.s
	rm *.x
	rm *.o
#clang -S -emit-llvm hello.c
#llc hello.ll
#gcc hello.s -o hello
#./hello
