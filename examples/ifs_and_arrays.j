.class public ifandarray
.super java/lang/Object

.field static x [I

.method static public <clinit>()V
.limit locals 1  
.limit stack 3  

bipush 10
newarray int
putstatic ifandarray/x [I

iconst_0
istore_0
loop0:
iload_0
getstatic ifandarray/x [I
arraylength
if_icmpge loop0_end
getstatic ifandarray/x [I
iload_0
iconst_m1
iastore
iinc 0 1
goto loop0
loop0_end:

return
.end method

.method public static f(I)[I
.limit locals 4  
.limit stack 3  

iconst_1
newarray int
astore_1

iload_0
ifge else0

iconst_5
newarray int
astore_1

iload_0
istore_3
iconst_0
istore_2
loop1:
iload_2
aload_1
arraylength
if_icmpge loop1_end
aload_1
iload_2
iload_3
iastore
iinc 2 1
goto loop1
loop1_end:


goto if0_end

else0:
iload_0
ifne else1

iconst_3
newarray int
astore_1

iconst_0
istore_2
loop2:
iload_2
aload_1
arraylength
if_icmpge loop2_end
aload_1
iload_2
iconst_3
iastore
iinc 2 1
goto loop2
loop2_end:


aload_1
iconst_2
iconst_m1
iastore

iconst_0
istore_2
loop3:
iload_2
getstatic ifandarray/x [I
arraylength
if_icmpge loop3_end
getstatic ifandarray/x [I
iload_2
iconst_3
iastore
iinc 2 1
goto loop3
loop3_end:


getstatic ifandarray/x [I
iconst_2
iconst_m1
iastore

goto if1_end

else1:
iload_0
iconst_5
if_icmple else2

iconst_0
istore_2
loop4:
iload_2
aload_1
arraylength
if_icmpge loop4_end
aload_1
iload_2
bipush -100
iastore
iinc 2 1
goto loop4
loop4_end:


goto if2_end

else2:
bipush 50
newarray int
astore_1

iconst_0
istore_2
loop5:
iload_2
aload_1
arraylength
if_icmpge loop5_end
aload_1
iload_2
bipush 25
iastore
iinc 2 1
goto loop5
loop5_end:



if2_end:



if1_end:



if0_end:


aload_1
areturn
.end method

.method public static print_array([I)V
.limit locals 4  
.limit stack 2  

iconst_0
istore_1

aload_0
arraylength
istore_2

loop6:

iload_1
iload_2
if_icmpge loop6_end

ldc "index: "
iload_1
invokestatic io/print(Ljava/lang/String;I)V

aload_0
iload_1
iaload
istore_3

ldc " value: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

iinc 1 1

goto loop6

loop6_end:


return
.end method

.method public static main([Ljava/lang/String;)V
.limit locals 2  
.limit stack 2  

ldc "Enter arg: "
invokestatic io/print(Ljava/lang/String;)V

invokestatic io/read()I
istore_1

iload_1
bipush -5
if_icmplt else3

iload_1
invokestatic ifandarray/f(I)[I
astore_0

ldc "res:"
invokestatic io/println(Ljava/lang/String;)V

aload_0
invokestatic ifandarray/print_array([I)V

ldc "x:"
invokestatic io/println(Ljava/lang/String;)V

getstatic ifandarray/x [I
invokestatic ifandarray/print_array([I)V

goto if3_end

else3:
ldc "Higher please"
invokestatic io/println(Ljava/lang/String;)V


if3_end:


return
.end method

