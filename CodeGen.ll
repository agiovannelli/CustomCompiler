@codegen_c = common global i8* null, align 8
@codegen_l = common global i32 0, align 4

define void @codegen_proc1(i32* %w) {
%1 = alloca i32*, align 8
store i32* %w, i32** %1
%codegen_proc1_z = alloca i32, align 4
ret void
}

define i32 @main() {
%codegen_t = alloca [2 x i8], align 1
%codegen_a = alloca i32, align 4
%codegen_s = alloca i8**, align 8

ret i32 0
}
