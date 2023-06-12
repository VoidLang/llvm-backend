; ModuleID = 'factorial'
source_filename = "factorial"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"

define i32 @factorial(i32 %0) #0 {
entry:
  %"condition = n == 0" = icmp eq i32 %0, 0
  br i1 %"condition = n == 0", label %exit, label %if_false

if_false:                                         ; preds = %entry
  %"nMinusOne = n - 1" = sub i32 %0, 1
  %"factorialResult = factorial(nMinusOne)" = call i32 @factorial(i32 %"nMinusOne = n - 1")
  %"resultIfFalse = n * factorialResult" = mul i32 %0, %"factorialResult = factorial(nMinusOne)"
  br label %exit

exit:                                             ; preds = %if_false, %entry
  %result = phi i32 [ 1, %entry ], [ %"resultIfFalse = n * factorialResult", %if_false ]
  ret i32 %result
}

define i32 @main() #0 {
entry:
  %factorialResult = call i32 @factorial(i32 10)
  ret i32 %factorialResult
}

attributes #0 = { "frame-pointer"="none" }
