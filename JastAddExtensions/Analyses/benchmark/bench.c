int main()
{
	int	a;	// Assigned once
	int	b;
	int	c;	// Constant
	int	d;
	int	e;
	int	f;
	int	g;
	int	h;
	int	i;
	int	j;
	int	s;
	int	t;
	int	u;	
	int	v;
	int	n;

	put(__LINE__);	// This is one way to trace the execution
			// It will print the line number (as in ANSI C)

	n = get();
	a = get();

	b = 0;
	c = 12;
	g = 13;
	h = a + 14;
	s = 0;
	t = 0;

	put(a);

	while (n > 0) {
		b = b + 2 * c * 3 + 4 + 5;

		b = b + (a * b) - (b * a);
		b = b + (a + b) + (b + a) + (a + a) + (b + b);

 		b = b * 12;
		b = b * 13;

		put(b);

		if (c > 100)
			d = -1;
		else
			d = c + 1;

		put(c);
		put(d);

		e = 6 + d;
		e = a + e;
		e = e * 8;
		e = e / 16;

		if (e > 0)
			f = e + 12;
		else
			f = e - 12;
		g = g * f;
		h = h + 2;
		i = h * 8;
	
		s = s + n / i;
		t = t + n / i;

		u = 0;
		v = 0;
		while (u < n / (a * 4)) {
			u = u + 1;
			v = v * a;
		}
			
		put(s);

		if (s != t)
			b = b / a;

		
		b = a * b;

		b = a * b;
		e = e + a * b;
		
		d = a * b;

		if (a == 2) {
			d = d + c / a;
			d = d + c / n;
		} 

		h = a / n;

		if (h != 0)
			d = d - c / a;
		else {
			e = h * e * f;
			f = f - h * e * f;
		}

		e = e + c / a;
		e = e + c / n;
		e = 10;

		while (e > 0) {
			d = d + a * c;
			e = e - 1;
		}

		put(d);
		put(b);
		put(e);
		n = n - 1;
		put(n);
	}

	return 0;
}
