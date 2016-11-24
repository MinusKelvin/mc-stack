declare
declare
function loop
	push 0x010001
	ldlocal
	dup
	push "print"
	ldglobal
	call
	push 1
	sub
	dup
	push 0x010001
	stlocal
	push 0
	neq
	push 0x010000
	ldlocal
	tailif

dup
push 0
stlocal
push 10
push 1
stlocal
call

push "test "
push "world"
concat
push "print"
ldglobal
call

function outer
	declare
	push 0
	stlocal
	function closure
		push 0x010000
		ldlocal
		push "print"
		ldglobal
		tailcall

dup
push 0
swap
call
dup
call
swap
push 35
swap
call
dup
call
swap
call
call

push -128
push "print"
ldglobal
call

push 128
push "print"
ldglobal
call