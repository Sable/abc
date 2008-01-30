
package style;

aspect PublicAccessFields {

	declare warning :
		get (public !final * *) || set(public * *) :
		"Please consider using non-public access";
}
