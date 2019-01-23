#include <stdio.h>

int proc(int a)
{
	int b;
	b=10;
	a=b;
	return a;
}

int main(void)
{
	int c;
	c=proc(1);
	printf("%d\n",c);
	return 0;
}