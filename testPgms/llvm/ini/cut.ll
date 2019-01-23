; ModuleID = 'hello.c'
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

@_global_int = common global i32 0, align 4
@_global_float = common global float 0.000000e+00, align 4
@_global_char = common global i8 0, align 1
@_global_str = common global i8* null, align 8
@_global_bool = common global i32 0, align 4

; Function Attrs: nounwind uwtable
define i32 @main() #0 {
  %1 = alloca i32, align 4
  %_int = alloca i32, align 4
  %_float = alloca float, align 4
  %_char = alloca i8, align 1
  %_str = alloca i8*, align 8
  %_bool = alloca i32, align 4
  store i32 0, i32* %1, align 4
  ret i32 0
}

; Function Attrs: nounwind uwtable
define void @test() #0 {
  ret void
}

attributes #0 = { nounwind uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+fxsr,+mmx,+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = !{!"clang version 3.8.0-2ubuntu4 (tags/RELEASE_380/final)"}
@_global_int = common global i32 0, align 4
