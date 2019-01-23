	.text
	.file	"hello.ll"
	.globl	main
	.align	16, 0x90
	.type	main,@function
main:                                   # @main
# BB#0:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$8, %esp
	movl	$0, -4(%ebp)
	movl	$0, -8(%ebp)
	xorl	%eax, %eax
	testb	%al, %al
	je	.LBB0_1
# BB#2:
	movl	$9, -8(%ebp)
	jmp	.LBB0_3
.LBB0_1:
	movl	$8, -8(%ebp)
.LBB0_3:
	movl	-4(%ebp), %eax
	addl	$8, %esp
	popl	%ebp
	retl
.Lfunc_end0:
	.size	main, .Lfunc_end0-main


	.ident	"clang version 3.8.0-2ubuntu4 (tags/RELEASE_380/final)"
	.section	".note.GNU-stack","",@progbits
