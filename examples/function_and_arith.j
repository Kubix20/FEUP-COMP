.class public funcandarith
.super java/lang/Object

.field static x I = 3

.method public static f()V
.limit locals 7  
.limit stack 2  

getstatic funcandarith/x I
iconst_1
isub
putstatic funcandarith/x I

getstatic funcandarith/x I
ifle if0_end

getstatic funcandarith/x I
istore_0

bipush -10
istore_1

iload_0
iload_1
iadd
istore_2

iload_2
iload_1
imul
istore_3

iload_3
iload_0
idiv
istore 4

iload 4
iconst_1
ishr
istore 5

iload 5
iconst_3
ishl
istore 6

iload_0
invokestatic io/println(I)V

iload_1
invokestatic io/println(I)V

iload_2
invokestatic io/println(I)V

iload_3
invokestatic io/println(I)V

iload 4
invokestatic io/println(I)V

iload 5
invokestatic io/println(I)V

iload 6
invokestatic io/println(I)V

ldc "Back to main"
invokestatic io/println(Ljava/lang/String;)V

aconst_null
invokestatic funcandarith/main([Ljava/lang/String;)V


if0_end:


return
.end method

.method public static main([Ljava/lang/String;)V
.limit locals 1  
.limit stack 1  

ldc "Back to f"
invokestatic io/println(Ljava/lang/String;)V

invokestatic funcandarith/f()V

ldc "Over"
invokestatic io/println(Ljava/lang/String;)V

return
.end method

