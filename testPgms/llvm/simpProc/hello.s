	.text
	.file	"hello.ll"
	.globl	simp
	.align	16, 0x90
	.type	simp,@function
simp:                                   # @simp
# BB#0:
	pushl	%ebp
	movl	%esp, %ebp
	popl	%ebp
	retl
.Lfunc_end0:
	.size	simp, .Lfunc_end0-simp

	.globl	main
	.align	16, 0x90
	.type	main,@function
main:                                   # @main
# BB#0:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$8, %esp
	calll	simp
	xorl	%eax, %eax
	addl	$8, %esp
	popl	%ebp
	retl
.Lfunc_end1:
	.size	main, .Lfunc_end1-main


	.ident	"clang version 3.8.0-2ubuntu4 (tags/RELEASE_380/final)"
	.section	".note.GNU-stack","",@progbits
