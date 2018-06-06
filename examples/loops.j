.class public loops
.super java/lang/Object

.field static x I = 3
.field static y I = 2

.method public static f()V
.limit locals 1  
.limit stack 2  

loop0:

getstatic loops/x I
ifle loop0_end

ldc "I am x: "
getstatic loops/x I
invokestatic io/println(Ljava/lang/String;I)V

ldc "What are you: "
invokestatic io/print(Ljava/lang/String;)V

invokestatic io/read()I
istore_0

iload_0
getstatic loops/x I
if_icmple if0_end

getstatic loops/x I
iconst_1
isub
putstatic loops/x I


if0_end:


goto loop0

loop0_end:


iconst_3
putstatic loops/x I

return
.end method

.method public static main([Ljava/lang/String;)V
.limit locals 1  
.limit stack 2  

loop1:

getstatic loops/y I
ifle loop1_end

invokestatic loops/f()V

getstatic loops/y I
iconst_1
isub
putstatic loops/y I

ldc "y is "
getstatic loops/y I
invokestatic io/println(Ljava/lang/String;I)V

goto loop1

loop1_end:


return
.end method

