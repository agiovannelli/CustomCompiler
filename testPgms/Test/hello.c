#include <stdio.h>

void proc(int a)
{
	int b;
	b  = 10;
	a = b;
	return;
}

int y;

int main()
{
	proc(y);
}